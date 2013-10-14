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

import java.util.HashSet;
import java.util.Set;

import de.cubeisland.engine.core.config.YamlConfiguration;
import de.cubeisland.engine.core.config.annotations.Option;
import de.cubeisland.engine.core.util.Version;

/**
 * This configuration is used to parse the module.yml file.
 */
public class ModuleConfig extends YamlConfiguration
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
    @Option("dependencies")
    public Set<String> dependencies = new HashSet<>(0);
    @Option("soft-dependencies")
    public Set<String> softDependencies = new HashSet<>(0);
    @Option("plugin-dependencies")
    public Set<String> pluginDependencies = new HashSet<>(0);
    @Option("load-after")
    public Set<String> loadAfter = new HashSet<>(0);
    @Option("services")
    public Set<String> services = new HashSet<>(0);
    @Option("service-providers")
    public Set<String> serviceProviders = new HashSet<>(0);
}
