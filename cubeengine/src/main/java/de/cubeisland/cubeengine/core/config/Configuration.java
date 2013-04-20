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
package de.cubeisland.cubeengine.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.codec.ConfigurationCodec;
import de.cubeisland.cubeengine.core.config.codec.YamlCodec;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.module.Module;

import org.apache.commons.lang.Validate;
import org.yaml.snakeyaml.reader.ReaderException;

import static java.util.logging.Level.SEVERE;

/**
 * This abstract class represents a configuration.
 */
public abstract class Configuration<ConfigCodec extends ConfigurationCodec>
{
    private static final Map<String, ConfigurationCodec> codecs = new HashMap<String, ConfigurationCodec>();
    protected final Class<? extends Configuration> configurationClass;
    public final ConfigCodec codec;
    protected File file;

    static
    {
        registerCodec(new YamlCodec(), "yml", "yaml");
    }

    public Configuration()
    {
        this.codec = initCodec();
        this.configurationClass = this.getClass();
    }

    private ConfigCodec initCodec()
    {
        return resolveCodec(findCodec(this.getClass()).value());

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
        if (targetFile == null)
        {
            throw new IllegalArgumentException("A configuration cannot be saved without a valid file!");
        }
        this.codec.save(this, targetFile);
        this.onSaved(targetFile);
    }

    public void reload()
    {
        if (this.file == null)
        {
            throw new IllegalArgumentException("The file must not be null in order to load the configuration!");
        }
        try
        {
            FileInputStream is = new FileInputStream(this.file);
            this.codec.load(this, is);
        }
        catch (Exception e)
        {
            CubeEngine.getLog().log(SEVERE, "Failed to load the configuration " + this.file.getPath(), e);
        }
    }

    /**
     * Saves the configuration to the set file.
     */
    public final void save()
    {
        this.save(this.file);
    }

    /**
     * Returns the current Codec
     *
     * @return the ConfigurationCodec
     */
    public final ConfigCodec getCodec()
    {
        return this.codec;
    }

    /**
     * Sets the file to load from
     *
     * @param file the file
     */
    public final void setFile(File file)
    {
        Validate.notNull(file, "The file must not be null!");
        this.file = file;
    }

    /**
     * Returns the file this config will be saved to and loaded from by default
     *
     * @return the file of this config
     */
    public final File getFile()
    {
        return this.file;
    }

    /**
     * This method is called right after the configuration got loaded.
     */
    public void onLoaded(File loadFrom)
    {}

    /**
     * This method gets called right after the configuration get saved.
     */
    public void onSaved(File savedIn)
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

    /**
     * Gets the Codec for given FileExtension
     *
     * @param fileExtension the file extension
     * @return the Codec
     * @throws IllegalStateException if no Codec is found for given FileExtension
     */
    public static <Codec extends ConfigurationCodec> Codec resolveCodec(String fileExtension)
    {
        if (fileExtension == null)
        {
            return null;
        }
        ConfigurationCodec codec = codecs.get(fileExtension);
        if (codec == null)
        {
            throw new InvalidConfigurationException("No Codec known for the file-extension ." + fileExtension);
        }
        return (Codec) codec;
    }

    /**
     * Creates an instance of this given configuration-class.
     * The configuration has to have the default Constructor for this to work!
     *
     * @param clazz the configurations class
     * @param <T>
     * @return the created configuration
     */
    public static <T extends Configuration> T createInstance(Class<T> clazz) //TODO constructor with params?
    {
        try
        {
            T instance = clazz.newInstance();
            return instance;
        }
        catch (Exception e)
        {
            throw new InvalidConfigurationException("Failed to create an instance of " + clazz.getName(), e);
        }
    }

    /**
     * Returns the loaded Configuration
     *
     * @param clazz the configurations class
     * @param module the module to load the configuration from
     * @param <T>
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
     * Returns the loaded Configuration
     *
     * @param clazz the configurations class
     * @param is the input-stream to load from
     * @param file the corresponding file
     * @param <T>
     * @return the loaded Configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, InputStream is, File file)
    {
        T config = createInstance(clazz);

        config.file = file;
        try
        {
            if (is != null)
            {
                config.codec.load(config, is); //load config in maps -> updates -> sets fields
            }
            config.onLoaded(file);
            return config;
        }
        catch (Exception e)
        {
            if (e instanceof ReaderException)
            {//TODO abstract...
                throw new InvalidConfigurationException("Failed to parse the YAML configuration. Try encoding it as UTF-8 or validate on yamllint.com", e);
            }
            throw new InvalidConfigurationException("Error while loading a Configuration!", e);
        }
    }

    /**
     * Returns the loaded Configuration
     *
     * @param clazz the configurations class
     * @param is the input-stream to load from
     * @param <T>
     * @return the loaded Configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, InputStream is)
    {
        return load(clazz, is, null);
    }

    /**
     * Returns the loaded Configuration
     *
     * @param clazz the configurations class
     * @param file the file to load from
     * @param save whether to save after loading
     * @param <T>
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
            CubeEngine.getLog().log(LogLevel.NOTICE, "{0} not found! Creating new config from default...", file.getName());
        }
        T config = load(clazz, inputStream, file); //loading config from InputSream or Default

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
     *
     * @param clazz the configurations class
     * @param file the file to load from
     * @param <T>
     * @return the loaded configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, File file)
    {
        return load(clazz, file, true);
    }

    public static Codec findCodec(Class<? extends Configuration> clazz)
    {
        Class<? extends Configuration> tmpClass = clazz;
        Codec codecAnnotation = tmpClass.getAnnotation(Codec.class);
        while (codecAnnotation == null && (tmpClass = (Class<? extends Configuration>)tmpClass.getSuperclass()) != Configuration.class)
        {
            codecAnnotation = tmpClass.getAnnotation(Codec.class);
        }
        if (codecAnnotation == null)
        {
            throw new InvalidConfigurationException("Missing codec-annotation for configuration: "+ clazz);
        }
        return codecAnnotation;
    }
}
