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
package de.cubeisland.engine.core.storage;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class DatabaseClassloader extends ClassLoader
{
    Set<ClassLoader> classLoaders = new HashSet<>();

    public void addClassLoader(ClassLoader classLoader)
    {
        this.classLoaders.add(classLoader);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        for (ClassLoader classLoader : classLoaders)
        {
            try
            {
                return classLoader.loadClass(name);
            }
            catch (ClassNotFoundException ignored)
            {}
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public InputStream getResourceAsStream(String name)
    {
        for (ClassLoader classLoader : classLoaders)
        {
            InputStream stream = classLoader.getResourceAsStream(name);
            if (stream != null) return stream;
        }
        return null;
    }

    @Override
    public URL getResource(String name)
    {
        for (ClassLoader classLoader : classLoaders)
        {
            URL resource = classLoader.getResource(name);
            if (resource != null) return resource;
        }
        return null;
    }
}
