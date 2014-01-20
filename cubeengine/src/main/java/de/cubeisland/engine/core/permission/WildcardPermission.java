package de.cubeisland.engine.core.permission;

import de.cubeisland.engine.core.command.exception.InvalidArgumentException;

import static de.cubeisland.engine.core.permission.PermDefault.FALSE;
import static de.cubeisland.engine.core.permission.PermDefault.OP;

public class WildcardPermission extends ParentPermission
{
    WildcardPermission(String name)
    {
        super(name + ".*");
    }

    WildcardPermission(String name, PermDefault def)
    {
        super(name + ".*", def);
    }

    WildcardPermission(String parentName, String name, PermDefault def)
    {
        super(parentName, name + ".*", def);
    }

    WildcardPermission(String parentName, String name)
    {
        super(parentName, name + ".*");
    }

    public Permission child(String name)
    {
        return this.child(name, OP);
    }

    /**
     * Creates a child-permission
     */
    public Permission child(String name, PermDefault def)
    {
        return this.getChild(name, def, false);
    }

    public WildcardPermission childWildcard(String name)
    {
        return this.childWildcard(name, FALSE);
    }

    public WildcardPermission childWildcard(String name, PermDefault def)
    {
        return (WildcardPermission)this.getChild(name, def, true);
    }

    Permission getChild(String name, PermDefault def, boolean wildcard)
    {
        Permission perm = this.getChild(name);
        if (perm != null)
        {
            if (!wildcard || (perm instanceof WildcardPermission))
            {
                return perm;
            }
            throw new InvalidArgumentException("Invalid permission!");
        }
        if (wildcard)
        {
            perm = this.newWildcard(name, def);
        }
        else
        {
            perm = this.newPerm(name, def);
        }
        this.addChild(perm);
        return perm;
    }
}
