package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Comment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.MapComment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.MapComments;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Option;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Revision;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Updater;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Faithcaio
 */
public abstract class ConfigurationCodec
{
    public String COMMENT_PREFIX;
    public String SPACES;
    public String LINEBREAK;
    public String QUOTE;
    protected LinkedHashMap<String, Object> values;
    protected HashMap<String, String> comments;
    protected boolean first;
    protected Integer revision = null;

    public ConfigurationCodec()
    {
        this.comments = new HashMap<String, String>();
        this.values = new LinkedHashMap<String, Object>();
    }

    /**
     * Loads the Configuration from a InputStream
     *
     * @param is the InputStream
     * @throws IOException
     */
    public void load(Configuration config, InputStream is) throws IOException
    {
        Logger logger = CubeEngine.getLogger();
        if (is == null)
        {
            return;
        }
        InputStreamReader reader = new InputStreamReader(is);
        StringBuilder builder = new StringBuilder();
        BufferedReader input = new BufferedReader(reader);
        try
        {
            String line;
            line = input.readLine();
            if (line != null)
            {
                if (!line.startsWith("#Revision:"))//Detect Revision
                {
                    builder.append(line).append(LINEBREAK);
                }
                else
                {
                    try
                    {
                        int rev = Integer.parseInt(line.substring(line.indexOf(" ")));
                        this.revision = rev;
                    }
                    catch (NumberFormatException ex)
                    {
                    }
                }
                while ((line = input.readLine()) != null)
                {
                    builder.append(line);
                    builder.append(LINEBREAK);
                }
            }
        }
        finally
        {
            input.close();
        }
        loadFromString(builder.toString());
        //Config loaded into Maps...
        //Check for Updating:
        Class<? extends Configuration> clazz = config.getClass();
        Revision revis = clazz.getAnnotation(Revision.class);
        if (revis != null && this.revision != null)
        {
            if (revis.value() > this.revision)
            {
                logger.log(Level.INFO, "Updating Configuration from Revision {0} to {1}", new Object[]
                    {
                        this.revision, revis.value()
                    });
                this.updateConfig(clazz, this.revision);
            }
        }
        //Write loaded maps into Fields
        for (Field field : clazz.getFields())
        {
            try
            {
                if (field.isAnnotationPresent(Option.class))
                {
                    String path = field.getAnnotation(Option.class).value();
                    //Get savedValue or default
                    Object configElem = this.get(path);
                    if (configElem == null)
                    {
                        //Set defaultValue if no value saved
                        this.set(path, Configuration.convertFrom(field, field.get(config)));
                    }
                    else
                    {
                        //Set new Field Value
                        field.set(config, Configuration.convertTo(config, field, configElem));
                    }

                    if (field.isAnnotationPresent(Comment.class))
                    {
                        this.addComment(path, field.getAnnotation(Comment.class).value());
                    }
                }
            }
            catch (IllegalAccessException ex)
            {
                logger.severe("Error while loading a Configuration-Element!");
            }
        }
        this.clear(); //Clear loaded Maps
    }

    private void updateConfig(Class<? extends Configuration> clazz, int fromRevision)
    {
        Updater annotation = clazz.getClass().getAnnotation(Updater.class);
        if (annotation != null)
        {
            Class<? extends ConfigurationUpdater> updaterClass = annotation.value();
            ConfigurationUpdater updater;
            try
            {
                updater = updaterClass.newInstance();
                this.values = updater.update(this.values, fromRevision);
            }
            catch (Exception ex)
            {
            }
        }
    }

    /**
     * Converts the inputString into ConfigurationMaps
     *
     * @param contents
     */
    public abstract void loadFromString(String contents);

    public void save(Configuration config, File file)
    {
        Logger logger = CubeEngine.getLogger();
        try
        {
            Class<? extends Configuration> clazz = config.getClass();
            if (file == null)
            {
                throw new IllegalStateException("Tried to save config without File.");
            }
            if (clazz.isAnnotationPresent(MapComments.class))
            {
                MapComment[] mapcomments = clazz.getAnnotation(MapComments.class).value();
                for (MapComment comment : mapcomments)
                {
                    this.addComment(comment.path(), comment.text());
                }
            }
            for (Field field : clazz.getFields())
            {
                this.saveField(field, config);
            }
            Revision a_revision = clazz.getAnnotation(Revision.class);
            if (a_revision != null)
            {
                this.revision = a_revision.value();
            }
            this.save(file);
            this.clear();
        }
        catch (IOException ex)
        {
            logger.severe("Error while saving a Configuration-File!");
        }
        catch (IllegalAccessException ex)
        {
            logger.severe("Error while saving a Configuration-Element!");
        }
    }

    public void saveField(Field field, Configuration config) throws IllegalAccessException
    {
        if (field.isAnnotationPresent(Option.class))
        {
            String path = field.getAnnotation(Option.class).value();
            if (field.isAnnotationPresent(Comment.class))
            {
                this.addComment(path, field.getAnnotation(Comment.class).value());
            }
            this.set(path, Configuration.convertFrom(field, field.get(config)));
        }
    }

