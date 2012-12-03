package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class RoleProvider
{//TODO when reading a world and both roles/users are false remove it
    public final String mainWorld;
    private TIntObjectHashMap<Pair<Boolean, Boolean>> worlds = new TIntObjectHashMap<Pair<Boolean, Boolean>>(); //mirror roles / users
    private THashMap<String, RoleConfig> configs = new THashMap<String, RoleConfig>();
    private THashMap<String, Role> roles = new THashMap<String, Role>();

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

    public TIntObjectHashMap<Role> getMergedRolesFor(User user)
    {
        //TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
