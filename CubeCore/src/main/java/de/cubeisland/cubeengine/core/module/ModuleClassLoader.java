package de.cubeisland.cubeengine.core.module;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

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
        super(new URL[] {info.getFile().toURI().toURL()}, parent);
        this.moduleLoader = moduleLoader;
        this.classMap = new HashMap<String, Class<?>>();
        this.moduleInfo = info;
    }

    public ModuleInfo getModuleInfo()
    {
        return this.moduleInfo;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        Class<?> clazz = this.classMap.get(name);
        
        if (clazz == null)
        {
            try
            {
                clazz = super.findClass(name);
            }
            catch (ClassNotFoundException e)
            {}

            if (clazz == null)
            {
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
