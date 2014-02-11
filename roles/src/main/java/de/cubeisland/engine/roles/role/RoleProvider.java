/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.roles.role;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Stack;

import org.bukkit.World;

import de.cubeisland.engine.configuration.exception.InvalidConfigurationException;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.config.RoleConfig;
import gnu.trove.map.hash.THashMap;

import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.YAML;

public abstract class RoleProvider
{
    protected final Roles module;
    protected final RolesManager manager;
    protected final Permission basePerm;

    protected THashMap<String, RoleConfig> configs;
    protected THashMap<String, Role> roles;

    protected RoleProvider(RolesManager manager, Permission basePerm)
    {
        this.module = manager.module;
        this.manager = manager;
        this.basePerm = basePerm;
    }

    /**
     * Gets the role with given name in the worlds managed by this RoleProvider
     *
     * @param name
     * @return
     */
    public Role getRole(String name)
    {
        return this.roles.get(name.toLowerCase());
    }

    /**
     * Gets all available roles in the worlds managed by this RoleProvider
     *
     * @return
     */
    public Collection<Role> getRoles()
    {
        return this.roles.values();
    }

    /**
     * Gets the folder where the configurations of this RoleProvider are saved.
     *
     * @return
     */
    protected abstract Path getFolder();

    /**
     * Loads in the configurations. Also removes all currently loaded roles.
     */
    protected void loadConfigurations()
    {
        this.configs = new THashMap<>();
        this.roles = new THashMap<>();
        for (User user : this.module.getCore().getUserManager().getLoadedUsers())
        {
            RolesAttachment rolesAttachment = user.get(RolesAttachment.class);
            if (rolesAttachment != null)
            {
                rolesAttachment.flushData();
            }
        }

        int i = 0;
        try
        {
            Path folder = this.getFolder(); // Creates folder for this provider if not existent
            Files.createDirectories(folder);
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(folder, YAML))
            {
                for (Path configFile : directory)
                {
                    ++i;
                    RoleConfig config = module.getCore().getConfigFactory().load(RoleConfig.class, configFile.toFile());
                    this.configs.put(config.roleName.toLowerCase(), config);
                }
            }
        }
        catch (IOException|InvalidConfigurationException e)
        {
            this.module.getLog().warn(e, "Failed to load a configuration");
        }
        this.module.getLog().debug("provider {}: {} role-configs read!", this.getFolder().getFileName(), i);
    }

    /**
     * Loads in all roles from the previously loaded configurations
     */
    protected void reloadRoles()
    {
        for (RoleConfig config : this.configs.values())
        {
            Role role = new Role(this.manager, this, config);
            this.roles.put(role.getName().toLowerCase(Locale.ENGLISH), role);
        }
    }

    /**
     * ReCalculates all roles
     */
    public void recalculateRoles()
    {
        Stack<String> roleStack = new Stack<>(); // stack for detecting circular dependencies
        for (Role role : this.roles.values())
        {
            role.resetTempData();
        }
        for (Role role : this.roles.values())
        {
            role.calculate(roleStack);
        }
    }

    /**
     * Creates a new Role with given name
     *
     * @param roleName
     * @return
     */
    public Role createRole(String roleName)
    {
        if (roleName.length() > 255)
        {
            throw new IllegalArgumentException("The max. length for rolenames is 255!");
        }
        roleName = roleName.toLowerCase(Locale.ENGLISH);
        if (this.roles.containsKey(roleName))
        {
            return null;
        }
        RoleConfig config = this.module.getCore().getConfigFactory().create(RoleConfig.class);
        config.roleName = roleName;
        this.configs.put(roleName,config);
        config.onLoaded(null);
        config.setFile(this.getFolder().resolve(roleName + ".yml").toFile());
        config.save();
        Role role = new Role(manager, this, config);
        this.roles.put(roleName, role);
        role.calculate(new Stack<String>());
        return role;
    }

    /**
     * Removes a managed role
     *
     * @param role
     */
    protected void removeRole(Role role)
    {
        this.roles.remove(role.getName());
        this.configs.remove(role.getName());
    }

    /**
     * Adds a managed role
     *
     * @param role
     */
    protected void addRole(Role role)
    {
        this.roles.put(role.getName(), role);
        this.configs.put(role.getName(), role.config);
    }

    public abstract World getMainWorld();
}
