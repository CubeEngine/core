package de.cubeisland.cubeengine.core.util;

import java.lang.reflect.Method;

/**
 * This class is a wrapper for a method.
 */
public class Callback
{
    private final Object holder;
    private final Method method;

    public Callback(Object holder, Method method)
    {
        method.setAccessible(true);
        this.holder = holder;
        this.method = method;
    }

    public static Callback createCallback(Object holder, String methodName, Class... argTyps)
    {
        try
        {
            return new Callback(holder, holder.getClass().getDeclaredMethod(methodName, argTyps));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Object call(Object... args)
    {
        try
        {
            return method.invoke(holder, args);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
