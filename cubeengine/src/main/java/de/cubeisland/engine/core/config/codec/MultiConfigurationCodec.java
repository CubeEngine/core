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
package de.cubeisland.engine.core.config.codec;

import java.io.InputStream;
import java.nio.file.Path;

import de.cubeisland.engine.core.config.InvalidConfigurationException;
import de.cubeisland.engine.core.config.MultiConfiguration;
import de.cubeisland.engine.core.config.node.MapNode;

/**
 * This abstract Codec can be implemented to read and write configurations that allow child-configs
 */
public abstract class MultiConfigurationCodec<Container extends MultiCodecContainer,
    Config extends MultiConfiguration>
    extends ConfigurationCodec<Container, Config>
{
    public void saveChildConfig(Config parentConfig, Config config, Path file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            MultiCodecContainer container = this.createContainer();
            container.values = MapNode.emptyMap();
            container.fillFromFields(parentConfig, config, container.values);
            container.saveIntoFile(config, file);
        }
        catch (Exception ex)
        {
            throw new InvalidConfigurationException("Error while saving Configuration!", ex);
        }
    }

    /**
     * Loads in the given configuration using the InputStream
     *
     * @param config the config to load
     * @param is the InputStream to load from
     */
    public void loadChildConfig(MultiConfiguration config, InputStream is) throws InstantiationException, IllegalAccessException
    {
        MultiCodecContainer container = this.createContainer();
        container.loadFromInputStream(is);
        container.dumpIntoFields(config, container.values, config.getParent());
    }
}
