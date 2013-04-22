package de.cubeisland.cubeengine.roles.role.newRole;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.config.RoleMirror;

import gnu.trove.map.hash.TLongObjectHashMap;

public class WorldRoleProvider extends RoleProvider
{
    private RoleMirror mirrorConfig;
    private Set<Role> defaultRoles = new HashSet<Role>();

    public WorldRoleProvider(Roles module, RolesManager manager, RoleMirror mirror, long mainWorldId)
    {
        super(module,manager,mainWorldId);
        this.mirrorConfig = mirror;
    }

    public WorldRoleProvider(Roles module, RolesManager manager, long worldId)
    {
        super(module, manager, worldId);
        this.mirrorConfig = new RoleMirror(this.module, worldId);
    }

    public TLongObjectHashMap<Triplet<Boolean, Boolean, Boolean>> getWorldMirrors()
    {
        return this.mirrorConfig.getWorlds();
    }

    public Set<Role> getDefaultRoles()
    {
        return this.defaultRoles;
    }

    public String getMainWorld()
    {
        return this.mirrorConfig.mainWorld;
    }

    @Override
    public File getFolder()
    {
        if (this.folder == null)
        {
            // Sets the folder for this provider
            this.folder = new File(this.manager.getRolesFolder(), this.mirrorConfig.mainWorld);
        }
        return this.folder;
    }

    @Override
    public Role getRole(String name)
    {
        name = name.toLowerCase();
        if (name.startsWith("g:"))
        {
            return this.manager.getGlobalProvider().getRole(name.substring(2));
        }
        return super.getRole(name);
    }
}
