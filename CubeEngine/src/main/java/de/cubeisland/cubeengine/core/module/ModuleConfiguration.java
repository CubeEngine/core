package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import java.util.HashSet;
import java.util.Set;

/**
 * This configuration is used to parse the module.yml file.
 */
@Codec("yml")
public class ModuleConfiguration extends Configuration
{
    @Option("main")
    public String main;
    
    @Option("name")
    public String name;
    
    @Option("revision")
    public int revision = 1;
    
    @Option("description")
    public String description;
    
    @Option("core-version")
    public int minCoreRevision = -1;
    
    @Option("world-generator")
    public boolean provideWorldGenerator = false;
    
    @Option(value = "dependencies", genericType = String.class)
    public Set<String> dependencies = new HashSet<String>();
    
    @Option(value = "soft-dependencies", genericType = String.class)
    public Set<String> softDependencies = new HashSet<String>();
    
    @Option(value = "plugin-dependencies", genericType = String.class)
    public Set<String> pluginDependencies = new HashSet<String>();
}