/*
 *  Copyright (c) 2014 Andree Hagelstein, Maik Schulze, Deutsche Telekom AG. All Rights Reserved.
 *  Copyright (c) 2014 Ryan Michela.
 *  
 *  Filename: Task.java
 */
package rx.bukkit.task;

import rx.bukkit.RxJavaPlugin;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

/**
 * <p> The {@code Task} extends the {@link FutureTask} with new methods (see {@code continueWith}) to support
 * the composition of asynchronous operations. Therefor the {@code Task} can be used to wrap
 * a {@link Callable} and executes a callback function ({@link TaskFunc}) if
 * the {@code Task} is done.
 *
 * @param <T> The result type returned by this Task's {@code get} methods.
 */
public class Task<T> extends FutureTask<T> implements ITask<T>, ITaskStart
{
    private RxJavaPlugin plugin;
    
    /** The _new task list. */
    private final ArrayList<ITaskStart> _newTaskList = new ArrayList<ITaskStart>();
    
    /** The _executor. */
    private Executor _executor;
    
    /** The _id. */
    private String _id;
    
    /** The boolean is result read. */
    private boolean _blnIsResultRead = false;
    
    /** The boolean is done. */
    private boolean _blnIsDone = false;
    
    /** The boolean is started. */
    private boolean _blnIsStarted = false;

    
    /**
     * Creates a {@code Task} that will, upon starting, execute the given {@code Callable}. 
     *
     * @param callable the callable task
     */
    public Task(RxJavaPlugin plugin, Callable<T> callable)
    {
        this(plugin, callable, "");
    }
    
    /**
     * Creates a {@code Task} that will, upon starting, execute the given {@code Callable}.
     *
     * @param callable the callable task
     * @param id the id
     */
    public Task(RxJavaPlugin plugin, Callable<T> callable, String id)
    {
        this(plugin, callable, plugin.getTaskFactory().defaultExecutor, id);
    }

    /**
     * Creates a {@code Task} that will, upon starting, execute the given {@code Callable}.
     *
     * @param callable the callable task
     * @param executor the executor to use for the execution of the task
     */
    public Task(RxJavaPlugin plugin, Callable<T> callable, Executor executor)
    {
        this(plugin, callable, executor, "");
    }
    
    /**
     * Creates a {@code Task} that will, upon starting, execute the given {@code Callable}.
     *
     * @param callable the callable task
     * @param executor the executor to use for the execution of the task
     * @param id the id
     */
    public Task(RxJavaPlugin plugin, Callable<T> callable, Executor executor, String id)
    {
        super(callable);

        _executor = executor;
        _id = id;

        if (_executor == null)
        {
            throw new IllegalArgumentException("The executor argument cannot be null.");
        }
    }

    public String getId() {
        return _id;
    }

    /**
     * Creates a {@code Task} from the specified {@link TaskCompletionSource}.
     *
     * @param <V> The result type returned by the Task's {@code get} methods.
     * @param source the source to create the task from
     * @return the task
     */
    static <V> Task<V> fromSource(RxJavaPlugin plugin, final TaskCompletionSource<V> source)
    {
        final Task<V> task = new Task<V>(plugin, new Callable<V>()
        {
            @Override
            public V call() throws Exception
            {
                return null;
            }
        });

        source.setSetCallback(new Action1<V>() {
            @Override
            public void call(V value) {
                task.set(value);
            }
        });

        source.setExceptionCallback(new Action1<Throwable>() {
            @Override
            public void call(Throwable value) {
                task.setException(value);
            }
        });

        return task;
    }

    /**
     * Creates a {@code Task} from the specified data.
     *
     * @param <V> The result type returned by the Task's {@code get} methods.
     * @param data the data
     * @return the task
     */
    static <V> Task<V> fromResult(RxJavaPlugin plugin, final V data)
    {
        return fromResult(plugin, data, "");
    }

    /**
     * Creates a {@code Task} from the specified data.
     *
     * @param <V> The result type returned by the Task's {@code get} methods.
     * @param data the data
     * @param id the id
     * @return the task
     */
    static <V> Task<V> fromResult(RxJavaPlugin plugin, final V data, String id)
    {
        Task<V> task = new Task<V>(plugin, new Callable<V>()
        {
            @Override
            public V call() throws Exception
            {
                return data;
            }
        }, id);

        task.set(data);

        return task;
    }

