package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Codec;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.codec.JsonCodec;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.codec.YamlCodec;
import de.cubeisland.cubeengine.core.util.Validate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Faithcaio
 * @author Phillip Schichtel
 */
public abstract class Configuration
{
    private static final Map<String, ConfigurationCodec> codecs = new HashMap<String, ConfigurationCodec>();
    protected static final Logger logger = CubeEngine.getLogger();
    protected ConfigurationCodec codec = null;
    protected File file;

    static
    {
        registerCodec("yml", new YamlCodec());
        registerCodec("json", new JsonCodec());
    }

    /**
     * Registers a ConfigurationCodec for given extension
     *
     * @param extension the extension
     * @param codec the codec
     */
    public static void registerCodec(String extension, ConfigurationCodec codec)
    {
        codecs.put(extension, codec);
    }

    /**
     * Saves the Configuration to given file
     */
    public void saveConfiguration()
    {
        this.codec.save(this, this.file);
    }

    /**
     * Gets the Codec for given FileExtension
     *
     * @param fileExtension
     * @return the Codec
     * @throws IllegalStateException if no Codec is found given FileExtension
     */
    public static ConfigurationCodec resolveCodec(String fileExtension)
    {
        ConfigurationCodec codec = codecs.get(fileExtension);
        if (codec == null)
        {
            throw new IllegalStateException("FileExtension ." + fileExtension + " cannot be used for Configurations!");
        }
        return codec;
    }

    /**
     * Loads and returns the loaded Configuration from File
     *
     * @param file the configurationfile
     * @param clazz the configuration
     * @return the loaded configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, File file)
    {
        Validate.notNull(file, "The file must not be null!");
        if (file == null)
        {
            return null;
        }
        InputStream is = null;
        try
        {
            is = new FileInputStream(file);
        }
        catch (FileNotFoundException ex)
        {
            logger.log(Level.INFO, "{0} not found! Creating new config...", file.getName());
        }
        T config = load(is, clazz); //loading config from InputSream or Default
        config.file = file;
        config.saveConfiguration();
        return config;
    }

    /**
     * Loads and returns the loaded Configuration from InputStream
     *
     * @param is the Inputstream to load the codec from
     * @param clazz the Configuration to use
     * @return the loaded Configuration
     */
    public static <T extends Configuration> T load(InputStream is, Class<T> clazz)
    {
        try
        {
            Codec type = clazz.getAnnotation(Codec.class);
            if (type == null)
            {
                throw new IllegalStateException("Configuration Type undefined!");
            }
            T config = clazz.newInstance();
            config.setCodec(type.value());
            if (is != null)
            {
                config.codec.load(config, is); //load config in maps -> updates -> sets fields
            }
            config.onLoaded();
            return config;
        }
        catch (Throwable t)
        {
            logger.log(Level.SEVERE, "Error while loading a Configuration!", t);
            return null;
        }
    }

    /**
     * Returns the loaded Configuration
     *
     * @param module the module to load the configuration from
     * @param clazz the configuration
     * @return the loaded configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, Module module)
    {
        Codec type = clazz.getAnnotation(Codec.class);
        if (type == null)
        {
            //ConfigType undefined
            return null;
        }
        return load(clazz, new File(module.getFolder(), module.getName().toLowerCase() + "." + type.value()));
    }

    public void setCodec(String fileExtension)
    {
        this.codec = resolveCodec(fileExtension);
    }

    /**
     * Sets the file to load from
     *
     * @param file
     */
    public void setFile(File file)
    {
        Validate.notNull(file, "The file must not be null!");
        this.file = file;
    }

    /**
     * Is used after config is loaded
     */
    public void onLoaded()
    {
    }
}
