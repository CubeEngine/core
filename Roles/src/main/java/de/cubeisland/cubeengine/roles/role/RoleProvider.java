package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.exception.CircularRoleDepedencyException;
import de.cubeisland.cubeengine.roles.exception.RoleDependencyMissingException;
import de.cubeisland.cubeengine.roles.role.config.Priority;
import de.cubeisland.cubeengine.roles.role.config.RoleConfig;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import org.apache.commons.lang.Validate;

public abstract class RoleProvider
{
    protected THashMap<String, RoleConfig> configs = new THashMap<String, RoleConfig>();
    protected THashMap<String, Role> roles = new THashMap<String, Role>();
    protected boolean init = false;
    protected boolean rolesCalculated = false;
    protected File folder = null;
    protected Stack<String> roleStack = new Stack<String>();
    protected Roles module;
    public final boolean isGlobal;

    public RoleProvider(Roles module, boolean isGlobal)
    {
        this.module = module;
        this.isGlobal = false;
    }

    public Iterable<RoleConfig> getConfigs()
    {
        return this.configs.values();
    }

    public void addConfig(RoleConfig config)
    {
        this.configs.put(config.roleName.toLowerCase(Locale.ENGLISH), config);
    }

    public void setRole(Role role)
    {
        this.roles.put(role.getName().toLowerCase(Locale.ENGLISH), role);
    }

    public Role getRole(String roleName)
    {
        Validate.notNull(roles, "The RoleName cannot be null!");
        if (roleName.startsWith("g:"))
        {
            return this.module.getManager().getGlobalRoles().get(roleName.substring(2).toLowerCase(Locale.ENGLISH));
        }
        return this.roles.get(roleName.toLowerCase(Locale.ENGLISH));
    }

    public RoleConfig getConfig(String parentName)
    {
        return this.configs.get(parentName.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Initializes this RoleProvider with its configurations
     *
     * @param rolesFolder
     */
    public void loadInConfigurations(File rolesFolder)
    {
        this.folder.mkdir(); // Creates folder for this privder if not existant
        int i = 0;
        for (File configFile : this.folder.listFiles())
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
        this.loadInConfigurations(null);
    }

    public void recalculateRoles()
    {
        this.calculateRoles(true);
    }

    public void recalculateDirtyRoles(THashMap<String, Role> globalRoles)
    {
        Set<Role> dirtyChilds = new HashSet<Role>();
        for (Role role : this.roles.values())
        {
            if (role.isDirty())
            {
                dirtyChilds.add(role);
            }
        }
        this.recalculateDirtyRoles(dirtyChilds, globalRoles);
        for (User user : module.getUserManager().getOnlineUsers())
        {
            module.getManager().preCalculateRoles(user, true);
            module.getManager().applyRole(user.getPlayer());
        }
    }

    private void recalculateDirtyRoles(Set<Role> dirtyRoles, THashMap<String, Role> globalRoles)
    {
        Set<Role> dirtyChilds = new HashSet<Role>();
        for (Role role : dirtyRoles)
        {
            String roleName = role.getName().toLowerCase(Locale.ENGLISH);
            this.roles.remove(roleName);
            this.roles.put(roleName, this.calculateRole(this.configs.get(roleName), globalRoles));
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

    public boolean calculateRoles(boolean recalculate)
    {
        THashMap<String, Role> globalRoles = this.module.getManager().getGlobalRoles();
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
            this.roles.put(role.getName().toLowerCase(Locale.ENGLISH), role);
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
                        parentConfig = this.configs.get(parentName);
                    }
                    if (parentConfig == null) // Dependency Missing?
                    {
                        throw new RoleDependencyMissingException("ParentRole missing for \"" + config.roleName + "\"\nUnkown role: " + parentName);
                    }
                    Role parentRole = this.calculateRole(parentConfig, globalRoles); // calculate parent-role
                    if (parentRole != null)
                    {
                        this.roles.put(parentRole.getName().toLowerCase(Locale.ENGLISH), parentRole);
                    }
                }
                catch (RoleDependencyMissingException ex)
                {
                    module.getLogger().log(LogLevel.WARNING, ex.getMessage());
                }
            }
            // now all parent roles should be loaded
            TreeSet<Role> parentRoles = new TreeSet<Role>();
            for (String parentName : config.parents)
            {
                Role parentRole;
                parentName = parentName.toLowerCase(Locale.ENGLISH);
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

    public THashMap<String, Role> getRoles()
    {
        return roles;
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

    public boolean setParentRole(Role role, Role pRole) throws CircularRoleDepedencyException
    {
        this.checkCircularDependency(role, pRole);
        boolean added = role.setParentRole(pRole.getName());
        if (added)
        {
            this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
        }
        return added;
    }

    private void checkCircularDependency(Role role, Role pRole) throws CircularRoleDepedencyException
    {
        for (Role cRole : pRole.getParentRoles())
        {
            if (cRole.equals(role))
            {
                throw new CircularRoleDepedencyException("Cannot add parent!");
            }
            this.checkCircularDependency(role, cRole);
        }
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
        newName = newName.toLowerCase(Locale.ENGLISH);
        if (this.roles.containsKey(newName))
        {
            return false;
        }
        role.rename(newName);
        // Removing old role
        RoleConfig config = this.configs.remove(role.getName().toLowerCase(Locale.ENGLISH));
        this.roles.remove(role.getName());
        // Set new role
        this.configs.put(newName.toLowerCase(Locale.ENGLISH), config);
        Role newRole = this.calculateRole(config, this.module.getManager().getGlobalRoles());
        newRole.setChildRoles(role.getChildRoles());
        this.roles.put(newName, newRole);
        if (this instanceof WorldRoleProvider)
        {
            this.module.getDbManager().rename((WorldRoleProvider)this, role.getName(), newName);
        }
        // Recalculate dependend roles
        this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
        return true;
    }

    public boolean createRole(String roleName)
    {
        roleName = roleName.toLowerCase(Locale.ENGLISH);
        if (this.roles.containsKey(roleName))
        {
            return false;
        }
        RoleConfig config = new RoleConfig();
        config.setCodec("yml");
        config.roleName = roleName;
        this.configs.put(roleName.toLowerCase(Locale.ENGLISH), config);
        config.onLoaded();
        config.setFile(new File(this.folder, roleName + ".yml"));
        config.save();
        this.roles.put(roleName, this.calculateRole(config, this.module.getManager().getGlobalRoles()));
        return true;
    }

    public void deleteRole(Role role)
    {
        for (Role crole : role.getChildRoles())
        {
            crole.removeParentRole(role.getName());
        }
        this.roles.remove(role.getName());
        this.configs.remove(role.getName());
        ((ConfigRole)role).deleteConfigFile();
        this.recalculateDirtyRoles(this.module.getManager().getGlobalRoles());
    }
}
