package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.roles.Roles;
import java.io.File;
import java.util.Locale;
import org.apache.commons.lang.Validate;

public class GlobalRoleProvider extends RoleProvider
{
    public GlobalRoleProvider(Roles module)
    {
        super(module, true);
    }

    @Override
    public void init(File rolesFolder)
    {
        this.module.getLogger().debug("Loading global roles...");
        if (this.init) // provider is already initialized!
        {
            return;
        }
        if (this.folder == null)
        {
            // Sets the folder for this provider
            this.folder = rolesFolder;
        }
        super.init(rolesFolder);
    }
    
    @Override
    public Role getRole(String roleName)
    {
        Validate.notNull(roles, "The RoleName cannot be null!");
        if (roleName.startsWith("g:"))
        {
            roleName = roleName.substring(2);
        }
        return this.roles.get(roleName.toLowerCase(Locale.ENGLISH));
    }
}