    /**
     * Creates a {@code Task} from the specified {@link Throwable}.
     *
     * @param <V> The result type returned by the Task's {@code get} methods.
     * @param t the t
     * @return the task
     */
    static <V> Task<V> fromException(RxJavaPlugin plugin, final Throwable t)
    {
        return fromException(plugin, t, "");
    }

    /**
     * Creates a {@code Task} from the specified {@link Throwable}.
     *
     * @param <V> The result type returned by the Task's {@code get} methods.
     * @param t the t
     * @param id the id
     * @return the task
     */
    static <V> Task<V> fromException(RxJavaPlugin plugin, final Throwable t, String id)
    {
        Task<V> task = new Task<V>(plugin, new Callable<V>()
        {
            @Override
            public V call() throws Exception
            {
                return null;
            }
        }, id);

        task.setException(t);

        return task;
    }

    /**
     * Waits for the completion of all specified tasks.
     *
     * @param <VResult> the generic type
     * @param tasks the tasks to wait for
     * @return the task
     */
    static <VResult> Task<List<Task<VResult>>> whenAll(RxJavaPlugin plugin, final Task<VResult> ... tasks)
    {
        List<Task<VResult>> list = new ArrayList<Task<VResult>>();
        
        if (tasks != null)
        {
            Collections.addAll(list, tasks);
        }
        
        return whenAll(plugin, list);
    }
    
    /**
     * Waits for the completion of all specified tasks.
     *
     * @param <VResult> the generic type
     * @param tasks the tasks to wait for
     * @return the task
     */
    static <VResult> Task<List<Task<VResult>>> whenAll(RxJavaPlugin plugin, final List<Task<VResult>> tasks)
    {       
        if (tasks == null || tasks.size() == 0)
        {
        	List<Task<VResult>> emptyList = new ArrayList<Task<VResult>>();
            return Task.fromResult(plugin, emptyList);
        }
        
        Task<VResult> task = tasks.get(0);

        for (int i = 1; i < tasks.size(); i++)
        {
            final int finalI = i;
            
            task = task.continueWith(new TaskFunc<VResult, Task<VResult>>()
            {
                @Override
                public Task<VResult> call(Task<VResult> task) throws Exception
                {
                    task.markAsRead();
                    
                    return tasks.get(finalI);
                }
            }).unwrap();
        }
        
        return task.continueWith(new TaskFunc<VResult, List<Task<VResult>>>() {
            @Override
            public List<Task<VResult>> call(Task<VResult> task) throws Exception {
                
                task.markAsRead();
                
                return tasks;
            }
        });
    }

    /**
     * Waits for the completion of any task.
     *
     * @param <VResult> the generic type
     * @param tasks the tasks to wait for
     * @return the task
     */
    static <VResult> Task<VResult> whenAny(RxJavaPlugin plugin, final Task<VResult> ... tasks)
    {
        List<Task<VResult>> list = new ArrayList<Task<VResult>>();

        if (tasks != null)
        {
            for (Task<VResult> task: tasks)
            {
                list.add(task);
            }
        }

        return whenAny(plugin, list);
    }

    /**
     * Waits for the completion of any task.
     *
     * @param <VResult> the generic type
     * @param tasks the tasks to wait for
     * @return the task
     */
    static <VResult> Task<VResult> whenAny(RxJavaPlugin plugin, final List<Task<VResult>> tasks)
    {
        if (tasks == null || tasks.size() == 0)
        {
            return Task.fromResult(plugin, null);
        }

        final Task<VResult> taskResult = new Task<VResult>(plugin, new Callable<VResult>()
            {
                @Override
                public VResult call() throws Exception
                {
                    return null;
                }
            });

        for (Task<VResult> task: tasks)
        {
            task.continueWith(new TaskFunc<VResult, VResult>() {
                @Override
                public VResult call(Task<VResult> task) throws Exception {

                	synchronized (taskResult) {
									
	                    try
	                    {
	                        if (!taskResult.isDone())
	                        {
	                            taskResult._id = task._id;
	                            taskResult.set(task.get());
	                        }
	                    }
	                    catch (Throwable e)
	                    {
	                        taskResult.setException(e);
	                    }
                	}

                    return task.get();
                }
            });
        }

        return taskResult;
    }

