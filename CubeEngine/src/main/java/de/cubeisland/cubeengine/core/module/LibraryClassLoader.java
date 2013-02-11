package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.CubeEngine;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;

/**
 * This is a global ClassLoader that should be used to load classes from libraries.
 */
public class LibraryClassLoader extends URLClassLoader
{
    public LibraryClassLoader(ClassLoader parent)
    {
        super(new URL[0], parent);
    }

    @Override
    public void addURL(URL url)
    {
        super.addURL(url);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException
    {
        return super.findClass(name);
    }

    public void shutdown()
    {
        try
        {
            Method method = this.getClass().getMethod("close");
            method.setAccessible(true);
            method.invoke(this);
        }
        catch (Exception ignored)
        {
            CubeEngine.getLogger().log(WARNING, "Failed to close the library class loader");
        }
    }
}
