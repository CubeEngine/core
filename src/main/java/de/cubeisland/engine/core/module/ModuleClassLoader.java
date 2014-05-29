/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.module;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.ArgumentReader;

/**
 * This is the ClassLoader used by modules
 */
public class ModuleClassLoader extends URLClassLoader
{
    private final ModuleLoader moduleLoader;
    private final Map<String, Class> classMap;
    private final ModuleInfo moduleInfo;

    public ModuleClassLoader(ModuleLoader moduleLoader, URL jarURL, ModuleInfo info, ClassLoader parent) throws MalformedURLException
    {
        super(new URL[] {
            jarURL
        }, parent);
        this.moduleLoader = moduleLoader;
        this.classMap = new ConcurrentHashMap<>();
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

    public ModuleInfo getModuleInfo()
    {
        return moduleInfo;
    }

    void shutdown()
    {
        Class clazz;
        final Iterator<Map.Entry<String, Class>> it = this.classMap.entrySet().iterator();
        while (it.hasNext())
        {
            clazz = it.next().getValue();
            this.moduleLoader.getCore().getConfigFactory().getDefaultConverterManager().removeConverter(clazz);
            ArgumentReader.removeReader(clazz);
            it.remove();
        }

        try
        {
            this.close();
        }
        catch (IOException ex)
        {
            CubeEngine.getLog().warn(ex, "Failed to close the class loader of the module '{}'", this.moduleInfo.getName());
        }
    }
}