    /* (non-Javadoc)
     * @see de.telekom.util.concurrent.ITaskStart#start()
     */
    @Override
    public void start()
    {
        if (_blnIsStarted)
        {
            if (plugin.getTaskFactory().unhandledExceptions != null)
            {
            	TaskException ex = new TaskStartException("The task has already been started.", this);
            	
                plugin.getTaskFactory().unhandledExceptions.call(ex);
            }
        }
        
        _blnIsStarted = true;
        
        _executor.execute(this);
    }
    
    /* (non-Javadoc)
     * @see java.util.concurrent.FutureTask#run()
     */
    @Override
    public void run()
    {
        _blnIsStarted = true;
        
        super.run();
    }
    
    /* (non-Javadoc)
     * @see java.util.concurrent.FutureTask#get()
     */
    @Override
    public T get() throws InterruptedException, ExecutionException
    {
        _blnIsResultRead = true;
        
        return super.get();
    }
    
    /**
     * Gets the raw result from this {@code Task} if this {@code Task} has finished
     * or null if there is no result.
     *
     * @return the raw
     */
    public T getRaw()
    {
    	
    	if (this.isDone())
    	{
    		try {
				return this.get();
			} 
    		catch (Exception e)
			{
			}
    	}
    
    	return null;
    }

    /**
     * Unwraps this {@code Task} if the result type is a {@code Task<Task<T>>}.
     *
     * @return the {@code Task<T>}
     */
    public T unwrap()
    {

        return this.continueWithInternal(new TaskFunc<Task<T>, T>() {
            @Override
            public T call(Task<Task<T>> task) throws ExecutionException, InterruptedException {
                return task.get().get();
            }
        });
    }
    
    /**
     * Unwraps this {@code Task} if the result type is a {@code Task<Task<T>>}.
     *
     * @param executor the executor
     * @return the {@code Task<T>}
     */
    public T unwrap(Executor executor)
    {

        return this.continueWithInternal(new TaskFunc<Task<T>, T>()
        {
            @Override
            public T call(Task<Task<T>> task) throws ExecutionException, InterruptedException
            {
                return task.get().get();
            }
        }, executor);
    }
    
    /**
     * To void.
     *
     * @return the task
     */
    public Task<Void> toVoid()
    {
    	return this.continueWith(new TaskFunc<T, Void>(){

			@Override
			public Void call(Task<T> task) throws Exception {
				task.get();
				return null;
			}});
    }
    
    /**
     * To null.
     *
     * @param <VNew> the generic type
     * @return the task
     */
    public <VNew> Task<VNew> toNull()
    {
    	return this.continueWith(new TaskFunc<T, VNew>(){

			@Override
			public VNew call(Task<T> task) throws Exception {
				
				task.get();
				
                return null;
            }});
    }
    
    /**
     * To object.
     *
     * @return the task
     */
    public Task<Object> toObject()
    {
        return this.continueWith(new TaskFunc<T, Object>(){

            @Override
            public Object call(Task<T> task) throws Exception {
                return task.get();
            }});
    }
    
    /**
     * To type.
     *
     * @param <VNew> the generic type
     * @return the task
     */
    public <VNew> Task<VNew> toType()
    {
        return this.continueWith(new TaskFunc<T, VNew>(){

            @SuppressWarnings("unchecked")
			@Override
            public VNew call(Task<T> task) throws Exception {
                return (VNew) task.get();
            }});
    }
    
    /**
     * To callable task.
     *
     * @return the callable task
     */
    public TaskFunc<Void, Task<T>> toCallableTask()
    {
        return new TaskFunc<Void, Task<T>>()
        {
            @Override
            public Task<T> call(Task<Void> task) throws Exception
            {
                task.get();
                
                return Task.this;
            }
        };
    }

    /**
     * Continue with.
     *
     * @param <VNew> the generic type
     * @param task the task
     * @return the task
     */
    private <VNew> Task<VNew> continueWith(Task<VNew> task)
    {
        boolean blnIsDone;

        synchronized (_newTaskList)
        {
            blnIsDone = _blnIsDone;

            if (!blnIsDone)
            {
                _newTaskList.add(task);
            }
        }

        if (blnIsDone)
        {
            task.start();
        }

        return task;
    }

    /**
     * Defines a {@link TaskFunc} for continuation after this {@code Task} has finished.
     *
     * @param <VNew> the generic type
     * @param taskFunc the callable task for continuation
     * @return the task
     */
    public <VNew> Task<VNew> continueWith(TaskFunc<T, VNew> taskFunc)
    {

        Task<VNew> newTask = new Task<VNew>(plugin, createCallable(this, taskFunc), this._id);

        return continueWith(newTask);
    }

