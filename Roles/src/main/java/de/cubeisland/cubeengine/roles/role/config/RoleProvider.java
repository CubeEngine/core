package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.roles.role.config.RoleConfig;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.List;

public class RoleProvider
{
    public final String mainWorld;
    private TIntObjectHashMap<Pair<Boolean, Boolean>> worlds = new TIntObjectHashMap<Pair<Boolean, Boolean>>(); //mirror roles / users
    private THashMap<String, RoleConfig> configs = new THashMap<String, RoleConfig>();
    private THashMap<String, Role> roles = new THashMap<String, Role>();

    public RoleProvider(String mainWorld)
    {
        this.mainWorld = mainWorld;
    }

    /**
     * RoleProvider for a single world
     *
     * @param worldId
     */
    public RoleProvider(int worldId)
    {
        this.mainWorld = CubeEngine.getCore().getWorldManager().getWorld(worldId).getName();
        this.worlds.put(worldId, new Pair<Boolean, Boolean>(true, true));
    }

    public Iterable<RoleConfig> getConfigs()
    {
        return this.configs.values();
    }

    public void addConfig(RoleConfig config)
    {
        this.configs.put(config.roleName, config);
    }

    public void setRole(Role role)
    {
        this.roles.put(role.getName(), role);
    }

    public Role getRole(String roleName)
    {
        return this.roles.get(roleName);
    }

    public RoleConfig getConfig(String parentName)
    {
        return this.configs.get(parentName);
    }

    public TIntObjectHashMap<Pair<Boolean, Boolean>> getWorlds()
    {
        return this.worlds;
    }

    public TIntObjectHashMap<List<Role>> getRolesFor(User user, boolean reload)
    {
        TIntObjectHashMap<List<Role>> result = new TIntObjectHashMap<List<Role>>();
        TIntObjectHashMap<List<String>> rolesFromDb;
        if (reload)
        {
            rolesFromDb = Roles.getInstance().getManager().reloadRoles(user);
        }
        else
        {
            rolesFromDb = Roles.getInstance().getManager().loadRoles(user);
        }
        for (int worldID : rolesFromDb.keys())
        {
            Pair<Boolean, Boolean> mirrorRoleUsers = this.worlds.get(worldID);
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

    public void setWorld(String worldName, boolean roles, boolean users)
    {
        Integer world = CubeEngine.getCore().getWorldManager().getWorldId(worldName);
        if (world == null)
        {
            Roles.getInstance().getLogger().log(LogLevel.WARNING, "Unkown world " + worldName + "! Removing from config...");
            return;
        }
        this.worlds.put(world, new Pair<Boolean, Boolean>(roles, users));
    }

    public List<Role> getDefaultRoles()
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
