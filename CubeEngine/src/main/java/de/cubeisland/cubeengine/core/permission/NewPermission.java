package de.cubeisland.cubeengine.core.permission;

import java.util.Set;

import org.bukkit.permissions.Permissible;

import gnu.trove.set.hash.THashSet;

import static de.cubeisland.cubeengine.core.permission.PermDefault.OP;

public class NewPermission
{
    private String permission;
    private PermDefault def;

    private Set<NewPermission> roots = null;

    private NewPermission parent = null;
    private Set<NewPermission> children = null;

    final boolean canRegister;

    /**
     * Creates a new abstract permission that will not register
     */
    NewPermission(boolean b, String parentName, String name)
    {
        this.permission = parentName.toLowerCase() + "." + name.toLowerCase();
        this.canRegister = false;
    }

    /**
     * Creates a new abstract permission that will not register
     */
    NewPermission(boolean b, String name)
    {
        this.permission = name.toLowerCase();
        this.canRegister = false;
    }

    public NewPermission(String name)
    {
       this(name,OP);
    }

    public NewPermission(String name, PermDefault def)
    {
        this.permission = name.toLowerCase();
        this.def = def;
        this.canRegister = true;
    }

    public NewPermission(String parentName, String name, PermDefault def)
    {
        this.permission = parentName + "." + name.toLowerCase();
        this.def = def;
        this.canRegister = true;
    }

    public NewPermission(String parentName, String name)
    {
        this.permission = parentName + "." + name.toLowerCase();
        this.def = def;
        this.canRegister = true;
    }

    /**
     * Adds this permission to given bundle-permission.
     * This will not affect any name of the permissions.
     *
     * @param bundlePermission
     * @return fluent interface
     */
    public NewPermission attachTo(NewPermission bundlePermission)
    {
        this.addRoot(bundlePermission);
        return this;
    }

    /**
     * Adds given permissions to this bundle-permission to form a bundle.
     * This will not affect any name of the permissions.
     *
     * @param toAttach
     * @return fluent interface
     */
    public NewPermission attach(NewPermission... toAttach)
    {
        for (NewPermission perm : toAttach)
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
    public NewPermission setAsChildOf(NewPermission parentPermission)
    {
        this.parent = parentPermission;
        this.permission = parentPermission.permission + "." + this.permission;
        if (!parentPermission.hasChildren())
        {
            parentPermission.children = new THashSet<NewPermission>();
        }
        parentPermission.children.add(this);
        for (NewPermission childPerm : children)
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
    public NewPermission addChildren(NewPermission... childPerms)
    {
        for (NewPermission child : childPerms)
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
            for (NewPermission childPerm : children)
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
    public NewPermission createChild(String name)
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
    public NewPermission createAbstract(String name)
    {
        return new NewPermission(true, this.permission, name);
    }

    /**
     * Creates an abstract child-permission.
     * This permission will not be registered.
     * A wildcard permission however will be registered if needed.
     *
     * @param name
     * @return
     */
    public NewPermission createAbstractChild(String name)
    {
        NewPermission newPermission = this.createAbstract(name);
        newPermission.parent = this;
        if (!this.hasChildren())
        {
            this.children = new THashSet<NewPermission>();
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
    public NewPermission createNew(String name)
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
    public NewPermission createNew(String name, PermDefault def)
    {
        NewPermission newPermission = new NewPermission(this.permission,name,def);
        return newPermission;
    }

    /**
     * Creates a child-permission
     *
     * @param name
     * @param def
     * @return
     */
    public NewPermission createChild(String name, PermDefault def)
    {
        NewPermission newPermission = new NewPermission(this.permission,name,def);
        newPermission.parent = this;
        if (!this.hasChildren())
        {
            this.children = new THashSet<NewPermission>();
        }
        this.children.add(newPermission);
        return newPermission;
    }

    public Set<NewPermission> getRoots()
    {
        return this.roots;
    }

    public NewPermission getParent()
    {
        return this.parent;
    }

    public boolean isAuthorized(Permissible player)
    {
        return player.hasPermission(this.permission);
    }

    public String getPermission()
    {
        return this.permission;
    }

    public PermDefault getPermissionDefault()
    {
        return this.def;
    }

    private void addRoot(NewPermission root)
    {
        if (roots == null)
        {
            roots = new THashSet<NewPermission>();
        }
        roots.add(root);
    }

    public Set<NewPermission> getChildren()
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

    public boolean hasRoots()
    {
        return this.roots == null ? false : !this.roots.isEmpty();
    }
}
