package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Option;
import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Phillip Schichtel
 */
public class ModuleInfo extends Configuration
{
    @Option("name")
    public String name;

    @Option("revision")
    public int revision;

    @Option("description")
    public String desciprtion;

    @Option(value = "dependencies", genericType = String.class)
    public Set<String> dependencies;

    @Option(value = "soft-dependencies", genericType = String.class)
    public Set<String> softDependencies;

    public void onLoaded()
    {
        this.dependencies       = Collections.unmodifiableSet(this.dependencies);
        this.softDependencies   = Collections.unmodifiableSet(this.softDependencies);
    }
}
