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

import de.cubeisland.engine.reflect.ReflectedYaml;
import de.cubeisland.engine.core.util.Version;

/**
 * This configuration is used to parse the module.yml file.
 */
@SuppressWarnings("all")
public class ModuleConfig extends ReflectedYaml
{
    public String main;
    public String name;
    public String description;
    public Version version = Version.ONE;
    public String sourceVersion = "unknown-unknown";
    public Version minCoreVersion = Version.ZERO;
    public Set<String> dependencies = new HashSet<>(0);
    public Set<String> softDependencies = new HashSet<>(0);
    public Set<String> pluginDependencies = new HashSet<>(0);
    public Set<String> loadAfter = new HashSet<>(0);
    public Set<String> services = new HashSet<>(0);
    public Set<String> providedServices = new HashSet<>(0);

    @Override
    public boolean useStrictExceptionPolicy()
    {
        return true;
    }
}