    /**
     * Defines a {@link TaskFunc} for continuation after this {@code Task} has finished.
     *
     * @param <VNew> the generic type
     * @param taskFunc the callable task for continuation
     * @param executor the executor
     * @return the task
     */
    public <VNew> Task<VNew> continueWith(TaskFunc<T, VNew> taskFunc, Executor executor)
    {

        Task<VNew> newTask = new Task<VNew>(plugin, createCallable(this, taskFunc), executor, this._id);

        return continueWith(newTask);
    }
   
    /**
     * Mark as readed.
     */
    private void markAsRead()
    {
        try
        {
            this.get();
        }
        catch (Exception e)
        {
        }
    }
    
    /**
     * Continue with internal.
     *
     * @param taskFunc the callable task
     * @return the v
     */
    @SuppressWarnings("unchecked")
    private T continueWithInternal(TaskFunc<Task<T>, T> taskFunc)
    {
        Task<T> newTask = new Task<T>(plugin, createCallable((Task<Task<T>>) this, taskFunc), this._id);

        return (T) continueWith(newTask);
    }

    /**
     * Continue with internal.
     *
     * @param taskFunc the callable task
     * @param executor the executor
     * @return the v
     */
    @SuppressWarnings("unchecked")
    private T continueWithInternal(TaskFunc<Task<T>, T> taskFunc, Executor executor)
    {
        Task<T> newTask = new Task<T>(plugin, createCallable((Task<Task<T>>) this, taskFunc), executor, this._id);

        return (T) continueWith(newTask);
    }

    /**
     * Creates the callable.
     *
     * @param <VNew> the generic type
     * @param <V> the value type
     * @param task the task
     * @param taskFunc the callable task
     * @return the callable
     */
    private <VNew, V> Callable<VNew> createCallable(final Task<V> task, final TaskFunc<V, VNew> taskFunc)
    {
        return new Callable<VNew>()
        {
            public VNew call() throws Exception
            {
                VNew result = taskFunc.call(task);
                
                if (! task._blnIsResultRead)
                {
                    if (plugin.getTaskFactory().unhandledExceptions != null)
                    {
                    	TaskException ex = new TaskResultException("The task result has not been read within the ContinueWith method.", task);
                    	
                        plugin.getTaskFactory().unhandledExceptions.call(ex);
                    }
                }
                
                return result;
            }
        };
    }

    /**
     * Checks if is faulted.
     *
     * @return true, if is faulted
     */
    public boolean isFaulted()
    {
    	boolean isFaulted = false;
    	
        if (isDone())
        {
            try
            {
                this.get();
            }
            catch (Exception e)
            {
                isFaulted = true;
            } 
        }
        
        return isFaulted;
    }
    
    /* (non-Javadoc)
     * @see java.util.concurrent.FutureTask#done()
     */
    @Override
    protected void done()
    {
        super.done();

        synchronized (_newTaskList)
        {
            _blnIsDone = true;

            for (ITaskStart task : _newTaskList)
            {
                task.start();
            }

            _newTaskList.clear();
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String id = this._id;
        
        if (isNullOrEmpty(this._id))
        {
            id = "<undefined>";
        }
        
        StringBuilder stb = new StringBuilder();
        
        if (isFaulted())
        {
            try
            {
                this.get();        
            }
            catch(Exception e)
            {
                stb.append("Task {status: is faulted, id: ");
                stb.append(id);
                stb.append(", exception: ");
                stb.append(e.toString()); 
                stb.append("}");
            }
        }
        else if (isDone())
        {
            try
            {
                stb.append("Task {status: is done, id: ");
                stb.append(id);
                stb.append(", value: ");
                stb.append(this.get().toString()); 
                stb.append("}");
            }
            catch(Exception e)
            {
            }
        }
        else if (isCancelled())
        {
            stb.append("Task {status: is cancelled, id: ");
            stb.append(id);
            stb.append("}");
        }
        else
        {
            stb.append("Task {status: is running, id: ");
            stb.append(id);
            stb.append("}");
        }
        
        return stb.toString();
    }

    private static boolean isNullOrEmpty(String string) {

        return (string == null || string.length() == 0);

    }
}
