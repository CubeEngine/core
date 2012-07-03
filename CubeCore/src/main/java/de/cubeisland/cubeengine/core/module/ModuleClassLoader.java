package de.cubeisland.cubeengine.core.module;

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
    private Module module;

    public ModuleClassLoader(ModuleLoader moduleLoader, URL url, ClassLoader parent)
    {
        super(new URL[] {url}, parent);
        this.moduleLoader = moduleLoader;
        this.classMap = new HashMap<String, Class<?>>();
        this.module = null;
    }

    public void setModule(Module module)
    {
        if (this.module == null)
        {
            this.module = module;
        }
        else
        {
            throw new IllegalStateException("The module was already set!");
        }
    }

    public Module getModule()
    {
        return this.module;
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
            catch (Exception e)
            {}

            if (clazz == null)
            {
                clazz = this.moduleLoader.getClazz(this, name);
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
