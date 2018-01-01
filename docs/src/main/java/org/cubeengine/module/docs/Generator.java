/*
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
package org.cubeengine.module.docs;

import org.cubeengine.logscribe.Log;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.libcube.service.permission.Permission;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public interface Generator
{
    String generate(Log log, String id, String name, PluginContainer pc, Info info, Set<PermissionDescription> permissions, Set<CommandBase> commands,
            Permission basePermission);

    String generateList(Map<String, ModuleDocs> docs, Path modulePath, ModuleManager mm);
}
