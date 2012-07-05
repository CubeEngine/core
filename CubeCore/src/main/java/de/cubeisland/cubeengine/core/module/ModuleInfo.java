package de.cubeisland.cubeengine.core.module;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Phillip Schichtel
 */
public final class ModuleInfo
{
    private final File file;
    private final String name;
    private final int revision;
    private final String description;
    private final Set<String> dependencies;
    private final Set<String> softDependencies;
    
    public ModuleInfo(File file, ModuleConfiguration config)
    {
        this.file               = file;
        this.name               = config.name;
        this.revision           = config.revision;
        this.description        = config.desciprtion;
        this.dependencies       = Collections.unmodifiableSet(config.dependencies);
        this.softDependencies   = Collections.unmodifiableSet(config.softDependencies);
    }

    public File getFile()
    {
        return this.file;
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

    public Set<String> getDependencies()
    {
        return this.dependencies;
    }

    public Set<String> getSoftDependencies()
    {
        return this.softDependencies;
    }
}
