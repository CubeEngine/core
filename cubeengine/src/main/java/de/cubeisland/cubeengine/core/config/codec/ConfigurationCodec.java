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
package de.cubeisland.cubeengine.core.config.codec;

import java.io.File;
import java.io.InputStream;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.InvalidConfigurationException;
import de.cubeisland.cubeengine.core.config.annotations.Revision;
import de.cubeisland.cubeengine.core.config.annotations.Updater;
import de.cubeisland.cubeengine.core.config.node.IntNode;
import de.cubeisland.cubeengine.core.config.node.MapNode;

/**
 * This abstract Codec can be implemented to read and write configurations.
 */
public abstract class ConfigurationCodec<Container extends CodecContainer,Config extends Configuration>
{
    protected final String PATH_SEPARATOR = ":";

    /**
     * Loads in the given configuration using the InputStream
     *
     * @param config the config to load
     * @param is the InputStream to load from
     */
    public void load(Config config, InputStream is) throws InstantiationException, IllegalAccessException
    {
        CodecContainer container = this.createCodecContainer();
        container.fillFromInputStream(is);
        Revision revisionAnnotation = config.getClass().getAnnotation(Revision.class);
        if (revisionAnnotation != null && revisionAnnotation.value() > container.revision)
        {
            if (config.getClass().isAnnotationPresent(Updater.class))
            {
                Updater updater = config.getClass().getAnnotation(Updater.class);
                updater.value().newInstance().update(container.values, container.revision);
            }
        }
        container.dumpIntoFields(config, container.values);
    }

    /**
     * Creates a new CodecContainer for this Codec
     *
     * @return
     */
    protected abstract Container createCodecContainer();

    /**
     * Saves the configuration into given file
     *
     * @param config the configuration to save
     * @param file the file to save into
     */
    public void save(Config config, File file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            CodecContainer container = this.createCodecContainer();
            container.values = MapNode.emptyMap();
            Revision a_revision = config.getClass().getAnnotation(Revision.class);
            if (a_revision != null)
            {
                container.values.setNodeAt("revision", PATH_SEPARATOR, new IntNode(a_revision.value()));
            }
            container.fillFromFields(config, container.values);
            container.saveIntoFile(config, file);
        }
        catch (Exception ex)
        {
            throw new InvalidConfigurationException("Error while saving Configuration!", ex);
        }
    }
    /**
     * Returns the FileExtension as String
     *
     * @return the fileExtension
     */
    public abstract String getExtension();

    /**
     * Converts the inputStream into a readable Object
     * @param container the container to fill with values
     * @param is the InputStream
     */
    public abstract void loadFromInputStream(CodecContainer container, InputStream is);
}
