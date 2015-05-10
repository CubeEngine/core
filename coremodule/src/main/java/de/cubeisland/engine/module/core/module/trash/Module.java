/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 * <p/>
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.module.core.module.trash;

import de.cubeisland.engine.module.core.permission.Permission;
import de.cubeisland.engine.reflect.ReflectedFile;


/**
 * Module for CubeEngine.
 */
public abstract class Module
{
    /**
     * Loads and saves from config.{@link de.cubeisland.engine.reflect.codec.FileCodec#getExtension()} in the module folder
     *
     * @param clazz the configurations class
     * @return the loaded configuration
     */
    protected final <T extends ReflectedFile<?, ?, ?>> T loadConfig(Class<T> clazz)
    {
        /* TODO
        T config = this.core.getReflector().create(clazz);
        config.setFile(this.getFolder().resolve("config." + config.getCodec().getExtension()).toFile());
        if (config.reload(true))
        {
            this.getLog().info("Saved new configuration file! config.{}", config.getCodec().getExtension());
        }
        return config;
        */
        return null;
    }
}
