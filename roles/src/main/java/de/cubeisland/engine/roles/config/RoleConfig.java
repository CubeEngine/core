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
package de.cubeisland.engine.roles.config;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.reflect.ReflectedYaml;
import de.cubeisland.engine.reflect.annotations.Comment;
import de.cubeisland.engine.reflect.annotations.Name;

@SuppressWarnings("all")
public class RoleConfig extends ReflectedYaml
{
    @Name("role-name")
    @Comment("The name of this role")
    public String roleName = "defaultName";
    @Name("priority")
    @Comment("Use these as priority or just numbers\n"
        + "ABSULTEZERO(-273) < MINIMUM(0) < LOWEST(125) < LOWER(250) < LOW(375) < NORMAL(500) < HIGH(675) < HIGHER(750) < HIGHEST(1000) < OVER9000(9001)")
    public Priority priority = Priority.ABSULTEZERO;
    @Name("permissions")
    @Comment("The permission\n" +
                 "permission nodes can be assigned individually e.g.:\n" +
                 " - cubeengine.roles.command.assign\n" +
                 "or grouped into a tree (this will be done automatically) like this:\n" +
                 " - cubeengine.roles:\n" +
                 "     - command.assign\n" +
                 "     - world.world:\n" +
                 "         - guest\n" +
                 "         - member\n" +
                 "Use - directly in front of a permission to revoke that permission e.g.:\n" +
                 " - -cubeengine.roles.command.assign")
    public PermissionTree perms = new PermissionTree();
    @Name("parents")
    @Comment("The roles this role will inherit from.\n"
        + "Any priority of parents will be ignored!")
    public Set<String> parents = new HashSet<>();
    @Name("metadata")
    @Comment("The metadata such as prefix or suffix e.g.:\n" +
                 "metadata: \n" +
                 "  prefix: '&7Guest'")
    public Map<String, String> metadata = new LinkedHashMap<>();

    @Override
    public void onLoaded(File loadFrom) {
        if (this.priority == null)
        {
            this.priority = Priority.ABSULTEZERO;
        }
        if (this.parents == null)
        {
            this.parents = new HashSet<>();
        }
        if (this.metadata == null)
        {
            this.metadata = new LinkedHashMap<>();
        }
    }
}
