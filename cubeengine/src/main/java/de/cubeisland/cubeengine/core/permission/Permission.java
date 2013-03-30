package de.cubeisland.cubeengine.core.permission;

import java.util.Locale;
import java.util.Set;

import org.bukkit.permissions.Permissible;

import de.cubeisland.cubeengine.core.CubeEngine;

import gnu.trove.set.hash.THashSet;

import static de.cubeisland.cubeengine.core.permission.PermDefault.FALSE;
import static de.cubeisland.cubeengine.core.permission.PermDefault.OP;

public class Permission
{
    private String permission;
    private PermDefault def;

    private Set<Permission> bundle = null;

    private Permission parent = null;
    private Set<Permission> children = null;

    public final boolean canRegister;

    public static final Permission BASE = new Permission(false,"cubeengine",FALSE);

    /**
     * Creates a new permission
     *
     * @param canRegister false to make an abstract permission that will not register
     * @param parentName
     * @param name
     */
    private Permission(boolean canRegister, String parentName, String name, PermDefault def)
    {
        this.permission = parentName.toLowerCase() + "." + name.toLowerCase();
        this.canRegister = canRegister;
        this.def = def;
    }

    /**
     * Creates a new permission
     *
     * @param canRegister false to make an abstract permission that will not register
     * @param name
     */
    private Permission(boolean canRegister, String name, PermDefault def)
    {
        this.permission = name.toLowerCase();
        this.canRegister = canRegister;
        this.def = def;
    }

    public Permission(String name)
    {
       this(name,OP);
    }

    public Permission(String name, PermDefault def)
    {
        this(true,name,def);
    }

    public Permission(String parentName, String name, PermDefault def)
    {
        this(true,parentName,name,def);
    }

    public Permission(String parentName, String name)
    {
        this(parentName,name,OP);
    }

    /**
     * Adds this permission to given bundle-permission.
     * This will not affect any name of the permissions.
     *
     * @param bundlePermission
     * @return fluent interface
     */
    public Permission attachTo(Permission bundlePermission)
    {
        this.addBundle(bundlePermission);
        return this;
    }

    /**
     * Adds given permissions to this bundle-permission to form a bundle.
     * This will not affect any name of the permissions.
     *
     * @param toAttach
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
     * @param parentPermission
     * @return fluent interface
     */
    public Permission setAsChildOf(Permission parentPermission)
    {
        this.parent = parentPermission;
        this.permission = parentPermission.permission + "." + this.permission;
        if (!parentPermission.hasChildren())
        {
            parentPermission.children = new THashSet<Permission>();
        }
        parentPermission.children.add(this);
        for (Permission childPerm : children)
        {
            childPerm.prepend(parentPermission.permission);
        }
        return this;
    }

    /**
     * Sets the given permission as child of this parent-permission.
     * The child-permission and all its children will prepend the parents-permission
     *
     * @param childPerms
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
     *
     * @param s
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
     *
     * @param name
     * @return
     */
    public Permission createChild(String name)
    {
        return this.createChild(name , OP);
    }

    /**
     * Creates an independent abstract permission that begins with the path of this permission.
     * This permission will not be registered.
     *
     * @param name
     * @return
     */
    public Permission createAbstract(String name)
    {
        return new Permission(false, this.permission, name, FALSE);
    }

    /**
     * Creates an abstract child-permission.
     * This permission will not be registered.
     * A wildcard permission however will be registered if needed.
     *
     * @param name
     * @return
     */
    public Permission createAbstractChild(String name)
    {
        Permission newPermission = this.createAbstract(name);
        newPermission.parent = this;
        if (!this.hasChildren())
        {
            this.children = new THashSet<Permission>();
        }
        this.children.add(newPermission);
        return newPermission;
    }


    /**
     * Creates an independent permission that begins with the path of this permission
     *
     * @param name
     * @return
     */
    public Permission createNew(String name)
    {
        return this.createNew(name, OP);
    }

    /**
     * Creates an independent permission that begins with the path of this permission
     *
     * @param name
     * @param def
     * @return
     */
    public Permission createNew(String name, PermDefault def)
    {
        Permission newPermission = new Permission(this.permission,name,def);
        return newPermission;
    }

    /**
     * Creates a child-permission
     *
     * @param name
     * @param def
     * @return
     */
    public Permission createChild(String name, PermDefault def)
    {
        Permission newPermission = new Permission(this.permission,name,def);
        newPermission.parent = this;
        if (!this.hasChildren())
        {
            this.children = new THashSet<Permission>();
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

    public boolean isAuthorized(Permissible player)
    {
        return player.hasPermission(this.permission);
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
            bundle = new THashSet<Permission>();
        }
        bundle.add(bundlePermission);
    }

    public Set<Permission> getChildren()
    {
        return children;
    }

    public boolean hasChildren()
    {
        return children == null ? false : !children.isEmpty();
    }

    public boolean hasParent()
    {
        return this.parent != null;
    }

    public boolean hasBundles()
    {
        return this.bundle == null ? false : !this.bundle.isEmpty();
    }
}
