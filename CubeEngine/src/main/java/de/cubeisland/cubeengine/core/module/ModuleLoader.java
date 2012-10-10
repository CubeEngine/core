package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.cubeengine.core.module.exception.InvalidModuleException;
import de.cubeisland.cubeengine.core.module.exception.MissingDependencyException;
import de.cubeisland.cubeengine.core.util.log.ModuleLogger;
import gnu.trove.set.hash.THashSet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.lang.Validate;

public class ModuleLoader
{
    private final Core core;
    private final LibraryClassLoader libClassLoader;
    private final Map<String, ModuleClassLoader> classLoaders;
    protected String classPrefix = "Cube";
    protected final String infoFileName = "module.yml";

    protected ModuleLoader(Core core)
    {
        this.core = core;
        this.libClassLoader = new LibraryClassLoader(this.getClass().getClassLoader());
        this.classLoaders = new HashMap<String, ModuleClassLoader>();
    }

    public synchronized Module loadModule(File file) throws InvalidModuleException, MissingDependencyException, IncompatibleDependencyException, IncompatibleCoreException
    {
        return this.loadModule(this.loadModuleInfo(file));
    }

    public synchronized Module loadModule(ModuleInfo info) throws InvalidModuleException, MissingDependencyException, IncompatibleDependencyException, IncompatibleCoreException
    {
        final String name = info.getName();

        if (info.getMinimumCoreRevision() > Core.REVISION)
        {
            throw new IncompatibleCoreException(name, info.getMinimumCoreRevision());
        }

        try
        {
            ModuleClassLoader classLoader = new ModuleClassLoader(this, info, getClass().getClassLoader());
            Class<? extends Module> moduleClass = Class.forName(info.getMain(), true, classLoader).asSubclass(Module.class);
            Module module = moduleClass.getConstructor().newInstance();

            module.initialize(
                this.core,
                info,
                new File(info.getFile().getParentFile(), name),
                new ModuleLogger(this.core, info),
                this,
                classLoader);

            for (Field field : moduleClass.getDeclaredFields())
            {
                if (field.isAnnotationPresent(From.class))
                {
                    if (Configuration.class.isAssignableFrom(field.getType()))
                    {
                        field.setAccessible(true);
                        field.set(module, Configuration.load(field.getType().asSubclass(Configuration.class),
                            new File(module.getFolder(), field.getAnnotation(From.class).value() + "." + field.getType().getAnnotation(Codec.class).value())));
                    }
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

    public void unloadModule(Module module)
    {
        Validate.notNull(module, "The module must not be null!");

        this.classLoaders.remove(module.getId());
    }

    public synchronized ModuleInfo loadModuleInfo(File file) throws InvalidModuleException
    {
        Validate.notNull(file, "The file most not be null!");
        if (!file.exists())
        {
            throw new IllegalArgumentException("The file must exist!");
        }
        if (!FileExtentionFilter.JAR.accept(file))
        {
            throw new IllegalArgumentException("The file doesn't seem to be a JAR file");
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
            info = new ModuleInfo(file, Configuration.load(ModuleConfiguration.class, configStream));
            configStream.close();
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
                catch (IOException e)
                {
                }
            }
        }
        return info;
    }

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
        catch (ClassNotFoundException e)
        {
        }

        if (clazz != null)
        {
            return clazz;
        }

        Set<String> alreadyChecked = new THashSet<String>(this.classLoaders.size() / 2);
        alreadyChecked.add(info.getName());

        for (String dep : info.getSoftDependencies().keySet())
        {
            alreadyChecked.add(dep);
            try
            {
                clazz = this.classLoaders.get(dep).findClass(name, false);
                if (clazz != null)
                {
                    return clazz;
                }
            }
            catch (ClassNotFoundException e)
            {
            }
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
            catch (ClassNotFoundException e)
            {
            }
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
                {
                }
            }
        }

        return null;
    }
    
    public void registerLibraryClassPath(File file) throws MalformedURLException
    {
        Validate.notNull(file, "The file must not be null!");
        
        this.registerLibraryClassPath(file.toURI().toURL());
    }
    
    public void registerLibraryClassPath(URL url)
    {
        Validate.notNull(url, "The url must not be null!");
        
        this.libClassLoader.addURL(url);
    }

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
}