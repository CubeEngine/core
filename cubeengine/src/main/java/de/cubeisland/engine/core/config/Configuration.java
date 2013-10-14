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
import java.io.IOException;
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
     * Saves this configuration into given File
     *
     * @param target the File to save to
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
     */
    public void reload()
    {
        if (this.file == null)
        {
            throw new IllegalArgumentException("The file must not be null in order to load the configuration!");
        }
        try
        {
            try (InputStream is = new FileInputStream(this.file.toFile()))
            {
                this.loadFrom(is);
            }
        }
        catch (Exception e)
        {
            CubeEngine.getLog().error("Failed to load the configuration for {}", this.file);
            CubeEngine.getLog().debug(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Loads the configuration using the given InputStream
     *
     * @param is the InputStream to load from
     */
    public void loadFrom(InputStream is)
    {
        if (is != null)
        {
            this.codec.load(this, is); //load config in maps -> updates -> sets fields
        }
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
     * Returns the current Codec
     *
     * @return the ConfigurationCodec
     */
    public final Codec getCodec()
    {
        return this.codec;
    }

    /**
     * Sets the file to load from
     *
     * @param file the file
     */
    public final void setPath(Path file)
    {
        assert file != null: "The file must not be null!";
        this.file = file;
    }

    /**
     * Returns the file this config will be saved to and loaded from by default
     *
     * @return the file of this config
     */
    public final Path getPath()
    {
        return this.file;
    }

    /**
     * This method is called right after the configuration got loaded.
     */
    public void onLoaded(Path loadFrom)
    {}

    /**
     * This method gets called right after the configuration get saved.
     */
    public void onSaved(Path savedTo)
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
     * Creates an instance of this given configuration-class.
     * <p>The configuration has to have the default Constructor for this to work!
     *
     * @param clazz the configurations class
     * @param <T>
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
        try (FileInputStream fis = new FileInputStream(file))
        {
            T config = create(clazz); // loading
            config.file = file.toPath(); // IMPORTANT TO SET BEFORE LOADING!
            config.reload();
            if (save)
            {
                config.save(); // saving
            }
            return config;
        }
        catch (IOException ex)
        {
            CubeEngine.getLog().warn("Could not load configuration from file! " + clazz);
            CubeEngine.getLog().debug(ex.getLocalizedMessage(), ex);
        }
        return null;
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
     * Loads from config.{@link ConfigurationCodec#getExtension()} in the module folder
     *
     * @param clazz the configurations class
     * @param module the module
     * @return the loaded configuration
     */
    public static <T extends Configuration> T load(Class<T> clazz, Module module)
    {
        T config = create(clazz);
        config.file = module.getFolder().resolve("config." + config.codec.getExtension());
        config.reload();
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

    private static <C extends ConfigurationCodec, Config> C getCodec(Class<Config> clazz)
    {
        Type genericSuperclass = clazz.getGenericSuperclass();
        try
        {
            if (genericSuperclass instanceof ParameterizedType)
            {
                Class<C> codecClass = (Class<C>)((ParameterizedType)genericSuperclass).getActualTypeArguments()[0];
                return codecClass.newInstance();
            }
            else if (genericSuperclass.equals(Configuration.class))
            {
                throw new InvalidConfigurationException("Configuration has no codec set!");
            }
            else
            {
                return getCodec((Class<?>)genericSuperclass);
            }
        }
        catch (ClassCastException e)
        {
            if (clazz.getSuperclass().equals(Configuration.class))
            {
                throw new InvalidConfigurationException("Configuration has no codec set!", e);
            }
            else
            {
                return getCodec((Class<?>)genericSuperclass);
            }
        }
        catch (ReflectiveOperationException ex)
        {
            throw new InvalidConfigurationException("Could not instantiate the codec!", ex);
        }
    }
}
