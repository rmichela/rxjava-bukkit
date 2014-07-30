/*
 *  Copyright (c) 2014 Andree Hagelstein, Maik Schulze, Deutsche Telekom AG. All Rights Reserved.
 *  
 *  Filename: TaskException.java
 */
package rx.bukkit.task;

public class TaskException extends Exception
{
    private static final long serialVersionUID = 1L;

    private ITask<?> _task;

    public TaskException(String message, ITask<?> task) {
        super(message);
        _task = task;
    }

    public ITask<?> getTask()
    {
        return _task;
    }
}
