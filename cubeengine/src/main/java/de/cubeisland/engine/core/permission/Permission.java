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

import org.bukkit.permissions.Permissible;

import static de.cubeisland.engine.core.permission.PermDefault.FALSE;
import static de.cubeisland.engine.core.permission.PermDefault.OP;

public class Permission
{
    public final String name;
    String permission;
    PermDefault def;
    ParentPermission parent = null;

    public static final WildcardPermission BASE = createWildcard("cubeengine"); // cubeengine.*

    Permission(String name)
    {
        this(name, OP);
    }

    Permission(String name, PermDefault def)
    {
        this(null, name, def);
    }

    Permission(String parentName, String name)
    {
        this(parentName, name, OP);
    }

    Permission(String parentName, String name, PermDefault def)
    {
        if (parentName != null)
        {
            this.permission = parentName.toLowerCase() + "." + name.toLowerCase();
        }
        else
        {
            this.permission = name.toLowerCase();
        }
        this.def = def;
        this.name = name;
    }

    public static WildcardPermission createWildcard(String name)
    {
        return new WildcardPermission(name, FALSE);
    }

    public static WildcardPermission createWildcard(String name, PermDefault def)
    {
        return new WildcardPermission(name, def);
    }

    public static ParentPermission createParent(String name)
    {
        return new ParentPermission(name);
    }

    public static ParentPermission createParent(String name, PermDefault def)
    {
        return new ParentPermission(name, def);
    }

    public static Permission create(String name)
    {
        return new Permission(name, OP);
    }

    public static Permission create(String name, PermDefault def)
    {
        return new Permission(name, def);
    }

    Permission prepend(Permission toPrepend)
    {
        this.permission = toPrepend.permission + "." + this.permission;
        return this;
    }

    /**
     * Creates a detached permission that begins with the path of this permission
     */
    public Permission newPerm(String name)
    {
        return this.newPerm(name, OP);
    }

    /**
     * Creates a detached permission that begins with the path of this permission
     */
    public Permission newPerm(String name, PermDefault def)
    {
        return new Permission(this.permission, name, def);
    }

    /**
     * Creates a detached wildcard-permission that begins with the path of this permission
     */
    public WildcardPermission newWildcard(String name)
    {
        return this.newWildcard(name, FALSE);
    }

    /**
     * Creates a detached wildcard-permission that begins with the path of this permission
     */
    public WildcardPermission newWildcard(String name, PermDefault def)
    {
        return new WildcardPermission(this.permission, name, def);
    }

    public ParentPermission getParent()
    {
        return this.parent;
    }

    public boolean isAuthorized(Permissible permissible)
    {
        assert permissible != null: "The player may not be null!";

        return permissible.hasPermission(this.permission);
    }

    public String getName()
    {
        return this.permission;
    }

    public PermDefault getDefault()
    {
        return this.def;
    }

    public boolean hasChildren()
    {
        return false;
    }

    public boolean hasParent()
    {
        return this.parent != null;
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

        if (permission != null ? !permission.equals(that.permission) : that.permission != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return permission != null ? permission.hashCode() : 0;
    }
}
