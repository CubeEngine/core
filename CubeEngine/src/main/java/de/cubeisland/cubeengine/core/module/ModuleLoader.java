package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.module.event.ModuleLoadedEvent;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.cubeengine.core.module.exception.InvalidModuleException;
import de.cubeisland.cubeengine.core.module.exception.MissingDependencyException;
import de.cubeisland.cubeengine.core.storage.Registry;
import de.cubeisland.cubeengine.core.logger.ModuleLogger;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang.Validate;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class is used to load modules and provide a centralized place for class
 * lookups.
 */
public class ModuleLoader
{
    private final Core core;
    private final LibraryClassLoader libClassLoader;
    private final Map<String, ModuleClassLoader> classLoaders;
    protected final String infoFileName;
    private final File tempFolder;
    private Registry registry;

    ModuleLoader(Core core)
    {
        this.core = core;
        this.libClassLoader = new LibraryClassLoader(this.getClass().getClassLoader());
        this.classLoaders = new HashMap<String, ModuleClassLoader>();
        this.infoFileName = "module.yml";
        this.tempFolder = new File(core.getFileManager().getTempDir(), "modules");
        this.registry = new Registry(core.getDB());
        if (!this.tempFolder.exists() && !this.tempFolder.mkdir())
        {
            throw new RuntimeException("Failed to create a temporary folder for the modules!");
        }
    }

    /**
     * Loads a module from a file
     *
     * @param file the file to load from
     * @return the loaded module
     *
     * @throws InvalidModuleException if the file is not a valid module
     * @throws MissingDependencyException if the module has missing hard
     * dependencies
     * @throws IncompatibleDependencyException if the module depends on a newer
     * version of a module
     * @throws IncompatibleCoreException if the module depends on a newer core
     * version
     */
    public synchronized Module loadModule(File file) throws InvalidModuleException, MissingDependencyException, IncompatibleDependencyException, IncompatibleCoreException
    {
        return this.loadModule(this.loadModuleInfo(file));
    }

    /**
     * Loads a module from a ModuleInfo instance
     *
     * @param info the module info
     * @return the loaded module
     *
     * @throws InvalidModuleException if the file is not a valid module
     * @throws MissingDependencyException if the module has missing hard
     * dependencies
     * @throws IncompatibleDependencyException if the module depends on a newer
     * version of a module
     * @throws IncompatibleCoreException if the module depends on a newer core
     * version
     */
    @SuppressWarnings("unchecked")
    public synchronized Module loadModule(ModuleInfo info) throws InvalidModuleException, MissingDependencyException, IncompatibleDependencyException, IncompatibleCoreException
    {
        final String name = info.getName();

        if (info.getMinimumCoreRevision() > Core.REVISION)
        {
            throw new IncompatibleCoreException(name, info.getMinimumCoreRevision());
        }

        try
        {
            File tempFile = new File(this.tempFolder, System.nanoTime() + "_" + info.getFile().getName());
            tempFile.delete();
            tempFile.createNewFile();
            FileManager.copyFile(info.getFile(), tempFile);

            ModuleClassLoader classLoader = new ModuleClassLoader(this, tempFile.toURI().toURL(), info, getClass().getClassLoader());
            Class<? extends Module> moduleClass = Class.forName(info.getMain(), true, classLoader).asSubclass(Module.class);
            Module module = moduleClass.getConstructor().newInstance();

            module.initialize(
                    this.core,
                    info,
                    new File(info.getFile().getParentFile(), name),
                    new ModuleLogger(this.core, info),
                    this,
                    classLoader);

            this.core.getEventManager().fireEvent(new ModuleLoadedEvent(this.core, module));

            for (Field field : moduleClass.getDeclaredFields())
            {
                if (Configuration.class.isAssignableFrom(field.getType()) && field.getType().isAnnotationPresent(Codec.class))
                {
                    Class<? extends Configuration> configClass = (Class<? extends Configuration>)field.getType();

                    String filename = null;
                    if (configClass.isAnnotationPresent(DefaultConfig.class))
                    {
                        filename = "config";
                    }
                    else
                    {
                        continue;
                    }
                    field.setAccessible(true);
                    field.set(module, Configuration.load(configClass, new File(module.getFolder(), filename + "." + configClass.getAnnotation(Codec.class).value())));
                }
            }

            module.onLoad();

            this.classLoaders.put(info.getId(), classLoader);
            return module;
        }
        catch (Exception e)
        {
            throw new InvalidModuleException("Module: " + info.getName(), e);
        }
    }

