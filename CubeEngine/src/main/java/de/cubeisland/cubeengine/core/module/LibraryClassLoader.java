package de.cubeisland.cubeengine.core.module;

import java.net.URL;
import java.net.URLClassLoader;

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
}
