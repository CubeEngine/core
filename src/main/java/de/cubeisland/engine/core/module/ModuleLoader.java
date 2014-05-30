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
package de.cubeisland.engine.core.module;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.event.ModuleLoadedEvent;
import de.cubeisland.engine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.engine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.engine.core.module.exception.InvalidModuleException;
import de.cubeisland.engine.core.module.exception.MissingDependencyException;
import de.cubeisland.engine.core.module.exception.ModuleException;
import de.cubeisland.engine.core.module.exception.ModuleLoadException;
import de.cubeisland.engine.core.storage.TableRegistry;
import gnu.trove.set.hash.THashSet;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

/**
 * This class is used to load modules and provide a centralized place for class
 * lookups.
 */
public class ModuleLoader
{
    private final Core core;
    private final ClassLoader parentClassLoader;
    private final LibraryClassLoader libClassLoader;
   private final Map<String, ModuleClassLoader> classLoaders;
    protected final String infoFileName;
    private final Path tempPath;
    private final TableRegistry registry;

    protected ModuleLoader(Core core, ClassLoader parentClassLoader)
    {
        this.core = core;
        this.parentClassLoader = parentClassLoader;
        this.libClassLoader = new LibraryClassLoader(this.getClass().getClassLoader());
        this.classLoaders = new HashMap<>();
        this.infoFileName = "module.yml";
        this.tempPath = core.getFileManager().getTempPath().resolve("modules");
        this.core.getDB().registerTable(TableRegistry.class);
        this.registry = TableRegistry.TABLE_REGISTRY;
        this.registry.setDsl(core.getDB().getDSL());
        try
        {
            Files.createDirectories(this.tempPath);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to create a temporary folder for the modules!", e);
        }
    }

    public Core getCore()
    {
        return core;
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
    public synchronized Module loadModule(Path file) throws ModuleException
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
    public synchronized Module loadModule(ModuleInfo info) throws ModuleException
    {
        final String name = info.getName();

        if (info.getMinimumCoreVersion().isNewerThan(this.core.getVersion()))
        {
            throw new IncompatibleCoreException(name, info.getMinimumCoreVersion(), this.core.getVersion());
        }

        Path tempFile;
        try
        {
            tempFile = this.tempPath.resolve(System.nanoTime() + "_" + info.getPath().getFileName());
            Files.copy(info.getPath(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            throw new ModuleLoadException("Failed to copy the module to the temporary folder.", e);
        }

        ModuleClassLoader classLoader;
        try
        {
            classLoader = new ModuleClassLoader(this, tempFile.toUri().toURL(), info, this.parentClassLoader);
        }
        catch (MalformedURLException e)
        {
            throw new ModuleLoadException("Failed to create the class loader.", e);
        }

        Class<? extends Module> moduleClass;
        try
        {
            moduleClass = Class.forName(info.getMain(), true, classLoader).asSubclass(Module.class);
        }
        catch (ClassNotFoundException | NoClassDefFoundError e)
        {
            throw new InvalidModuleException("Could not find the module main class!", e);
        }

        Constructor<? extends Module> constructor;
        try
        {
            constructor = moduleClass.getConstructor();
            constructor.setAccessible(true);
        }
        catch (NoSuchMethodException e)
        {
            throw new InvalidModuleException("No default constructor found in the module main class!", e);
        }

        Module module;
        try
        {
            module = constructor.newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            throw new InvalidModuleException("Failed to create a new instance of the module main class!", e);
        }

        module.initialize(this.core, info, info.getPath().getParent().resolve(name), this, classLoader);

        try
        {
            module.onLoad();
        }
        catch (Exception | Error e)
        {
            throw new ModuleLoadException("An error occurred during onLoad() !", e);
        }

        this.classLoaders.put(info.getId(), classLoader);

        this.core.getI18n().registerModule(module);
        this.core.getEventManager().fireEvent(new ModuleLoadedEvent(this.core, module));
        this.getCore().getLog().debug("Module {} loaded...", info.getId());
        return module;
    }

    /**
     * This method does some cleanup to be able to completely unload a module
     *
     * @param module the module
     */
    void unloadModule(Module module)
    {
        expectNotNull(module, "The module must not be null!");

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
    public synchronized ModuleInfo loadModuleInfo(Path file) throws InvalidModuleException
    {
        expectNotNull(file, "The file most not be null!");

        if (!Files.exists(file))
        {
            throw new IllegalArgumentException("The file must exist!");
        }
        ModuleInfo info;
        JarFile jarFile = null;
        try
        {
            jarFile = new JarFile(file.toFile());

            JarEntry entry = jarFile.getJarEntry(this.infoFileName);
            if (entry == null)
            {
                throw new InvalidModuleException("The file '" + file + "' does not contain a module.yml!");
            }

            try (InputStream is = jarFile.getInputStream(entry))
            {
                info = new ModuleInfo(file, this.getCore().getConfigFactory().load(ModuleConfig.class, is));
            }
        }
        catch (IOException e)
        {
            throw new InvalidModuleException("File: " + file, e);
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

        Set<String> alreadyChecked = new THashSet<>(this.classLoaders.size() / 2);
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
    public void registerLibraryClassPath(Path file) throws MalformedURLException
    {
        expectNotNull(file, "The file must not be null!");

        this.registerLibraryClassPath(file.toUri().toURL());
    }

    /**
     * Adds an URL to the library class loader
     *
     * @param url the URL to add
     */
    public void registerLibraryClassPath(URL url)
    {
        expectNotNull(url, "The url must not be null!");

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

    TableRegistry getRegistry()
    {
        return this.registry;
    }
}
