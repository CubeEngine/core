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

import java.io.Reader;
import java.nio.file.Path;

import de.cubeisland.engine.core.config.InvalidConfigurationException;
import de.cubeisland.engine.core.config.MultiConfiguration;
import de.cubeisland.engine.core.config.annotations.Revision;
import de.cubeisland.engine.core.config.annotations.Updater;
import de.cubeisland.engine.core.config.node.IntNode;
import de.cubeisland.engine.core.config.node.MapNode;

/**
 * This abstract Codec can be implemented to read and write configurations that allow child-configs
 */
public abstract class MultiConfigurationCodec<Container extends MultiCodecContainer, Config extends MultiConfiguration> extends ConfigurationCodec<Container, Config>
{
    public void saveChildConfig(Config parentConfig, Config config, Path file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            MultiCodecContainer container = this.createCodecContainer();
            Revision a_revision = config.getClass().getAnnotation(Revision.class);
            container.values = MapNode.emptyMap();
            if (a_revision != null)
            {
                container.values.setNodeAt("revision", PATH_SEPARATOR, new IntNode(a_revision.value()));
            }
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
     * @param reader the InputStream to load from
     */
    public void loadChildConfig(MultiConfiguration config, Reader reader) throws InstantiationException, IllegalAccessException
    {
        MultiCodecContainer container = new MultiCodecContainer(this);
        container.fillFromReader(reader);
        Revision a_revision = config.getClass().getAnnotation(Revision.class);
        if (a_revision != null)
        {
            if (config.getClass().isAnnotationPresent(Updater.class))
            {
                if (a_revision.value() > container.revision)
                {
                    Updater updater = config.getClass().getAnnotation(Updater.class);
                    updater.value().newInstance().update(container.values, container.revision);
                }
            }
        }
        container.dumpIntoFields(config, container.values, config.getParent());
    }
}
