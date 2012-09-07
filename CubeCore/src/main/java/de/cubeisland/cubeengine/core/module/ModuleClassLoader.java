package de.cubeisland.cubeengine.core.module;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

/**
 *
 * @author Phillip Schichtel
 */
public class ModuleClassLoader extends URLClassLoader
{
    private final ModuleLoader moduleLoader;
    private final Map<String, Class<?>> classMap;
    private ModuleInfo moduleInfo;

    public ModuleClassLoader(ModuleLoader moduleLoader, ModuleInfo info, ClassLoader parent) throws MalformedURLException
    {
        super(new URL[]
            {
                info.getFile().toURI().toURL()
            }, parent);
        this.moduleLoader = moduleLoader;
        this.classMap = new HashMap<String, Class<?>>();
        this.moduleInfo = info;
    }

    public ModuleInfo getModuleInfo()
    {
        return this.moduleInfo;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException
    {
        return this.findClass(name, true);
    }
/*
    @Override
    public InputStream getResourceAsStream(String name)//TODO get the correct Resource for the module (currently taking from CubeCore.jar)
    {
        //this all does not work:
        try
        {
            return moduleLoader.getClazz(moduleInfo, moduleInfo.getName()+".class").getResourceAsStream(name);
            //JarFile jFile = new JarFile(this.moduleInfo.getFile());
            //Perhaps get the main Class from the Module
            
            //return this.findResource(name).openConnection().getInputStream();
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Resource not found!", ex);
        }
    }

*/
    protected Class<?> findClass(String name, boolean global) throws ClassNotFoundException
    {
        Class<?> clazz = this.classMap.get(name);

        if (clazz == null)
        {
            try
            {
                clazz = super.findClass(name);
            }
            catch (ClassNotFoundException e)
            {
            }

            if (clazz == null && global)
            {
                //TODO STACKOVERFLOW when Class not found
                clazz = this.moduleLoader.getClazz(this.moduleInfo, name);
            }

            if (clazz == null)
            {
                throw new ClassNotFoundException(name);
            }
            this.classMap.put(name, clazz);
        }

        return clazz;
    }
}