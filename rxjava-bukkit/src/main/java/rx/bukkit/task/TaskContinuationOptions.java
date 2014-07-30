/*
 *  Copyright (c) 2014 Andree Hagelstein, Maik Schulze, Deutsche Telekom AG. All Rights Reserved.
 *  
 *  Filename: TaskContinuationOptions.java
 */
package rx.bukkit.task;

public enum TaskContinuationOptions
{
    None,
    NotOnFaulted,
    NotOnCanceled,
    OnlyOnFaulted,
    OnlyOnCanceled
}
