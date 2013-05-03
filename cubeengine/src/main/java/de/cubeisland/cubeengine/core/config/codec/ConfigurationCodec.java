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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

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
    protected String COMMENT_PREFIX;
    protected String OFFSET;
    protected String LINE_BREAK;
    protected String QUOTE;
    protected final String PATH_SEPARATOR = ":";
    protected boolean first;

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
        container.dumpIntoFields(config, container.values);
    }

    protected abstract Container createCodecContainer();

    /**
     * Returns the offset as String
     *
     * @param offset the offset
     * @return the offset
     */
    protected String offset(int offset)
    {
        StringBuilder off = new StringBuilder("");
        for (int i = 0; i < offset; ++i)
        {
            off.append(OFFSET);
        }
        return off.toString();
    }

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
     * Serializes the values in the map
     *
     * @param writer the Output to write into
     * @param container the codeccontainer
     * @param values the values
     */
    public abstract void convertMap(OutputStreamWriter writer, Container container, MapNode values) throws IOException;


    /**
     * Builds a the comment for given path
     *
     * @param path the path
     * @param off the current offset
     * @return the comment
     */
    public abstract String buildComment(CodecContainer container, String path, int off);

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
