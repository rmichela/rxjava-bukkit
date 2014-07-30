/*
 *  Copyright (c) 2014 Andree Hagelstein, Maik Schulze, Deutsche Telekom AG. All Rights Reserved.
 *  
 *  Filename: TaskStartException.java
 */
package rx.bukkit.task;

public class TaskStartException extends TaskException
{
    /*
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public TaskStartException(String message, ITask task) {
        super(message, task);
    }
}
