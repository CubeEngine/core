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

import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Name;
import de.cubeisland.engine.core.util.Version;

/**
 * This configuration is used to parse the module.yml file.
 */
public class ModuleConfig extends YamlConfiguration
{
    @Name("main")
    public String main;
    @Name("name")
    public String name;
    @Name("description")
    public String description;
    @Name("version")
    public Version version = Version.ONE;
    @Name("source-version")
    public String sourceVersion = "unknown-unknown";
    @Name("core-version")
    public Version minCoreRevision = Version.ZERO;
    @Name("dependencies")
    public Set<String> dependencies = new HashSet<>(0);
    @Name("soft-dependencies")
    public Set<String> softDependencies = new HashSet<>(0);
    @Name("plugin-dependencies")
    public Set<String> pluginDependencies = new HashSet<>(0);
    @Name("load-after")
    public Set<String> loadAfter = new HashSet<>(0);
    @Name("services")
    public Set<String> services = new HashSet<>(0);
    @Name("service-providers")
    public Set<String> serviceProviders = new HashSet<>(0);
}
