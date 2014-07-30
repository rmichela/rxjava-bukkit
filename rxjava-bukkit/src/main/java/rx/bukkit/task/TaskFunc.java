/*
 *  Copyright (c) 2014 Andree Hagelstein, Maik Schulze, Deutsche Telekom AG. All Rights Reserved.
 *  
 *  Filename: CallableTask.java
 */
package rx.bukkit.task;

import rx.functions.Func1;

/**
 * The {@code TaskFunc} defines the interface for implementing callback functions
 * for the {@link Task}. Implementors define a single method {@code call}.
 *
 * @param <R> the result type of this callback function
 * @param <T1> the value type
 */
public abstract class TaskFunc<T1, R> {

    /**
     * Call.
     *
     * @param task the task
     * @return the result of this callback function
     * @throws Exception the exception
     */
    public abstract R call(Task<T1> task) throws Exception;

    public static <T1, R> TaskFunc<T1, R> fromFunc(final Func1<T1, R> func) {
        return new TaskFunc<T1, R>() {
            @Override
            public R call(Task<T1> task) throws Exception {
                return func.call(task.get());
            }
        };
    }
}
