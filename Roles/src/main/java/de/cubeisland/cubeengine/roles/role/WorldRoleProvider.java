package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.RolesConfig;
import de.cubeisland.cubeengine.roles.role.config.RoleMirror;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;

public class WorldRoleProvider extends RoleProvider
{
    private RoleMirror mirrorConfig;
    private Set<Role> defaultRoles = new HashSet<Role>();

    public WorldRoleProvider(Roles module, RoleMirror mirrorConfig)
    {
        super(module, false);
        this.mirrorConfig = mirrorConfig;
    }

    public WorldRoleProvider(Roles module, long worldId)
    {
        super(module, false);
        this.mirrorConfig = new RoleMirror(this.module, worldId);
    }

    public TLongObjectHashMap<Pair<Boolean, Boolean>> getWorlds()
    {
        return this.mirrorConfig.getWorlds();
    }

    public TLongObjectHashMap<List<Role>> getRolesFor(User user, boolean reload)
    {
        TLongObjectHashMap<List<Role>> result = new TLongObjectHashMap<List<Role>>();
        TLongObjectHashMap<List<String>> rolesFromDb;
        if (reload)
        {
            rolesFromDb = module.getRoleManager().reloadRoles(user);
        }
        else
        {
            rolesFromDb = module.getRoleManager().loadRoles(user);
        }
        for (long worldID : rolesFromDb.keys())
        {
            Pair<Boolean, Boolean> mirrorRoleUsers = this.mirrorConfig.getWorlds().get(worldID);
            if (mirrorRoleUsers == null)
            {
                continue; // world is not in this provider
            }
            List<Role> roleList = new ArrayList<Role>();
            result.put(worldID, roleList);
            if (mirrorRoleUsers.getLeft() == mirrorRoleUsers.getRight()// both true -> full mirror
                || mirrorRoleUsers.getLeft()) // roles are mirrored BUT but assigned roles are not mirrored!
            {
                for (String roleName : rolesFromDb.get(worldID))
                {
                    Role role = this.getRole(roleName);
                    if (role == null)
                    {
                        throw new IllegalStateException("Role does not exist!");
                    }
                    roleList.add(role);
                }
            }
            //else roles are not mirrored BUT assigned roles are mirrored! -> this world will have its own provider
        }
        return result;
    }

    public Set<Role> getDefaultRoles()
    {
        return this.defaultRoles;
    }

    public void loadDefaultRoles(RolesConfig config)
    {
        List<String> dRoles = config.defaultRoles.get(this.mirrorConfig.mainWorld);
        if (dRoles == null || dRoles.isEmpty())
        {
            module.getLogger().log(LogLevel.WARNING, "No default-roles defined for " + this.mirrorConfig.mainWorld);
            return;
        }
        for (String roleName : dRoles)
        {
            Role role = this.roles.get(roleName.toLowerCase(Locale.ENGLISH));
            if (role == null)
            {
                module.getLogger().log(LogLevel.WARNING, "Could not find default-role " + roleName);
            }
            this.defaultRoles.add(role);
        }
    }

    @Override
    public void loadInConfigurations(File rolesFolder)
    {
        if (this.init) // provider is already initialized!
        {
            return;
        }
        if (this.folder == null)
        {
            // Sets the folder for this provider
            this.folder = new File(rolesFolder, this.mirrorConfig.mainWorld);
        }
        this.module.getLogger().log(DEBUG, "Loading roles for provider of " + this.mirrorConfig.mainWorld + ":");
        super.loadInConfigurations(rolesFolder);
    }

    public String getMainWorld()
    {
        return this.mirrorConfig.mainWorld;
    }

    public boolean toggleDefaultRole(Role role)
    {
        if (this.defaultRoles.contains(role))
        {
            List<String> defaultConfigRoles = this.module.getConfiguration().defaultRoles.get(this.mirrorConfig.mainWorld);
            defaultConfigRoles.remove(role.getName());
            this.defaultRoles.remove(role);
            this.module.getConfiguration().save();
            return false;
        }
        else
        {
            List<String> defaultConfigRoles = this.module.getConfiguration().defaultRoles.get(this.mirrorConfig.mainWorld);
            defaultConfigRoles.add(role.getName());
            this.defaultRoles.add(role);
            this.module.getConfiguration().save();
            return true;
        }
    }
}
