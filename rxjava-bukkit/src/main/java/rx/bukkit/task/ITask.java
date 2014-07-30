/*
 *  Copyright (c) 2014 Andree Hagelstein, Maik Schulze, Deutsche Telekom AG. All Rights Reserved.
 *  
 *  Filename: ITask.java
 */
package rx.bukkit.task;

import java.util.concurrent.ExecutionException;

/**
 * The {@code ITask} defines the interface to retrieve information from a {@link Task}.
 */
public interface ITask<T>
{
    
    /**
     * To string.
     *
     * @return the string
     */
    String toString();

    /**
     * Gets the task ID.
     * @return the ID
     */
    String getId();
    
    /**
     * Gets the task result.
     *
     * @return the object
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    T get() throws InterruptedException, ExecutionException;
}
