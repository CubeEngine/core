package de.cubeisland.cubeengine.core.module;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.cubeisland.cubeengine.core.Core;

import org.apache.commons.lang.Validate;

/**
 * This class provides information about a module.
 */
public class ModuleInfo
{
    private static final char DEP_VERSION_DELIM = '/';
    private final File file;
    private final String main;
    private final String id;
    private final String name;
    private final int revision;
    private final String description;
    private final int minCoreVersion;
    private final boolean providesWorldGenerator;
    private final Map<String, Integer> dependencies;
    private final Map<String, Integer> softDependencies;
    private final Set<String> pluginDependencies;
    private final Set<String> loadAfter;

    ModuleInfo()
    {
        this.file = null;
        this.main = null;
        this.id = CoreModule.ID;
        this.name = CoreModule.NAME;
        this.revision = Core.REVISION;
        this.description = "This is the core meta module.";
        this.minCoreVersion = Core.REVISION;
        this.providesWorldGenerator = false;
        this.dependencies = Collections.emptyMap();
        this.softDependencies = this.dependencies;
        this.pluginDependencies = Collections.emptySet();
        this.loadAfter = this.pluginDependencies;
    }

    private static String nameToId(String name)
    {
        name = name.toLowerCase(Locale.US);
        name = name.replaceAll("[^a-z0-9]", "");
        name = name.replaceAll("^[0-9]]", "");
        return name;
    }

    public ModuleInfo(File file, ModuleConfig config)
    {
        Validate.notNull(config, "The module configuration failed to loaded!");
        Validate.notNull(config.name, "The module doesn't seem to have a name.");

        this.name = config.name.trim();
        Validate.notEmpty(this.name, "The module name seems to be empty.");

        this.file = file;
        this.id = nameToId(config.name);

        if (config.main == null)
        {
            config.main = "de.cubeisland.cubeengine." + this.id + "." + this.id.substring(0, 1).toUpperCase(Locale.US) + this.id.substring(1);
        }
        this.main = config.main;

        this.revision = config.revision;
        this.description = config.description;
        this.minCoreVersion = config.minCoreRevision;
        this.providesWorldGenerator = config.provideWorldGenerator;

        int delimOffset;
        int version;

        this.dependencies = new HashMap<String, Integer>(config.dependencies.size());
        config.dependencies.remove(this.id);
        for (String dep : config.dependencies)
        {
            dep = dep.toLowerCase();

            version = -1;

            delimOffset = dep.indexOf(DEP_VERSION_DELIM);
            if (delimOffset > -1)
            {
                try
                {
                    version = Integer.parseInt(dep.substring(delimOffset + 1));
                }
                catch (NumberFormatException ignored)
                {}
                dep = dep.substring(0, delimOffset);
            }
            this.dependencies.put(dep, version);
        }

        this.softDependencies = new HashMap<String, Integer>(config.softDependencies.size());
        config.softDependencies.remove(this.id);
        for (String dep : config.softDependencies)
        {
            dep = dep.toLowerCase();
            version = -1;

            delimOffset = dep.indexOf(DEP_VERSION_DELIM);
            if (delimOffset > -1)
            {
                try
                {
                    version = Integer.parseInt(dep.substring(delimOffset + 1));
                }
                catch (NumberFormatException ignored)
                {}
                dep = dep.substring(0, delimOffset);
            }
            this.softDependencies.put(dep, version);
        }

        this.pluginDependencies = config.pluginDependencies;
        this.loadAfter = config.loadAfter;
    }

    /**
     * Returns the file the module got loaded from
     *
     * @return the module file
     */
    File getFile()
    {
        return this.file;
    }

    /**
     * Returns the module's main class
     *
     * @return the fully qualified class name
     */
    public String getMain()
    {
        return this.main;
    }

    /**
     * The module id (basicly the lowercased name)
     *
     * @return the module name
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * The module name
     *
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Gets the module's revision
     *
     * @return the revision
     */
    public int getRevision()
    {
        return this.revision;
    }

    /**
     * Gets the modules description
     *
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the minimum core revision this module requires
     *
     * @return the minimum core revision
     */
    public int getMinimumCoreRevision()
    {
        return this.minCoreVersion;
    }

    /**
     * Returns whether the module provides world generators
     * if true, this module is not allowed to depend on other module
     *
     * @return true if the module provides world generators
     */
    public boolean providesWorldGenerator()
    {
        return this.providesWorldGenerator;
    }

    /**
     * Returns a map of the module's dependencies and dependency versions
     *
     * @return the dependencies
     */
    public Map<String, Integer> getDependencies()
    {
        return this.dependencies;
    }

    /**
     * Returns a map of the module's soft dependencies and dependency versions
     *
     * @return the soft dependencies
     */
    public Map<String, Integer> getSoftDependencies()
    {
        return this.softDependencies;
    }

    /**
     * Returns a set of the module's plugin dependencies
     *
     * @return the plugin dependencies
     */
    public Set<String> getPluginDependencies()
    {
        return this.pluginDependencies;
    }

    public Set<String> getLoadAfter()
    {
        return loadAfter;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ModuleInfo))
        {
            return false;
        }

        ModuleInfo that = (ModuleInfo)o;

        if (minCoreVersion != that.minCoreVersion)
        {
            return false;
        }
        if (providesWorldGenerator != that.providesWorldGenerator)
        {
            return false;
        }
        if (revision != that.revision)
        {
            return false;
        }
        if (!dependencies.equals(that.dependencies))
        {
            return false;
        }
        if (!description.equals(that.description))
        {
            return false;
        }
        if (file != null ? !file.equals(that.file) : that.file != null)
        {
            return false;
        }
        if (!id.equals(that.id))
        {
            return false;
        }
        if (!loadAfter.equals(that.loadAfter))
        {
            return false;
        }
        if (main != null ? !main.equals(that.main) : that.main != null)
        {
            return false;
        }
        if (!name.equals(that.name))
        {
            return false;
        }
        if (!pluginDependencies.equals(that.pluginDependencies))
        {
            return false;
        }
        if (!softDependencies.equals(that.softDependencies))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = file != null ? file.hashCode() : 0;
        result = 31 * result + (main != null ? main.hashCode() : 0);
        result = 31 * result + id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + revision;
        result = 31 * result + description.hashCode();
        result = 31 * result + minCoreVersion;
        result = 31 * result + (providesWorldGenerator ? 1 : 0);
        result = 31 * result + dependencies.hashCode();
        result = 31 * result + softDependencies.hashCode();
        result = 31 * result + pluginDependencies.hashCode();
        result = 31 * result + loadAfter.hashCode();
        return result;
    }
}
