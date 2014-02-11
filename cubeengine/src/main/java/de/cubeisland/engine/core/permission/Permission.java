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
package de.cubeisland.engine.core.permission;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.util.StringUtils;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.permission.PermDefault.FALSE;
import static de.cubeisland.engine.core.permission.PermDefault.OP;

public class Permission
{
    private final String name;
    private final PermDefault def;
    private final Set<Permission> parents = new HashSet<>(); // bound as children or attached

    private final Set<Permission> children = new HashSet<>(); // bound onto name.*
    private final Set<Permission> attached = new HashSet<>(); // bound onto name

    private final boolean wildcard;

    public static final Permission BASE = new Permission(null, "cubeengine", FALSE, true); // cubeengine.*

    private Permission(String parentName, String name, PermDefault def)
    {
       this(parentName, name, def, false);
    }

    private Permission(String parentName, String name, PermDefault def, boolean wildcard)
    {
        if (parentName != null)
        {
            this.name = parentName.toLowerCase() + "." + name.toLowerCase();
        }
        else
        {
            this.name = name.toLowerCase();
        }
        this.def = def;
        this.wildcard = wildcard;
    }

    public Permission newPerm(String name)
    {
        return this.newPerm(name, OP);
    }

    public Permission newPerm(String name, PermDefault def)
    {
        return new Permission(this.name, name, def);
    }

    public Permission newWildcard(String name)
    {
        return this.newWildcard(name, FALSE);
    }

    public Permission newWildcard(String name, PermDefault def)
    {
        return new Permission(this.name, name, def, true);
    }

    public Permission child(String name)
    {
        return this.child(name, OP);
    }

    public Permission child(String name, PermDefault def)
    {
        return this.getChild(name, def, false);
    }

    public Permission childWildcard(String name)
    {
        return this.childWildcard(name, FALSE);
    }

    public Permission childWildcard(String name, PermDefault def)
    {
        return this.getChild(name, def, true);
    }

    private Permission getChild(String name, PermDefault def, boolean wildcard)
    {
        for (Permission child : children)
        {
            if (child.name.equals(name) && child.wildcard == wildcard)
            {
                if (child.def != def)
                {
                    throw new IllegalArgumentException("Duplicate Permission with different PermDefault");
                }
                return child;
            }
        }
        Permission perm;
        if (wildcard)
        {
            perm = this.newWildcard(name, def);
        }
        else
        {
            perm = this.newPerm(name, def);
        }
        checkForCircularDependency(perm);
        this.children.add(perm);
        perm.parents.add(this);
        return perm;
    }

    private void checkForCircularDependency(Permission perm)
    {
        if (this.parents.isEmpty())
        {
            return;
        }
        if (this.parents.contains(perm))
        {
            throw new IllegalStateException("Circular PermissionDependency!");
        }
        for (Permission parent : this.parents)
        {
            parent.checkForCircularDependency(perm);
        }
    }

    public void attach(Permission... toAttach)
    {
        for (Permission perm : toAttach)
        {
            checkForCircularDependency(perm);
            this.attached.add(perm);
            perm.parents.add(this);
        }
    }

    public boolean isAuthorized(Permissible permissible)
    {
        expectNotNull(permissible, "The player may not be null!");

        return permissible.hasPermission(this.name + (this.isWildcard() ? ".*" : ""));
    }

    public String getName()
    {
        return name;
    }

    public PermDefault getDefault()
    {
        return def;
    }

    public Set<Permission> getParents()
    {
        return parents;
    }

    public Set<Permission> getChildren()
    {
        return children;
    }

    public Set<Permission> getAttached()
    {
        return attached;
    }

    public boolean isWildcard()
    {
        return wildcard;
    }

    public boolean hasChildren()
    {
        return !this.children.isEmpty();
    }

    public boolean hasAttached()
    {
        return !this.attached.isEmpty();
    }

    public void detach(Permission perm)
    {
        this.children.remove(perm);
        this.attached.remove(perm);
        perm.parents.remove(this);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Permission that = (Permission)o;

        if (wildcard != that.wildcard)
        {
            return false;
        }
        return !(name != null ? !name.equals(that.name) : that.name != null);
    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (wildcard ? 1 : 0);
        return result;
    }

    public static Permission getFor(CubeCommand command)
    {
        if (command.getPermission() != null && !command.getPermission().isEmpty())
        {
            String[] parts = StringUtils.explode(".", command.getPermission());
            if ("cubeengine".equalsIgnoreCase(parts[0]))
            {
                Permission perm = BASE;
                for (int i = 1; i < parts.length; i++)
                {
                    if (i + 1 == parts.length) // last
                    {
                        return perm.child(parts[i]);
                    }
                    perm = perm.childWildcard(parts[i]);
                }
            }
            else
            {
                throw new IllegalArgumentException("CubeCommand permission is not valid!");
            }
        }
        return null;
    }
}
