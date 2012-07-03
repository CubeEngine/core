package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.persistence.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.util.Validate;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Phillip Schichtel
 */
public class ModuleLoader
{
    private final Map<Module, ModuleClassLoader> classLoaders;
    private final File directory;

    public ModuleLoader(File dir)
    {
        Validate.isDir(dir, "The give dir is no dir!");

        this.classLoaders = new HashMap<Module, ModuleClassLoader>();
        this.directory = dir;
    }

    public Module loadModule(String name)
    {
        return this.loadModule(new File(this.directory, name + FileExtentionFilter.JAR.getExtention()));
    }

    public Module loadModule(File file)
    {
        Validate.fileExists(file, "The file must exist!");
        return null;
    }

    public List<Module> loadModules()
    {
        List<Module> modules = new ArrayList<Module>();
        for (File file : this.directory.listFiles((FileFilter)FileExtentionFilter.JAR))
        {
            try
            {
                modules.add(this.loadModule(file));
            }
            catch (Throwable t)
            {
                // TODO log failure
            }
        }
        return modules;
    }

    public Class<?> getClazz(ModuleClassLoader classLoader, String name)
    {
        return null;
    }
}
