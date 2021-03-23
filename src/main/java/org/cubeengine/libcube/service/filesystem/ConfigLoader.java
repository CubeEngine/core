/*
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
package org.cubeengine.libcube.service.filesystem;

import java.nio.file.Path;
import org.cubeengine.reflect.ReflectedFile;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.plugin.PluginContainer;

public class ConfigLoader
{
    private final FileManager fm;
    private final Reflector reflector;
    private final PluginContainer plugin;

    public ConfigLoader(FileManager fm, Reflector reflector, PluginContainer plugin) {

        this.fm = fm;
        this.reflector = reflector;
        this.plugin = plugin;
    }

    public <T extends ReflectedFile<?, ?, ?>> T loadConfig(Class<T> clazz, String name)
    {
        return loadConfig(fm, reflector, plugin, clazz, name);
    }

    public <T extends ReflectedFile<?, ?, ?>> T loadConfig(Class<T> clazz)
    {
        return loadConfig(fm, reflector, plugin, clazz);
    }


    public static <T extends ReflectedFile<?, ?, ?>> T loadConfig(FileManager fm, Reflector reflector, PluginContainer plugin, Class<T> clazz, String name)
    {
        T config = reflector.create(clazz);
        Path path = fm.getModulePath(plugin);
        final String fileName = name + "." + config.getCodec().getExtension();
        config.setFile(path.resolve(fileName).toFile());
        if (config.reload(true))
        {
            plugin.getLogger().info("Saved new configuration file! {}", fileName);
        }
        return config;
    }


    public static <T extends ReflectedFile<?, ?, ?>> T loadConfig(FileManager fm, Reflector reflector, PluginContainer plugin, Class<T> clazz)
    {
        return loadConfig(fm, reflector, plugin, clazz, "config");
    }
}
