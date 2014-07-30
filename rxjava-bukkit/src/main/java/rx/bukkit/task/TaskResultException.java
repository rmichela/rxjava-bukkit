/*
 *  Copyright (c) 2014 Andree Hagelstein, Maik Schulze, Deutsche Telekom AG. All Rights Reserved.
 *  
 *  Filename: TaskResultException.java
 */
package rx.bukkit.task;

public class TaskResultException extends TaskException
{
    /*
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public TaskResultException(String message, ITask task) {
        super(message, task);
    }
    
    @Override
    public String toString() {
    	
    	if (getTask() == null)
    	{
    		return String.format("TaskResultException -- Message: %1$s", getMessage());
    	}
    	else
    	{
    		return String.format("TaskResultException -- Message: %1$s; Task: %2$s", getMessage(), getTask().toString());
    	}
    }
}
