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

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.config.InvalidConfigurationException;
import de.cubeisland.engine.core.config.annotations.Revision;
import de.cubeisland.engine.core.config.annotations.Updater;
import de.cubeisland.engine.core.config.node.IntNode;
import de.cubeisland.engine.core.config.node.MapNode;
import de.cubeisland.engine.core.config.node.Node;

/**
 * This abstract Codec can be implemented to read and write configurations.
 */
public abstract class ConfigurationCodec<Config extends Configuration>
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
     * @param reader the InputStream to load from
     */
    public void load(Config config, Reader reader) throws InstantiationException, IllegalAccessException
    {
        CodecContainer container = new CodecContainer<ConfigurationCodec>(this);
        container.fillFromReader(reader);
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
    public void save(Config config, Path file)
    {
        try
        {
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            CodecContainer container = new CodecContainer<ConfigurationCodec>(this);
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
     * @param container the codec-container
     * @param values the values at given path
     * @param off the current offset
     * @param inCollection
     * @return  the serialized value
     */
    public abstract String convertMap(CodecContainer container, MapNode values, int off, boolean inCollection);

    /**
     * Serializes a single value
     *
     * @param container the codec-container
     * @param value the value at given path
     * @param off the current offset
     * @param inCollection
     * @return
     */
    public abstract String convertValue(CodecContainer container, Node value, int off, boolean inCollection);

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
    public abstract void loadFromReader(CodecContainer container, Reader is);
}
