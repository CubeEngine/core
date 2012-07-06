package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Option;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Type;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Phillip Schichtel
 */
@Type("yml")
public class ModuleConfiguration extends Configuration
{
    @Option("name")
    public String name;

    @Option("revision")
    public int revision;

    @Option("description")
    public String desciprtion;

    @Option(value = "dependencies", genericType = String.class)
    public Set<String> dependencies = new HashSet<String>();

    @Option(value = "soft-dependencies", genericType = String.class)
    public Set<String> softDependencies = new HashSet<String>();

    @Override
    public void onLoaded()
    {
        this.dependencies.remove(this.name);
        this.softDependencies.remove(this.name);
    }
}