    /**
     * Saves the configuration to a File
     *
     * @param file the File
     * @throws IOException
     */
    public void save(File file) throws IOException
    {
        if (file == null)
        {
            return;
        }
        String data = this.convertConfig();
        FileWriter writer = new FileWriter(file);
        try
        {
            writer.write(data);
        }
        finally
        {
            writer.close();
        }
    }

    /**
     * Converts the Configuration into a String for saving
     *
     * @return the config as String
     */
    public String convertConfig()
    {
        StringBuilder sb = new StringBuilder();
        first = true;
        sb.append(this.revision());
        sb.append(this.head());
        sb.append(this.convertMap("", this.values, 0));
        sb.append(this.tail());
        return sb.toString();
    }

    /**
     * Converts a whole Section/Map into String for saving
     *
     * @param path the current path
     * @param values the values saved in the Section/Map
     * @param off the offset
     * @return the Section/Map as String
     */
    public abstract String convertMap(String path, Map<String, Object> values, int off);

    /**
     * Converts a Value into String for saving
     *
     * @param path the current path
     * @param value the value saved at given path
     * @param off the offset
     * @return the Value as String
     */
    public abstract String convertValue(String path, Object value, int off);

    /**
     * Builds a Comment for the given path
     *
     * @param path the path
     * @return the comment for path
     */
    public abstract String buildComment(String path, int off);

    /**
     * Gets the offset as String
     *
     * @param offset the offset
     * @return the offset as String
     */
    protected String offset(int offset)
    {
        StringBuilder off = new StringBuilder("");
        for (int i = 0; i < offset; ++i)
        {
            off.append(SPACES);
        }
        return off.toString();
    }

    /**
     * Gets the value saved under the path
     *
     * @param path the path
     * @return the value or null if no value saved
     */
    public Object get(String path)
    {
        if (path.contains("."))
        {
            return this.get(this.getSubPath(path), (Map<String, Object>)this.values.get(this.getBasePath(path)));
        }
        else
        {
            return this.values.get(path);
        }
    }

    /**
     * Gets the value saved under this path in given section
     *
     * @param path the path
     * @param section the section
     * @return the value saved under path in section
     */
    private Object get(String path, Map<String, Object> section)
    {
        if (section == null)
        {
            return null;
        }
        if (path.contains("."))
        {
            return this.get(this.getSubPath(path), (Map<String, Object>)section.get(this.getBasePath(path)));
        }
        else
        {
            return section.get(path);
        }
    }

    /**
     * Sets a value at a specified path
     *
     * @param path the path
     * @param value the value to set
     */
    public void set(String path, Object value)
    {
        if (path.contains("."))
        {
            Map<String, Object> subsection = this.createSection(this.values, this.getBasePath(path));
            this.set(subsection, this.getSubPath(path), value);
        }
        else
        {
            values.put(path, value);
        }
    }

    /**
     * Sets the value at the path in the specified section
     *
     * @param section the section
     * @param path the path
     * @param value the value to set
     */
    private void set(Map<String, Object> section, String path, Object value)
    {
        if (path.contains("."))
        {
            Map<String, Object> subsection = this.createSection(section, this.getBasePath(path));
            this.set(subsection, this.getSubPath(path), value);
        }
        else
        {
            section.put(path, value);
        }
    }

    /**
     * Gets or create the section with the path in the basesection
     *
     * @param basesection the basesection
     * @param path the path of the section
     * @return the section
     */
    private Map<String, Object> createSection(Map<String, Object> basesection, String path)
    {
        Map<String, Object> subsection = (Map<String, Object>)basesection.get(path);
        if (subsection == null)
        {
            subsection = new LinkedHashMap<String, Object>();
            basesection.put(path, subsection);
        }
        return subsection;
    }

    /**
     * Splits up the path and returns the basepath
     *
     * @param path the path
     * @return the basepath
     */
    public String getBasePath(String path)
    {
        return path.substring(0, path.indexOf("."));
    }

    /**
     * Splits up the path and returns the subpath
     *
     * @param path the path
     * @return the subpath
     */
    public String getSubPath(String path)
    {
        return path.substring(path.indexOf(".") + 1);
    }

    /**
     * Splits up the path and returns the key
     *
     * @param path the path
     * @return the key
     */
    public String getSubKey(String path)
    {
        return path.substring(path.lastIndexOf(".") + 1);
    }

    /**
     * Adds a Comment to the specified path
     *
     * @param path the path
     * @param comment the comment
     */
    public void addComment(String path, String comment)
    {
        this.comments.put(path, comment);
    }

    /**
     * Resets the value & comments Map
     */
    public void clear()
    {
        this.comments.clear();
        this.values.clear();
    }

    public String revision()
    {
        if (revision != null)
        {
            return "#Revision: " + this.revision;
        }
        return "";
    }

    public void setRevision(int revision)
    {
        this.revision = revision;
    }

    /**
     * This is inserted in front of the String to safe
     *
     * @return the head
     */
    public String head()
    {
        return "";
    }

    /**
     * This is appended to the String to safe
     *
     * @return the tail
     */
    public String tail()
    {
        return "";
    }
}
