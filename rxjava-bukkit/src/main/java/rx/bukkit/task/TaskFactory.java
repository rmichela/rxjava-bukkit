/*
 *  Copyright (c) 2014 Andree Hagelstein, Maik Schulze, Deutsche Telekom AG. All Rights Reserved.
 *  
 *  Filename: TaskFactory.java
 */
package rx.bukkit.task;

import rx.bukkit.RxJavaPlugin;
import rx.bukkit.scheduler.BukkitRxScheduler;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.logging.Level;

/**
 * A factory for creating {@link Task} objects.
 */
public class TaskFactory {
    private RxJavaPlugin plugin;

    public TaskFactory(RxJavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** The default executor service for the {@link Task}. */
	public Executor defaultExecutor = new BukkitRxScheduler(plugin, BukkitRxScheduler.ConcurrencyMode.SYNCHRONOUS);
	
	/** The background executor service. */
	public Executor backgroundExecutor = new BukkitRxScheduler(plugin, BukkitRxScheduler.ConcurrencyMode.ASYNCHRONOUS);

	/** The callback function to retrieve exceptions from the {@link Task}. */
	public Action1<TaskException> unhandledExceptions = new Action1<TaskException>() {
        @Override
        public void call(TaskException e) {
            plugin.getLogger().log(Level.SEVERE, "Unhandled exception in Task " + e.getTask().getId(), e);
        }
    };

	/**
	 * Creates and starts a new {@code Task}.
	 *
	 * @param <V> the value type
	 * @param callable the callable
	 * @return the task
	 */
	public <V> Task<V> startNew(Callable<V> callable) {
		
	    return startNew(callable, "");
	}

	/**
	 * Creates and starts a new {@code Task}.
	 *
	 * @param <V> the value type
	 * @param callable the callable
	 * @param id the id
	 * @return the task
	 */
	public <V> Task<V> startNew(Callable<V> callable, String id) {
        
	    Task<V> task = new Task<V>(plugin, callable, id);
        task.start();

        return task;
    }
	
	/**
	 * Creates and starts a new {@code Task}.
	 *
	 * @param <V> the value type
	 * @param callable the callable
	 * @param executor the executor
	 * @return the task
	 */
	public <V> Task<V> startNew(Callable<V> callable, Executor executor) {
		
		return startNew(callable, executor, "");
	}
	
	/**
	 * Creates and starts a new {@code Task}.
	 *
	 * @param <V> the value type
	 * @param callable the callable
	 * @param executor the executor
	 * @param id the id
	 * @return the task
	 */
	public <V> Task<V> startNew(Callable<V> callable, Executor executor, String id) {
        
        Task<V> task = new Task<V>(plugin, callable, executor, id);
        task.start();

        return task;
    }

    /**
     * Creates a {@code Task} from the specified {@link TaskCompletionSource}.
     *
     * @param <V> The result type returned by the Task's {@code get} methods.
     * @param source the source to create the task from
     * @return the task
     */
    public <V> Task<V> fromSource(final TaskCompletionSource<V> source)
    {
        return Task.fromSource(plugin, source);
    }

    /**
     * Creates a {@code Task} from the specified data.
     *
     * @param <V> The result type returned by the Task's {@code get} methods.
     * @param data the data
     * @return the task
     */
    public <V> Task<V> fromResult(final V data)
    {
        return fromResult(data, "");
    }

    /**
     * Creates a {@code Task} from the specified data.
     *
     * @param <V> The result type returned by the Task's {@code get} methods.
     * @param data the data
     * @param id the id
     * @return the task
     */
    public <V> Task<V> fromResult(final V data, String id)
    {
        return Task.fromResult(plugin, data, id);
    }

    /**
     * Creates a {@code Task} from the specified {@link Throwable}.
     *
     * @param <V> The result type returned by the Task's {@code get} methods.
     * @param t the t
     * @return the task
     */
    public <V> Task<V> fromException(final Throwable t)
    {
        return fromException(t, "");
    }

    /**
     * Creates a {@code Task} from the specified {@link Throwable}.
     *
     * @param <V> The result type returned by the Task's {@code get} methods.
     * @param t the t
     * @param id the id
     * @return the task
     */
    public <V> Task<V> fromException(final Throwable t, String id)
    {
        return Task.fromException(plugin, t, id);
    }

    /**
     * Waits for the completion of all specified tasks.
     *
     * @param <VResult> the generic type
     * @param tasks the tasks to wait for
     * @return the task
     */
    public <VResult> Task<List<Task<VResult>>> whenAll(final Task<VResult> ... tasks)
    {
        List<Task<VResult>> list = new ArrayList<Task<VResult>>();

        if (tasks != null)
        {
            Collections.addAll(list, tasks);
        }

        return whenAll(list);
    }

    /**
     * Waits for the completion of all specified tasks.
     *
     * @param <VResult> the generic type
     * @param tasks the tasks to wait for
     * @return the task
     */
    public <VResult> Task<List<Task<VResult>>> whenAll(final List<Task<VResult>> tasks)
    {
        return Task.whenAll(plugin, tasks);
    }

    /**
     * Waits for the completion of any task.
     *
     * @param <VResult> the generic type
     * @param tasks the tasks to wait for
     * @return the task
     */
    public <VResult> Task<VResult> whenAny(final Task<VResult> ... tasks)
    {
        List<Task<VResult>> list = new ArrayList<Task<VResult>>();

        if (tasks != null)
        {
            for (Task<VResult> task: tasks)
            {
                list.add(task);
            }
        }

        return whenAny(list);
    }

    /**
     * Waits for the completion of any task.
     *
     * @param <VResult> the generic type
     * @param tasks the tasks to wait for
     * @return the task
     */
    public <VResult> Task<VResult> whenAny(final List<Task<VResult>> tasks)
    {
        return Task.whenAny(plugin, tasks);
    }
}
