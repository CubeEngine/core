package de.cubeisland.cubeengine.core.module;

import java.util.HashSet;
import java.util.Set;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.util.Version;

/**
 * This configuration is used to parse the module.yml file.
 */
@Codec("yml")
public class ModuleConfig extends Configuration
{
    @Option("main")
    public String main;
    @Option("name")
    public String name;
    @Option("version")
    public Version version = Version.ONE;
    @Option("description")
    public String description;
    @Option("core-version")
    public Version minCoreRevision = Version.ZERO;
    @Option("world-generator")
    public boolean provideWorldGenerator = false;
    @Option("dependencies")
    public Set<String> dependencies = new HashSet<String>(0);
    @Option("soft-dependencies")
    public Set<String> softDependencies = new HashSet<String>(0);
    @Option("plugin-dependencies")
    public Set<String> pluginDependencies = new HashSet<String>(0);
    @Option("load-after")
    public Set<String> loadAfter = new HashSet<String>(0);
}
