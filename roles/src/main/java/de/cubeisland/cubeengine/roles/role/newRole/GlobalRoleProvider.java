package de.cubeisland.cubeengine.roles.role.newRole;

import java.io.File;

import de.cubeisland.cubeengine.roles.Roles;

public class GlobalRoleProvider extends RoleProvider
{
    public GlobalRoleProvider(Roles module, RolesManager manager)
    {
        super(module, manager, 0);
    }

    @Override
    public File getFolder()
    {
        return this.manager.getRolesFolder();
    }
}
