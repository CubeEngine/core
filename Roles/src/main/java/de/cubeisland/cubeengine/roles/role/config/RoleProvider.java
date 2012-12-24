package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.RolesConfig;
import de.cubeisland.cubeengine.roles.exception.CircularRoleDepedencyException;
import de.cubeisland.cubeengine.roles.exception.RoleDependencyMissingException;
import de.cubeisland.cubeengine.roles.role.ConfigRole;
import de.cubeisland.cubeengine.roles.role.Role;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class RoleProvider
{
    private RoleMirror mirrorConfig;
    private THashMap<String, RoleConfig> configs = new THashMap<String, RoleConfig>();
    private THashMap<String, Role> roles = new THashMap<String, Role>();
    private boolean init = false;
    private boolean rolesCalculated = false;
    private File worldfolder = null;
    private Set<Role> defaultRoles = new HashSet<Role>();
    private Stack<String> roleStack = new Stack<String>();
    private Roles module = (Roles) CubeEngine.getModuleManager().getModule("Roles");

    public RoleProvider(RoleMirror mirrorConfig)
    {
        this.mirrorConfig = mirrorConfig;
    }

    /**
     * RoleProvider for a single world
     *
     * @param worldId
     */
    public RoleProvider(long worldId)
    {
        this.mirrorConfig = new RoleMirror(this.module, worldId);
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
            rolesFromDb = module.getManager().reloadRoles(user);
        }
        else
        {
            rolesFromDb = module.getManager().loadRoles(user);
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

    /**
     * Initializes this RoleProvider with its configurations
     *
     * @param rolesFolder
     */
    public void init(File rolesFolder)
    {
        if (this.init) // provider is already initialized!
        {
            return;
        }
        if (this.worldfolder == null)
        {
            // Sets the folder for this provider
            this.worldfolder = new File(rolesFolder, this.mirrorConfig.mainWorld);
        }
        this.worldfolder.mkdir(); // Creates folder for this privder if not existant
        this.module.getLogger().debug("Reading roles for the world-provider " + this.mirrorConfig.mainWorld + ":");
        int i = 0;
        for (File configFile : this.worldfolder.listFiles())
        {
            if (configFile.getName().endsWith(".yml"))
            {
                ++i;
                RoleConfig config = Configuration.load(RoleConfig.class, configFile);
                this.addConfig(config);
                this.module.getLogger().debug(config.roleName);
            }
        }
        this.module.getLogger().debug(i + " roles read!");
        this.init = true;
    }

    public void reload()
    {
        this.init = false;
        this.init(null);
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
            Role role = this.roles.get(roleName);
            if (role == null)
            {
                module.getLogger().log(LogLevel.WARNING, "Could not find default-role " + roleName);
            }
            this.defaultRoles.add(role);
        }
    }

    public void recalculateRoles(THashMap<String, Role> globalRoles)
    {
        this.calculateRoles(globalRoles, true);
    }

    public void recalculateDirtyRoles(THashMap<String, Role> globalRoles)
    {
        List<Role> dirtyChilds = new ArrayList<Role>();
        for (Role role : this.roles.values())
        {
            if (role.isDirty())
            {
                dirtyChilds.add(role);
            }
        }
        this.recalculateDirtyRoles(dirtyChilds, globalRoles);
    }

    private void recalculateDirtyRoles(List<Role> dirtyRoles, THashMap<String, Role> globalRoles)
    {
        List<Role> dirtyChilds = new ArrayList<Role>();
        for (Role role : dirtyRoles)
        {
            this.roles.remove(role.getName());
            this.roles.put(role.getName(), this.calculateRole(this.configs.get(role.getName()), globalRoles));
            for (Role childRole : role.getChildRoles())
            {
                dirtyChilds.add(childRole);
            }
        }
        if (!dirtyChilds.isEmpty())
        {
            this.recalculateDirtyRoles(dirtyChilds, globalRoles);
        }
    }

    public boolean calculateRoles(THashMap<String, Role> globalRoles, boolean recalculate)
    {
        if (this.rolesCalculated && !recalculate)
        {
            return false;
        }
        this.rolesCalculated = true;
        for (RoleConfig config : this.configs.values())
        {
            Role role = this.calculateRole(config, globalRoles);
            if (role == null)
            {
                module.getLogger().log(LogLevel.WARNING, config.roleName + " could not be calculated!");
                continue;
            }
            this.roles.put(role.getName(), role);
        }
        return true;
    }

    public Role calculateRole(RoleConfig config, THashMap<String, Role> globalRoles)
    {
        try
        {
            Role role = this.getRole(config.roleName);
            if (role != null)
            {
                return role;
            }
            this.roleStack.push(config.roleName);
            for (String parentName : config.parents)
            {
                try
                {
                    if (this.roleStack.contains(parentName)) // Circular Dependency?
                    {
                        throw new CircularRoleDepedencyException("Cannot load role! Circular Depenency detected in " + config.roleName + "\n" + StringUtils.implode(", ", roleStack));
                    }
                    RoleConfig parentConfig = null;

                    if (parentName.startsWith("g:"))
                    {
                        if (globalRoles.containsKey(parentName.substring(2)))
                        {
                            continue;
                        }
                    }
                    else
                    {
                        parentConfig = this.configs.get(parentName);;
                    }
                    if (parentConfig == null) // Dependency Missing?
                    {
                        throw new RoleDependencyMissingException("ParentRole missing for \"" + config.roleName + "\"\nUnkown role: " + parentName);
                    }
                    Role parentRole = this.calculateRole(parentConfig, globalRoles); // calculate parent-role
                    if (parentRole != null)
                    {
                        this.roles.put(parentRole.getName(), parentRole);
                    }
                }
                catch (RoleDependencyMissingException ex)
                {
                    module.getLogger().log(LogLevel.WARNING, ex.getMessage());
                }
            }
            // now all parent roles should be loaded
            Set<Role> parentRoles = new HashSet<Role>();
            for (String parentName : config.parents)
            {
                Role parentRole;
                if (parentName.startsWith("g:"))
                {
                    parentRole = globalRoles.get(parentName.substring(2));
                }
                else
                {
                    parentRole = this.roles.get(parentName);
                }
                if (parentRole == null)
                {
                    continue; // In case parent role was missing ingore it
                }
                parentRoles.add(parentRole); // Role was found:
            }
            role = new ConfigRole(config, parentRoles, false);
            this.roleStack.pop();
            this.module.getLogger().debug(role.getName() + " loaded!");
            return role;
        }
        catch (CircularRoleDepedencyException ex)
        {
            module.getLogger().log(LogLevel.WARNING, ex.getMessage());
            return null;
        }
    }

    public Collection<Role> getAllRoles()
    {
        return this.roles.values();
    }

    public String getMainWorld()
    {
        return this.mirrorConfig.mainWorld;
    }

    public boolean isCalculated()
    {
        return rolesCalculated;
    }

    public void setRolePermission(Role role, String perm, Boolean set)
    {
        role.setPermission(perm, set);
        this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
    }

    public void setRoleMetaData(Role role, String key, String value)
    {
        role.setMetaData(key, value);
        this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
    }

    public void resetRoleMetaData(Role role, String key)
    {
        role.setMetaData(key, null);
        this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
    }

    public void clearRoleMetaData(Role role)
    {
        role.clearMetaData();
        this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
    }

    public boolean setParentRole(Role role, Role pRole)
    {
        boolean added = role.setParentRole(pRole.getName());
        if (added)
        {
            this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
        }
        return added;
    }

    public boolean removeParentRole(Role role, Role pRole)
    {
        boolean removed = role.removeParentRole(pRole.getName());
        if (removed)
        {
            this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
        }
        return removed;
    }

    public void clearParentRoles(Role role)
    {
        role.clearParentRoles();
        this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
    }

    public void setRolePriority(Role role, Priority priority)
    {
        role.setPriority(priority);
        this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
    }

    public boolean renameRole(Role role, String newName)
    {
        if (this.roles.containsKey(newName))
        {
            return false;
        }
        role.rename(newName);
        // Removing old role
        RoleConfig config = this.configs.remove(role.getName());
        this.roles.remove(role.getName());
        // Set new role
        this.configs.put(newName, config);
        Role newRole = this.calculateRole(config, this.module.getManager().getGlobalRoles());
        newRole.setChildRoles(role.getChildRoles());
        this.roles.put(newName, newRole);
        // Recalculate dependend roles
        this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
        return true;
    }
}
