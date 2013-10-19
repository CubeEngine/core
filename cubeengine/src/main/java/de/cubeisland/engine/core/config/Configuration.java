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
package de.cubeisland.engine.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.config.codec.ConfigurationCodec;
import de.cubeisland.engine.core.module.Module;

/**
 * This abstract class represents a configuration.
 */
public abstract class Configuration<Codec extends ConfigurationCodec>
{
    public final Codec codec;
    protected Path file;

    public Configuration()
    {
        this.codec = getCodec(this.getClass());
    }

    /**
     * Tries to get the codec of a configuration implementation.
     *
     * @param clazz the clazz of the configuration
     * @param <C> the CodecType
     * @param <Config> the ConfigType
     * @return the codec
     *
     * @throws InvalidConfigurationException if no codec was defined through the GenericType
     */
    @SuppressWarnings("unchecked cast")
    private static <C extends ConfigurationCodec, Config extends Configuration> C getCodec(Class<Config> clazz)
    {
        Type genericSuperclass = clazz.getGenericSuperclass(); // Get genegeric superclass
        Class<C> codecClass = null;
        try
        {
            if (genericSuperclass.equals(Configuration.class))
            {
                // superclass is this class -> No Codec set as GenericType
                throw new InvalidConfigurationException("Configuration has no codec set! A configuration needs to have a coded defined in its GenericType");
            }
            else if (genericSuperclass instanceof ParameterizedType) // check if genericsuperclass is ParamereizedType
            {
                codecClass = (Class<C>)((ParameterizedType)genericSuperclass).getActualTypeArguments()[0]; // Get Type
                return codecClass.newInstance(); // Create Instance
            }
            // else lookup next superclass
        }
        catch (ClassCastException e) // Somehow the configuration has a GenericType that is not a Codec
        {
            if (!(genericSuperclass instanceof Class))
            {
                throw new IllegalStateException("Something went wrong!", e);
            }
        }
        catch (ReflectiveOperationException ex)
        {
            throw new InvalidConfigurationException("Could not instantiate the codec! " + codecClass, ex);
        }
        return getCodec((Class<? extends Configuration>)genericSuperclass); // Lookup next superclass
    }

    /**
     * Saves this configuration in a file for given Path
     *
     * @param target the Path to the file to save into
     */
    public final void save(Path target)
    {
        if (target == null)
        {
            throw new IllegalArgumentException("A configuration cannot be saved without a valid file!");
        }
        this.codec.save(this, target);
        this.onSaved(target);
    }

    /**
     * Reloads the configuration from file
     * <p>This will only work if the file of the configuration got set previously (usually through loading from file)
     */
    public void reload()
    {
        this.reload(false);
    }

    /**
     * Reloads the configuration from file
     * <p>This will only work if the file of the configuration got set previously (usually through loading from file)
     *
     * @param save true if the configuration should be saved after loading
     */
    public void reload(boolean save)
    {
        if (this.file == null)
        {
            throw new IllegalArgumentException("The file must not be null in order to load the configuration!");
        }
        try (InputStream is = new FileInputStream(this.file.toFile()))
        {
            this.loadFrom(is);
        }
        catch (FileNotFoundException e)
        {
            if (save)
            {
                CubeEngine.getLog().info("Could not find {} creating new file...", file.toString());
            }
            else
            {
                CubeEngine.getLog().warn("Could not find {} to load from!", file.toString());
            }
        }
        catch (Exception e)
        {
            CubeEngine.getLog().error("Failed to load the configuration for {}", this.file);
            CubeEngine.getLog().debug(e.getLocalizedMessage(), e);
        }
        if (save)
        {
            this.save();
        }
    }

    /**
     * Loads the configuration using the given InputStream
     *
     * @param is the InputStream to load from
     */
    public void loadFrom(InputStream is)
    {
        assert is != null : "You hae to provide a InputStream to load from";
        this.codec.load(this, is); //load config in maps -> updates -> sets fields
        this.onLoaded(file);
    }

    /**
     * Saves the configuration to the set file.
     */
    public final void save()
    {
        this.save(this.file);
    }

    /**
     * Returns the Codec
     *
     * @return the ConfigurationCodec defined in the GenericType of the Configuration
     */
    public final Codec getCodec()
    {
        return this.codec;
    }

    /**
     * Sets the path to load from
     *
     * @param path the path the configuration will load from
     */
    public final void setPath(Path path)
    {
        assert path != null: "The file must not be null!";
        this.file = path;
    }

    /**
     * Returns the path this config will be saved to and loaded from by default
     *
     * @return the path of this config
     */
    public final Path getPath()
    {
        return this.file;
    }

    /**
     * This method gets called right after the configuration got loaded.
     */
    public void onLoaded(Path loadedFrom)
    {}

    /**
     * This method gets called right after the configuration get saved.
     */
    public void onSaved(Path savedTo)
    {}

    /**
     * Returns the lines to be added in front of the Configuration.
     * <p>not every codec may use this
     *
     * @return the head
     */
    public String[] head()
    {
        return null;
    }

    /**
     * Returns the lines to be added at the end of the Configuration.
     * <p>not every codec may use this
     *
     * @return the head
     */
    public String[] tail()
    {
        return null;
    }

    /**
     * Creates an instance of this given configuration-class.
     * <p>The configuration has to have the default Constructor for this to work!
     *
     * @param clazz the configurations class
     * @param <T> The Type of the returned configuration
     * @return the created configuration
     */
    public static <T extends Configuration> T create(Class<T> clazz)
    {
        try
        {
            return clazz.newInstance();
        }
        catch (Exception e)
        {
            throw new InvalidConfigurationException("Failed to create an instance of " + clazz.getName(), e);
        }
    }

    /**
     * Loads the configuration from given path and optionally saves it afterwards
     *
     * @param clazz the configurations class
     * @param path the path to load from and save to
     * @param save whether to save the configuration or not
     * @return the loaded Configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, Path path, boolean save)
    {
        return load(clazz, path.toFile(), save);
    }

    /**
     * Loads the configuration from given path and saves it afterwards
     *
     * @param clazz the configurations class
     * @param path the path to load from and save to
     * @return the loaded Configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, Path path)
    {
        return load(clazz, path, true);
    }

    /**
     * Loads the configuration from given file and optionally saves it afterwards
     *
     * @param clazz the configurations class
     * @param file the file to load from and save to
     * @param save whether to save the configuration or not
     * @return the loaded Configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, File file, boolean save)
    {
        T config = create(clazz); // loading
        config.file = file.toPath(); // IMPORTANT TO SET BEFORE LOADING!
        config.reload(save);
        return config;
    }

    /**
     * Loads the configuration from given file and saves it afterwards
     *
     * @param clazz the configurations class
     * @param file the file to load from and save to
     * @return the loaded Configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, File file)
    {
        return load(clazz, file, true);
    }

    /**
     * Loads and saves from config.{@link ConfigurationCodec#getExtension()} in the module folder
     *
     * @param clazz the configurations class
     * @param module the module
     * @return the loaded configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, Module module)
    {
        T config = create(clazz);
        config.file = module.getFolder().resolve("config." + config.codec.getExtension());
        config.reload(true);
        return config;
    }

    /**
     * Loads the configuration from the InputStream
     *
     * @param clazz the configurations class
     * @param is the InputStream to load from
     * @return the loaded configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, InputStream is)
    {
        T config = create(clazz);
        config.loadFrom(is);
        return config;
    }
}
