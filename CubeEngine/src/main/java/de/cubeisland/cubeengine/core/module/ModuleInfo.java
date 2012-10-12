package de.cubeisland.cubeengine.core.module;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;

/**
 *
 * @author Phillip Schichtel
 */
public final class ModuleInfo
{
    private static final char DEP_VERSION_DELIM = '/';
    
    private final File file;
    private final String main;
    private final String id;
    private final String name;
    private final int revision;
    private final String description;
    private final int minCoreVersion;
    private final Map<String, Integer> dependencies;
    private final Map<String, Integer> softDependencies;
    private final Set<String> pluginDependencies;

    public ModuleInfo(File file, ModuleConfiguration config)
    {
        Validate.notNull(config, "The module configuration failed to loaded!");
        Validate.notNull(config.name, "The module doesn't seem to have a name.");
        
        config.name = config.name.trim();
        Validate.notEmpty(config.name, "The module name seems to be empty.");
        
        this.file = file;
        this.id = config.name.toLowerCase(Locale.ENGLISH);
        this.name = this.id.substring(0, 1).toUpperCase(Locale.ENGLISH) + this.id.substring(1);
        
        if (config.main == null)
        {
            config.main = "de.cubeisland.cubeengine." + this.name.toLowerCase(Locale.ENGLISH) + "." + this.name;
        }
        this.main = config.main;
        
        this.revision = config.revision;
        this.description = config.description;
        this.minCoreVersion = config.minCoreRevision;
        
        
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
    }

    public File getFile()
    {
        return this.file;
    }
    
    public String getMain()
    {
        return this.main;
    }
    
    public String getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public int getRevision()
    {
        return this.revision;
    }

    public String getDescription()
    {
        return this.description;
    }
    
    public int getMinimumCoreRevision()
    {
        return this.minCoreVersion;
    }

    public Map<String, Integer> getDependencies()
    {
        return this.dependencies;
    }

    public Map<String, Integer> getSoftDependencies()
    {
        return this.softDependencies;
    }
    
    public Set<String> getPluginDependencies()
    {
        return this.pluginDependencies;
    }

    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof ModuleInfo)
        {
            return this.equals((ModuleInfo)obj);
        }
        
        return false;
    }
    
    public boolean equals(ModuleInfo other)
    {
        if (other == null)
        {
            return false;
        }
        
        if (!this.id.equals(other.id))
        {
            return false;
        }
        
        if (!this.main.equals(other.main))
        {
            return false;
        }
        
        if (this.minCoreVersion != other.minCoreVersion)
        {
            return false;
        }
        
        if (!this.dependencies.equals(other.dependencies))
        {
            return false;
        }
        
        if (!this.softDependencies.equals(other.softDependencies))
        {
            return false;
        }
        
        if (this.pluginDependencies.equals(other.pluginDependencies))
        {
            return false;
        }
        
        if (!this.file.equals(other.file))
        {
            return false;
        }
        
        return false;
    }
}