    /**
     * This method does some cleanup to be able to completely unload a module
     *
     * @param module the module
     */
    void unloadModule(Module module)
    {
        Validate.notNull(module, "The module must not be null!");

        ModuleClassLoader classLoader = this.classLoaders.remove(module.getId());
        if (classLoader != null)
        {
            classLoader.shutdown();
        }
    }

    /**
     * Loads a module info from a file
     *
     * @param file the file to load from
     * @return the loaded module info
     *
     * @throws InvalidModuleException if the file is not a valid module
     */
    public synchronized ModuleInfo loadModuleInfo(File file) throws InvalidModuleException
    {
        Validate.notNull(file, "The file most not be null!");

        if (!file.exists())
        {
            throw new IllegalArgumentException("The file must exist!");
        }
        ModuleInfo info;
        JarFile jarFile = null;
        try
        {
            jarFile = new JarFile(file);

            JarEntry entry = jarFile.getJarEntry(this.infoFileName);
            if (entry == null)
            {
                throw new InvalidModuleException("The module '" + file.getPath() + "' does not contain a module.yml!");
            }
            InputStream configStream = jarFile.getInputStream(entry);
            try
            {
                info = new ModuleInfo(file, Configuration.load(ModuleConfig.class, configStream, null));
            }
            finally
            {
                try
                {
                    configStream.close();
                }
                catch (IOException ignored)
                {}
            }
        }
        catch (IOException e)
        {
            throw new InvalidModuleException("File: " + file.getPath(), e);
        }
        finally
        {
            if (jarFile != null)
            {
                try
                {
                    jarFile.close();
                }
                catch (IOException ignored)
                {}
            }
        }
        return info;
    }

    /**
     * Searches all known class loaders for the given class
     *
     * @param info the module info of the module that requested the class
     * @param name the fully qualified class name
     * @return the class or null
     */
    public Class<?> getClazz(ModuleInfo info, String name)
    {
        if (name == null)
        {
            return null;
        }
        Class<?> clazz = null;
        try
        {
            clazz = this.libClassLoader.findClass(name);
        }
        catch (ClassNotFoundException ignored)
        {}

        if (clazz != null)
        {
            return clazz;
        }

        Set<String> alreadyChecked = new THashSet<String>(this.classLoaders.size() / 2);
        alreadyChecked.add(info.getId());

        for (String dep : info.getSoftDependencies().keySet())
        {
            alreadyChecked.add(dep);
            try
            {
                ModuleClassLoader ldr = this.classLoaders.get(dep);
                if (ldr != null)
                {
                    clazz = ldr.findClass(name, false);
                    if (clazz != null)
                    {
                        return clazz;
                    }
                }
            }
            catch (ClassNotFoundException ignore)
            {}
        }

        for (String dep : info.getDependencies().keySet())
        {
            if (alreadyChecked.contains(dep))
            {
                continue;
            }
            alreadyChecked.add(dep);
            try
            {
                clazz = this.classLoaders.get(dep).findClass(name, false);
                if (clazz != null)
                {
                    return clazz;
                }
            }
            catch (ClassNotFoundException ignored)
            {}
        }

        for (String module : this.classLoaders.keySet())
        {
            if (!alreadyChecked.contains(module))
            {
                try
                {
                    clazz = this.classLoaders.get(module).findClass(name, false);
                    if (clazz != null)
                    {
                        return clazz;
                    }
                }
                catch (ClassNotFoundException ignored)
                {}
            }
        }

        return null;
    }

    /**
     * Adds a new file to the library class loader
     *
     * @param file the file to add
     * @throws MalformedURLException if the file is invalid
     */
    public void registerLibraryClassPath(File file) throws MalformedURLException
    {
        Validate.notNull(file, "The file must not be null!");

        this.registerLibraryClassPath(file.toURI().toURL());
    }

    /**
     * Adds an URL to the library class loader
     *
     * @param url the URL to add
     */
    public void registerLibraryClassPath(URL url)
    {
        Validate.notNull(url, "The url must not be null!");

        this.libClassLoader.addURL(url);
    }

    /**
     * Searches a class in the library class loader
     *
     * @param name the fully qualified class name
     * @return the class or null if not found
     */
    public Class<?> getLibraryClass(String name)
    {
        try
        {
            return this.libClassLoader.findClass(name);
        }
        catch (ClassNotFoundException ignored)
        {
            return null;
        }
    }

    void shutdown()
    {
        this.classLoaders.clear();
        this.libClassLoader.shutdown();
    }

    Registry getRegistry()
    {
        return this.registry;
    }
}
