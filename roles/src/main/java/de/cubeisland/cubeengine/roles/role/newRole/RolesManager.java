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
package de.cubeisland.cubeengine.roles.role.newRole;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.config.RoleConfig;
import de.cubeisland.cubeengine.roles.config.RoleMirror;
import de.cubeisland.cubeengine.roles.storage.AssignedRoleManager;
import de.cubeisland.cubeengine.roles.storage.UserMetaDataManager;
import de.cubeisland.cubeengine.roles.storage.UserPermissionsManager;

import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public class RolesManager
{
    protected Roles module;
    private WorldManager wm;

    private GlobalRoleProvider globalRoleProvider;
    private TLongObjectHashMap<WorldRoleProvider> worldRoleProviders;
    private Set<RoleProvider> providerSet;
    private TLongLongHashMap userMirrors;
    private TLongLongHashMap assignedRoleMirrors;

    private final File rolesFolder;

    protected AssignedRoleManager rm;
    protected UserMetaDataManager mdm;
    protected UserPermissionsManager pm;

    public RolesManager(Roles module)
    {
        this.module = module;
        this.wm = module.getCore().getWorldManager();
        this.rolesFolder = new File(module.getFolder(),"roles");

        this.rm = new AssignedRoleManager(module.getCore().getDB());
        this.mdm = new UserMetaDataManager(module.getCore().getDB());
        this.pm = new UserPermissionsManager(module.getCore().getDB());
    }

    public void initRoleProviders()
    {
        this.userMirrors = new TLongLongHashMap();
        this.assignedRoleMirrors = new TLongLongHashMap();

        this.globalRoleProvider = new GlobalRoleProvider(module,this);

        this.providerSet = new LinkedHashSet<RoleProvider>();
        this.providerSet.add(this.globalRoleProvider);

        this.createWorldRoleProviders(); // Create all WorldProviders according to their mirrors
        // Load In All Configurations & Create Role-Objects
        // AND Resolve Dependencies and Role-Data
        this.reloadAllRoles();
    }

    public void saveAll()
    {
        for (RoleProvider roleProvider : this.providerSet)
        {
            for (RoleConfig roleConfig : roleProvider.configs.values())
            {
                roleConfig.save();
            }
        }
    }
    
    private void createWorldRoleProviders()
    {
        this.worldRoleProviders = new TLongObjectHashMap<WorldRoleProvider>();
        for (RoleMirror mirror : this.module.getConfiguration().mirrors)
        {
            Long mainWorldID = wm.getWorldId(mirror.mainWorld);
            WorldRoleProvider provider = new WorldRoleProvider(module, this, mirror, mainWorldID);
            TLongObjectHashMap<Triplet<Boolean, Boolean, Boolean>> worldMirrors = provider.getWorldMirrors();
            this.module.getLog().log(LogLevel.DEBUG, "Loading role-provider for " + provider.getMainWorld());
            if (mainWorldID == null)
            {
                this.module.getLog().log(LogLevel.WARNING, "Unknown world " + provider.getMainWorld());
                continue;
            }
            for (long worldId : worldMirrors.keys())
            {
                if (this.worldRoleProviders.containsKey(worldId))
                {
                    this.module.getLog().log(LogLevel.ERROR,
                                             "The world " + this.module.getCore().getWorldManager().getWorld(worldId).getName()
                                                 + " is mirrored multiple times!\n"
                                                 + "Check your configuration under mirrors." + provider.getMainWorld());
                    continue;
                }
                if (worldMirrors.get(worldId).getFirst()) // Roles are mirrored add to provider...
                {
                    this.module.getLog().log(LogLevel.DEBUG, "  Mirror: " + wm.getWorld(worldId).getName());
                    this.worldRoleProviders.put(worldId, provider);
                    this.providerSet.add(provider);
                }
                if (worldMirrors.get(worldId).getSecond())
                {
                    this.assignedRoleMirrors.put(worldId,mainWorldID);
                }
                else
                {
                    this.assignedRoleMirrors.put(worldId,worldId);
                }
                if (worldMirrors.get(worldId).getThird()) // specific user perm/metadata is mirrored
                {
                    this.userMirrors.put(worldId,mainWorldID);
                }
                else
                {
                    this.userMirrors.put(worldId,worldId);
                }
            }
        }
        // World without mirrors
        for (long worldId : this.module.getCore().getWorldManager().getAllWorldIds())
        {
            if (this.getProvider(worldId) == null)
            {
                WorldRoleProvider provider = new WorldRoleProvider(module, this, worldId);
                this.worldRoleProviders.put(worldId, provider);
                this.providerSet.add(provider);
                this.assignedRoleMirrors.put(worldId,worldId);
                this.userMirrors.put(worldId,worldId);
                this.module.getLog().log(LogLevel.DEBUG,"Loading role-provider without mirror: "+wm.getWorld(worldId).getName());
            }
        }
    }

    private WorldRoleProvider getProvider(long worldId)
    {
        return this.worldRoleProviders.get(worldId);
    }

    public <Provider extends RoleProvider> Provider getProvider(World world)
    {
        return (Provider)(world == null ?
                          this.globalRoleProvider :
                          this.getProvider(this.wm.getWorldId(world)));
    }

    public File getRolesFolder()
    {
        return rolesFolder;
    }

    public GlobalRoleProvider getGlobalProvider()
    {
        return this.globalRoleProvider;
    }

    private void recalculateAllRoles()
    {
        for (RoleProvider roleProvider : providerSet)
        {

            roleProvider.recalculateRoles();
        }
    }

    public void reloadAllRoles()
    {
        for (RoleProvider roleProvider : providerSet)
        {
            roleProvider.loadConfigurations();
            roleProvider.reloadRoles();
        }
        this.recalculateAllRoles();
    }

    public RolesAttachment getRolesAttachment(User user)
    {
        return user.attachOrGet(RolesAttachment.class, this.module);
    }
}
