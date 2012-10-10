package de.cubeisland.cubeengine.core.config;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.codec.YamlCodec;
import de.cubeisland.cubeengine.core.module.Module;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.Validate;
import org.yaml.snakeyaml.reader.ReaderException;

/**
 * This abstract class represents a configuration.
 */
public abstract class Configuration
{
    private static final Map<String, ConfigurationCodec> codecs = new HashMap<String, ConfigurationCodec>();
    protected static final Logger logger = CubeEngine.getLogger();
    protected ConfigurationCodec codec = null;
    protected File file;

    static
    {
        registerCodec(new YamlCodec(), "yml", "yaml");
    }

    /**
     * Registers a ConfigurationCodec for given extension
     *
     * @param extension the extension
     * @param codec the codec
     */
    public static void registerCodec(ConfigurationCodec codec, String... extensions)
    {
        for (String extension : extensions)
        {
            codecs.put(extension, codec);
        }
    }

    /**
     * Saves this configuration into given File
     * 
     * @param targetFile the File to save to
     */
    public final void save(File targetFile)
    {
        if (this.codec == null)
        {
            throw new IllegalStateException("A configuration cannot be saved without a valid codec!");
        }
        if (this.file == null)
        {
            throw new IllegalStateException("A configuration cannot be saved without a valid file!");
        }        
        this.codec.save(this, targetFile);
        this.onSaved(targetFile);
    }

    /**
     * Saves the Configuration.
     */
    public final void save()
    {
        this.save(this.file);
    }

    /**
     * Gets the Codec for given FileExtension
     *
     * @param fileExtension
     * @return the Codec
     * @throws IllegalStateException if no Codec is found for given FileExtension
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
        return load(clazz, file, true);
    }

    /**
     * Loads and returns the loaded Configuration from File
     *
     * @param file the configurationfile
     * @param clazz the configuration
     * @param save whether to instantly save the config after it was loaded
     * @return the loaded configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, File file, boolean save)
    {
        if (file == null)
        {
            return null;
        }
        InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(file);
        }
        catch (FileNotFoundException e)
        {
            logger.log(Level.INFO, file.getName() + " not found! Creating new config...");
        }
        T config = load(clazz, inputStream); //loading config from InputSream or Default
        if (inputStream != null)
        {
            try
            {
                inputStream.close();
            }
            catch (IOException ex)
            {
            }
        }
        config.file = file;
        if (save)
        {
            config.save();
        }
        return config;
    }

    /**
     * Loads and returns the loaded Configuration from InputStream
     *
     * @param is the Inputstream to load the codec from
     * @param clazz the Configuration to use
     * @return the loaded Configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, InputStream is)
    {
        Codec type = clazz.getAnnotation(Codec.class);
        if (type == null)
        {
            throw new InvalidConfigurationException("Configuration Type undefined!");
        }
        try
        {
            T config = clazz.newInstance();
            config.setCodec(type.value());
            if (is != null)
            {
                config.codec.load(config, is); //load config in maps -> updates -> sets fields
            }
            config.onLoaded();
            return config;
        }
        catch (Exception e)
        {
            if (e instanceof ReaderException)
            {
                throw new InvalidConfigurationException("Failed to parse the YAML configuration. Try encoding it as UTF-8 or validate on yamllint.com", e);
            }
            throw new InvalidConfigurationException("Error while loading a Configuration!", e);
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
        return load(clazz, new File(module.getFolder(), module.getName().toLowerCase(Locale.ENGLISH) + "." + type.value()));
    }

    /**
     * Sets the Codec for this Confguration
     * 
     * @param fileExtension 
   
     */
    public void setCodec(String fileExtension)
    {
        this.setCodec(resolveCodec(fileExtension));
    }

    /**
     * Sets the Codec for this Configuration
     * 
     * @param codec 
     */
    public void setCodec(ConfigurationCodec codec)
    {
        this.codec = codec;
    }

    /**
     * Returns the extension for the current Codec
     * 
     * @return the fileExtension
     */
    public String getCodecExtension()
    {
        return this.codec.getExtension();
    }
    
    /**
     * Returns the current Codec
     * 
     * @return the ConfigurationCodec
     */
    public ConfigurationCodec getCodec()
    {
        return this.codec;
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
     * Returns the file this config will be saved to
     *
     * @return the file this config will be saved to
     */
    public File getFile()
    {
        return this.file;
    }

    /**
     * This method is called right after the configuration got loaded.
     */
    public void onLoaded()
    {
    }

    /**
     * This method gets called right after the configration get saved.
     */
    public void onSaved(File file)
    {
    }

    /**
     * Returns the lines to be added in front of the Configuration.
     * 
     * @return the head
     */
    public String[] head()
    {
        return null;
    }

    /**
     * Returns the lines to be added at the end of the Configuration.
     * 
     * @return the head
     */
    public String[] tail()
    {
        return null;
    }
}