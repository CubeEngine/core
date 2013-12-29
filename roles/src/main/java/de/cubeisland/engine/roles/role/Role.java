package de.cubeisland.engine.roles.role;

import java.util.Stack;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.roles.config.Priority;
import de.cubeisland.engine.roles.config.RoleConfig;

public class Role extends ResolvedDataHolder implements Comparable<Role>
{
    protected RoleConfig config;
    protected RoleProvider roleProvider;
    protected Permission rolePermission;

    public Role(RoleProvider roleProvider, RoleConfig config)
    {
        super(roleProvider);
        this.config = config;
        this.roleProvider = roleProvider;
        this.rolePermission = roleProvider.basePerm.createChild(config.roleName);
        this.module.getCore().getPermissionManager().registerPermission(this.module, this.rolePermission);
    }

    @Override
    public void calculate(Stack<String> roleStack)
    {
        if (this.isDirty())
        {
            super.calculate(roleStack);
            this.module.getLog().debug("   - {} calculated!", this.getName());
        }
    }

    @Override
    public String getName()
    {
        if (this.isGlobal())
        {
            return "g:" + this.config.roleName;
        }
        else
        {
            return this.config.roleName;
        }
    }



    public boolean isGlobal()
    {
        return this.roleProvider instanceof GlobalRoleProvider;
    }

    public int getPriorityValue()
    {
        return this.config.priority.value;
    }

    public void setPriorityValue(int value)
    {
        this.makeDirty();
        this.config.priority = Priority.getByValue(value);
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

        Role role = (Role)o;

        if (getName() != null ? !getName().equals(role.getName()) : role.getName() != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public int compareTo(Role o)
    {
        return this.getPriorityValue() - o.getPriorityValue();
    }
}
