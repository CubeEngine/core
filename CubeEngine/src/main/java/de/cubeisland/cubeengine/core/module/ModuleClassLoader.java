package de.cubeisland.cubeengine.core.module;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.util.convert.Convert;

import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;

/**
 * This is the ClassLoader used by modules
 */
public class ModuleClassLoader extends URLClassLoader
{
    private ModuleLoader moduleLoader;
    private Map<String, Class> classMap;
    private ModuleInfo moduleInfo;

    public ModuleClassLoader(ModuleLoader moduleLoader, URL jarURL, ModuleInfo info, ClassLoader parent) throws MalformedURLException
    {
        super(new URL[] {
            jarURL
        }, parent);
        this.moduleLoader = moduleLoader;
        this.classMap = new ConcurrentHashMap<String, Class>();
        this.moduleInfo = info;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException
    {
        return this.findClass(name, true);
    }

    protected Class findClass(String name, boolean global) throws ClassNotFoundException
    {
        Class<?> clazz = this.classMap.get(name);

        if (clazz == null)
        {
            try
            {
                clazz = super.findClass(name);
            }
            catch (ClassNotFoundException ignored)
            {}

            if (clazz == null && global)
            {
                clazz = this.moduleLoader.getClazz(this.moduleInfo, name);
                if (clazz == null)
                {
                    clazz = this.moduleLoader.getLibraryClass(name);
                }
            }

            if (clazz == null)
            {
                throw new ClassNotFoundException(name);
            }
            this.classMap.put(name, clazz);
        }

        return clazz;
    }

    // This method got overridden to first search through the current ClassLoader
    @Override
    public URL getResource(String name)
    {
        URL url = findResource(name);
        if (url == null)
        {
            return super.getResource(name);
        }

        return url;
    }

    void shutdown()
    {
        Class clazz;
        final Iterator<Map.Entry<String, Class>> iter = this.classMap.entrySet().iterator();
        while (iter.hasNext())
        {
            clazz = iter.next().getValue();
            Convert.unregisterConverter(clazz);
            ArgumentReader.unregisterReader(clazz);
            this.moduleLoader.getCore().getCommandManager().removeCommandFactory(clazz);
            iter.remove();
        }
        this.moduleInfo = null;
        this.moduleLoader = null;

        try
        {
            Method method = this.getClass().getMethod("close");
            method.setAccessible(true);
            method.invoke(this);
        }
        catch (Exception ignored)
        {
            CubeEngine.getLog().log(WARNING, "Failed to close the class loader of the module ''{0}''", this.moduleInfo.getName());
        }
    }
}
