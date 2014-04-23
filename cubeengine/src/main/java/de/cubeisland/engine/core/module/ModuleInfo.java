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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.util.Version;
import org.apache.commons.lang.Validate;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

/**
 * This class provides information about a module.
 */
public class ModuleInfo
{
    private static final char DEP_VERSION_DELIM = '/';
    private final Path path;
    private final String main;
    private final String id;
    private final String name;
    private final String description;
    private final Version version;
    private final String sourceVersion;
    private final Version minCoreVersion;
    private final Map<String, Version> dependencies;
    private final Map<String, Version> softDependencies;
    private final Set<String> pluginDependencies;
    private final Set<String> loadAfter;
    // Service Info:
    private final Set<String> services;
    private final Set<String> providedServices;

    ModuleInfo(Core core)
    {
        this.path = Paths.get("CubeEngine.jar");
        this.sourceVersion = core.getSourceVersion();
        if (core instanceof BukkitCore)
        {
            this.main = ((BukkitCore)core).getDescription().getMain();
        }
        else
        {
            this.main = "";
        }
        this.id = CoreModule.ID;
        this.name = CoreModule.NAME;
        this.description = "This is the core meta module.";
        this.version = core.getVersion();
        this.minCoreVersion = core.getVersion();
        this.dependencies = Collections.emptyMap();
        this.softDependencies = this.dependencies;
        this.pluginDependencies = Collections.emptySet();
        this.loadAfter = this.pluginDependencies;
        this.services = Collections.emptySet();
        this.providedServices = Collections.emptySet();
    }

    private static String nameToId(String name)
    {
        name = name.toLowerCase(Locale.US);
        name = name.replaceAll("[^a-z0-9]", "");
        return name;
    }

    public ModuleInfo(Path path, ModuleConfig config)
    {
        expectNotNull(config, "The module configuration failed to loaded!");
        expectNotNull(config.name, "The module doesn't seem to have a name.");

        this.name = config.name.trim();
        Validate.notEmpty(this.name, "The module name seems to be empty.");

        this.path = path;
        this.id = nameToId(config.name);

        if (config.main == null)
        {
            config.main = "de.cubeisland.engine." + this.id + "." + this.id.substring(0, 1).toUpperCase(Locale.US) + this.id.substring(1);
        }
        this.main = config.main;
        this.description = config.description;
        this.version = config.version;
        this.sourceVersion = config.sourceVersion;
        this.minCoreVersion = config.minCoreVersion;

        int delimOffset;
        Version version;

        this.dependencies = new HashMap<>(config.dependencies.size());
        config.dependencies.remove(this.id);
        for (String dep : config.dependencies)
        {
            dep = dep.toLowerCase();

            version = Version.ZERO;

            delimOffset = dep.indexOf(DEP_VERSION_DELIM);
            if (delimOffset > -1)
            {
                try
                {
                    version = Version.fromString(dep.substring(delimOffset + 1));
                }
                catch (NumberFormatException ignored)
                {}
                dep = dep.substring(0, delimOffset);
            }
            this.dependencies.put(dep, version);
        }

        this.softDependencies = new HashMap<>(config.softDependencies.size());
        config.softDependencies.remove(this.id);
        for (String dep : config.softDependencies)
        {
            dep = dep.toLowerCase();
            version = Version.ZERO;

            delimOffset = dep.indexOf(DEP_VERSION_DELIM);
            if (delimOffset > -1)
            {
                try
                {
                    version = Version.fromString(dep.substring(delimOffset + 1));
                }
                catch (NumberFormatException ignored)
                {}
                dep = dep.substring(0, delimOffset);
            }
            this.softDependencies.put(dep, version);
        }

        this.pluginDependencies = config.pluginDependencies;
        this.loadAfter = config.loadAfter;

        this.services = config.services;
        this.providedServices = config.providedServices;
    }

    /**
     * Returns the file the module got loaded from
     *
     * @return the module file
     */
    public Path getPath()
    {
        return this.path;
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
     * Gets the module's version
     *
     * @return the version
     */
    public Version getVersion()
    {
        return this.version;
    }

    /**
     * Gets the module's source version
     *
     * @return the source version string
     */
    public String getSourceVersion()
    {
        return sourceVersion;
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
    public Version getMinimumCoreVersion()
    {
        return this.minCoreVersion;
    }

    /**
     * Returns a map of the module's dependencies and dependency versions
     *
     * @return the dependencies
     */
    public Map<String, Version> getDependencies()
    {
        return this.dependencies;
    }

    /**
     * Returns a map of the module's soft dependencies and dependency versions
     *
     * @return the soft dependencies
     */
    public Map<String, Version> getSoftDependencies()
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

        return minCoreVersion.equals(that.minCoreVersion)
            && version.equals(that.version)
            && dependencies.equals(that.dependencies)
            && !(description != null ? !description.equals(that.description) : that.description != null)
            && id.equals(that.id) && loadAfter.equals(that.loadAfter) && main.equals(that.main)
            && name.equals(that.name) && pluginDependencies.equals(that.pluginDependencies)
            && softDependencies.equals(that.softDependencies);
    }

    @Override
    public int hashCode()
    {
        int result = main.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + minCoreVersion.hashCode();
        result = 31 * result + dependencies.hashCode();
        result = 31 * result + softDependencies.hashCode();
        result = 31 * result + pluginDependencies.hashCode();
        result = 31 * result + loadAfter.hashCode();
        return result;
    }

    public Set<String> getServices()
    {
        return services;
    }

    public Set<String> getProvidedServices()
    {
        return providedServices;
    }
}
