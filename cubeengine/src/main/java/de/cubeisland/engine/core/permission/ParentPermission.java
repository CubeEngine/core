package de.cubeisland.engine.core.permission;

import java.util.HashSet;
import java.util.Set;

public class ParentPermission extends Permission
{
    Set<Permission> children = new HashSet<>();

    ParentPermission(String name)
    {
        super(name);
    }

    ParentPermission(String name, PermDefault def)
    {
        super(name, def);
    }

    ParentPermission(String parentName, String name, PermDefault def)
    {
        super(parentName, name, def);
    }

    ParentPermission(String parentName, String name)
    {
        super(parentName, name);
    }

    void addChild(Permission perm)
    {
        children.add(perm);
        perm.parent = this;
    }

    @Override
    ParentPermission prepend(Permission toPrepend)
    {
        super.prepend(toPrepend);
        for (Permission child : this.children)
        {
            child.prepend(toPrepend);
        }
        return this;
    }

    @Override
    public boolean hasChildren()
    {
        return !this.children.isEmpty();
    }

    /**
     * Adds given permissions to this parent-permission to form a bundle.
     * This will not affect any name of the permissions.
     *
     * @return fluent interface
     */
    public ParentPermission attach(Permission... toAttach)
    {
        for (Permission perm : toAttach)
        {
            this.addChild(perm);
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
            child.prepend(this);
            this.addChild(child);
        }
        return this;
    }

    public void removeChild(Permission permission)
    {
        this.children.remove(permission);
    }

    public Permission getChild(String name)
    {
        for (Permission child : children)
        {
            if (child.name.equals(name))
            {
                return child;
            }
        }
        return null;
    }

    public Set<Permission> getChildren()
    {
        return children;
    }
}
