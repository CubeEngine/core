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

import java.util.Set;

import org.bukkit.permissions.Permissible;

import gnu.trove.set.hash.THashSet;

import static de.cubeisland.engine.core.permission.PermDefault.FALSE;
import static de.cubeisland.engine.core.permission.PermDefault.OP;

public class Permission
{
    private String permission;
    private PermDefault def;

    private Set<Permission> bundle = null;

    private Permission parent = null;
    private Set<Permission> children = null;

    private final boolean registrable;

    public static final Permission BASE = createAbstractPermission("cubeengine", FALSE);

    public static Permission createAbstractPermission(String name)
    {
        return new Permission(false, name, OP);
    }

    public static Permission createAbstractPermission(String name, PermDefault def)
    {
        return new Permission(false, name, def);
    }

    public static Permission createPermission(String name)
    {
        return new Permission(true, name, OP);
    }

    public static Permission createPermission(String name, PermDefault def)
    {
        return new Permission(true, name, def);
    }

    /**
     * Creates a new permission
     *
     * @param registrable false to make an abstract permission that will not register
     */
    private Permission(boolean registrable, String parentName, String name, PermDefault def)
    {
        this.permission = parentName.toLowerCase() + "." + name.toLowerCase();
        this.registrable = registrable;
        this.def = def;
    }

    /**
     * Creates a new permission
     *
     * @param registrable false to make an abstract permission that will not register
     */
    private Permission(boolean registrable, String name, PermDefault def)
    {
        assert !name.contains("*") : "* permissions are generated automatically!";
        this.permission = name.toLowerCase();
        this.registrable = registrable;
        this.def = def;
    }

    public boolean isRegistrable()
    {
        return registrable;
    }

    protected Permission(String name)
    {
        this(name, OP);
    }

    protected Permission(String name, PermDefault def)
    {
        this(true, name, def);
    }

    protected Permission(String parentName, String name, PermDefault def)
    {
        this(true, parentName, name, def);
    }

    protected Permission(String parentName, String name)
    {
        this(parentName, name, OP);
    }

    /**
     * Adds this permission to given bundle-permission.
     * This will not affect any name of the permissions.
     *
     * @return fluent interface
     */
    public Permission attachTo(Permission bundlePermission)
    {
        bundlePermission.addBundle(this);
        return this;
    }

    /**
     * Adds given permissions to this bundle-permission to form a bundle.
     * This will not affect any name of the permissions.
     *
     * @return fluent interface
     */
    public Permission attach(Permission... toAttach)
    {
        for (Permission perm : toAttach)
        {
            perm.attachTo(this);
        }
        return this;
    }

    /**
     * Sets this permission as child of given parent-permission.
     * The child-permission and all its children will prepend the parents-permission
     *
     * @return fluent interface
     */
    public Permission setAsChildOf(Permission parentPermission)
    {
        this.parent = parentPermission;
        this.permission = parentPermission.permission + "." + this.permission;
        if (!parentPermission.hasChildren())
        {
            parentPermission.children = new THashSet<>();
        }
        parentPermission.children.add(this);
        if (children != null)
        {
            for (Permission childPerm : children)
            {
                childPerm.prepend(parentPermission.permission);
            }
        }
        return this;
    }

    /**
     * Prepends the parent-permission-name to this permission and all child permissions.
     * <p>This permission will not be included in the parents * permission!
     *
     * @param prependPermission the permission to prepend
     * @return fluent interface
     */
    public Permission prepend(Permission prependPermission)
    {
        this.permission = prependPermission.permission + "." + this.permission;
        for (Permission child : this.children)
        {
            child.prepend(prependPermission.permission);
        }
        return this;
    }

    /**
     * Sets the given permission as child of this parent-permission.
     * The child-permission and all its children will prepend the parents-permission
     *
     * @return fluent interface
     */
    public Permission addChildren(Permission... childPerms)
    {
        for (Permission child : childPerms)
        {
            child.setAsChildOf(this);
        }
        return this;
    }

    /**
     * Prepends given string to this permission and all child permissions
     */
    private void prepend(String s)
    {
        this.permission = s + "." + this.permission;
        if (this.hasChildren())
        {
            for (Permission childPerm : children)
            {
                childPerm.prepend(s);
            }
        }
    }

    /**
     * Creates a child-permission
     */
    public Permission createChild(String name)
    {
        return this.createChild(name, OP);
    }

    /**
     * Creates an independent abstract permission that begins with the path of this permission.
     * This permission will not be registered.
     */
    public Permission createAbstract(String name)
    {
        return new Permission(false, this.permission, name, FALSE);
    }

    /**
     * Creates an abstract child-permission.
     * This permission will not be registered.
     * A wildcard permission however will be registered if needed.
     */
    public Permission createAbstractChild(String name)
    {
        Permission newPermission = this.createAbstract(name);
        newPermission.parent = this;
        if (!this.hasChildren())
        {
            this.children = new THashSet<>();
        }
        this.children.add(newPermission);
        return newPermission;
    }


    /**
     * Creates an independent permission that begins with the path of this permission
     */
    public Permission createNew(String name)
    {
        return this.createNew(name, OP);
    }

    /**
     * Creates an independent permission that begins with the path of this permission
     */
    public Permission createNew(String name, PermDefault def)
    {
        return new Permission(this.permission, name, def);
    }

    /**
     * Creates a child-permission
     */
    public Permission createChild(String name, PermDefault def)
    {
        Permission newPermission = new Permission(this.permission, name, def);
        newPermission.parent = this;
        if (!this.hasChildren())
        {
            this.children = new THashSet<>();
        }
        this.children.add(newPermission);
        return newPermission;
    }

    public Set<Permission> getBundles()
    {
        return this.bundle;
    }

    public Permission getParent()
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

    private void addBundle(Permission bundlePermission)
    {
        if (bundle == null)
        {
            bundle = new THashSet<>();
        }
        bundle.add(bundlePermission);
    }

    public Set<Permission> getChildren()
    {
        return children;
    }

    public boolean hasChildren()
    {
        return children != null && !children.isEmpty();
    }

    public boolean hasParent()
    {
        return this.parent != null;
    }

    public boolean hasBundles()
    {
        return this.bundle != null && !this.bundle.isEmpty();
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

    public void removeChild(Permission permission)
    {
        this.children.remove(permission);
    }
}
