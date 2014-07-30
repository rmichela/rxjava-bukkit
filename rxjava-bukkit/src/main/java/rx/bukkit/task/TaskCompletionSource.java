/*
 *  Copyright (c) 2014 Andree Hagelstein, Maik Schulze, Deutsche Telekom AG. All Rights Reserved.
 *  
 *  Filename: TaskCompletionSource.java
 */
package rx.bukkit.task;

import rx.functions.Action1;

/**
 * The {@code TaskCompletionSource} represents the producer side of a {@link Task}.
 *
 * @param <V> the value type
 */
public class TaskCompletionSource<V> {

    /** The _set callback. */
    private Action1<V> _setCallback;
    
    /** The _exception callback. */
    private Action1<Throwable> _exceptionCallback;
    
    /** The result value. */
    private V resultValue;
    
    /** The result throwable. */
    private Throwable resultThrowable;

    /**
     * Sets the result value of the attached {@link Task}.
     *
     * @param result the result
     */
    public void set(V result) {

        if (_setCallback != null)
        {
        	_setCallback.call(result);
        }
        else
        {
        	resultValue = result;
        }
    }

    /**
     * Sets the result of the attached {@link Task}.
     *
     * @param t the new exception
     */
    public void setException(Throwable t) {

        if (_exceptionCallback != null)
        {
        	_exceptionCallback.call(t);
        }
        else
        {
        	resultThrowable = t;
        }
    }

    /**
     * Sets the set callback.
     *
     * @param callback the callback
     */
    void setSetCallback(Action1<V> callback)
    {
        _setCallback = callback;
        
        if (resultValue != null)
        {
        	_setCallback.call(resultValue);
        }
    }

    /**
     * Sets the exception callback.
     *
     * @param callback the callback
     */
    void setExceptionCallback(Action1<Throwable> callback)
    {
        _exceptionCallback = callback;
        
        if (resultThrowable != null)
        {
        	_exceptionCallback.call(resultThrowable);
        }
    }
}
