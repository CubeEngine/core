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
    @Option("description")
    public String description;
    @Option("version")
    public Version version = Version.ONE;
    @Option("source-version")
    public String sourceVersion;
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
