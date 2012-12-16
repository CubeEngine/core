package de.cubeisland.cubeengine.core.config;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.codec.YamlCodec;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import org.apache.commons.lang.Validate;
import org.yaml.snakeyaml.reader.ReaderException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * This abstract class represents a configuration.
 */
public abstract class Configuration
{
    private static final Map<String, ConfigurationCodec> codecs = new HashMap<String, ConfigurationCodec>();
    protected Class<? extends Configuration> configurationClass;
    protected static final Logger logger = CubeEngine.getLogger();
    protected ConfigurationCodec codec = null;
    protected File file;
    protected Configuration parent = null;

    static
    {
        registerCodec(new YamlCodec(), "yml", "yaml");
    }

    /**
     * Registers a ConfigurationCodec for given extension
     *
     * @param codec     the codec
     * @param extensions the extensions
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
        if (targetFile == null)
        {
            throw new IllegalStateException("A configuration cannot be saved without a valid file!");
        }
        this.codec.save(this, targetFile);
        this.onSaved(targetFile);
    }

    public final void saveChild()
    {
        if (this.codec == null)
        {
            throw new IllegalStateException("A configuration cannot be saved without a valid codec!");
        }
        if (this.file == null)
        {
            throw new IllegalStateException("A configuration cannot be saved without a valid file!");
        }
        this.codec.saveChildConfig(this.parent, this, this.file);
        this.onSaved(this.file);
    }
    
    public void load()
    {
        if (this.file == null)
        {
            throw new IllegalStateException("The file must not be null in order to load the configuration!");
        }
        try
        {
            FileInputStream is = new FileInputStream(this.file);
            this.codec.load(this, is);
        }
        catch (Exception e)
        {
            CubeEngine.getLogger().log(SEVERE, "Failed to load the configuration " + this.file.getPath(), e);
        }
    }

    public <T extends Configuration> T loadChild(File sourceFile) //and save
    {
        Configuration childConfig;
        try
        {
            childConfig = this.configurationClass.newInstance();
            childConfig.codec = this.codec;
            childConfig.file = sourceFile;
            childConfig.parent = this;
            try
            {
                FileInputStream is = new FileInputStream(sourceFile);
                childConfig.codec.load(childConfig, is);
            }
            catch (FileNotFoundException ignored) // not found load from parent / save child
            {
                childConfig.codec.load(childConfig, null);
            }
            childConfig.saveChild();
            return (T)childConfig;
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Could not load ChildConfig!", ex);
        }
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
        if (fileExtension == null)
        {
            return null;
        }
        ConfigurationCodec codec = codecs.get(fileExtension);
        if (codec == null)
        {
            throw new IllegalStateException("FileExtension ." + fileExtension + " cannot be used for Configurations!");
        }
        return codec;
    }

    public static <T extends Configuration> T createInstance(Class<T> clazz)
    {
        String codec = null;
        Codec codecAnnotation = clazz.getAnnotation(Codec.class);
        if (codecAnnotation != null)
        {
            codec = codecAnnotation.value();
        }
        try
        {
            T instance = clazz.newInstance();
            instance.setCodec(codec);
            return instance;
        }
        catch (Exception e)
        {
            throw new InvalidConfigurationException("Failed to create an instance of " + clazz.getName(), e);
        }
    }

    /**
     * Loads and returns the loaded Configuration from File
     *
     * @param file  the configurationfile
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
     * @param file  the configurationfile
     * @param clazz the configuration
     * @param save  whether to instantly save the config after it was loaded
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
            logger.log(LogLevel.NOTICE, "{0} not found! Creating new config...", file.getName());
        }
        T config = load(clazz, inputStream); //loading config from InputSream or Default

        if (inputStream != null)
        {
            try
            {
                inputStream.close();
            }
            catch (IOException ignored)
            {}
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
     * @param is    the Inputstream to load the codec from
     * @param clazz the Configuration to use
     * @return the loaded Configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, InputStream is)
    {
        T config = createInstance(clazz);
        if (config.codec == null)
        {
            throw new InvalidConfigurationException("No codec specified for " + clazz.getName());
        }
        try
        {
            if (is != null)
            {
                config.codec.load(config, is); //load config in maps -> updates -> sets fields
            }
            config.onLoaded();
            config.configurationClass = clazz;
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
     * @param clazz  the configuration
     * @return the loaded configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, Module module)
    {
        Codec codecAnnotation = clazz.getAnnotation(Codec.class);
        if (codecAnnotation == null)
        {
            throw new InvalidConfigurationException("No codec specified for " + clazz.getName());
        }
        return load(clazz, new File(module.getFolder(), module.getName().toLowerCase(Locale.ENGLISH) + "." + codecAnnotation.value()));
    }

    /**
     * Sets the Codec for this Confguration
     *
     * @param fileExtension *
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
    {}

    /**
     * This method gets called right after the configration get saved.
     */
    public void onSaved(File file)
    {}

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
