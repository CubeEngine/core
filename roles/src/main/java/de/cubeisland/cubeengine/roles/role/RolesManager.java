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
package de.cubeisland.cubeengine.roles.role;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Profiler;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.RolesConfig;
import de.cubeisland.cubeengine.roles.config.RoleConfig;
import de.cubeisland.cubeengine.roles.config.RoleMirror;
import de.cubeisland.cubeengine.roles.storage.AssignedRoleManager;
import de.cubeisland.cubeengine.roles.storage.UserMetaDataManager;
import de.cubeisland.cubeengine.roles.storage.UserPermissionsManager;

import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

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
        this.rolesFolder.mkdir();

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
            if (mainWorldID == null)
            {
                this.module.getLog().log(LogLevel.WARNING, "Ignoring unknown world: " + mirror.mainWorld);
                continue;
            }
            WorldRoleProvider provider = new WorldRoleProvider(module, this, mirror, mainWorldID);
            this.registerWorldRoleProvider(provider); // mirrors
        }
        this.createMissingWorldRoleProviders();// World without mirrors
    }

    private void registerWorldRoleProvider(WorldRoleProvider provider)
    {
        TLongObjectHashMap<Triplet<Boolean, Boolean, Boolean>> worldMirrors = provider.getWorldMirrors();
        this.module.getLog().log(LogLevel.DEBUG, "Loading role-provider for " + provider.getMainWorld());
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
                this.module.getLog().log(LogLevel.DEBUG, "  " + wm.getWorld(worldId).getName()+
                    ": ( RoleMirror " + (worldMirrors.get(worldId).getSecond() ? "; AssignedRoleMirror " : "")
                    + (worldMirrors.get(worldId).getThird() ? "; UserDataMirror " :"") + ")");
                this.worldRoleProviders.put(worldId, provider);
                this.providerSet.add(provider);
            }
            if (worldMirrors.get(worldId).getSecond())
            {
                this.assignedRoleMirrors.put(worldId,provider.getMainWorldId());
            }
            else
            {
                this.assignedRoleMirrors.put(worldId,worldId);
            }
            if (worldMirrors.get(worldId).getThird()) // specific user perm/metadata is mirrored
            {
                this.userMirrors.put(worldId,provider.getMainWorldId());
            }
            else
            {
                this.userMirrors.put(worldId,worldId);
            }
        }
    }

    private void createMissingWorldRoleProviders()
    {
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

    public WorldRoleProvider getProvider(long worldId)
    {
        return this.worldRoleProviders.get(worldId);
    }

    public <Provider extends RoleProvider> Provider getProvider(World world)
    {
        if (world == null)
        {
            return (Provider)this.globalRoleProvider;
        }
        return (Provider)this.getProvider(this.wm.getWorldId(world));
    }

    public File getRolesFolder()
    {
        return rolesFolder;
    }

    public GlobalRoleProvider getGlobalProvider()
    {
        return this.globalRoleProvider;
    }

    public void reloadAllRoles()
    {
        for (RoleProvider roleProvider : providerSet)
        {
            roleProvider.loadConfigurations();
            roleProvider.reloadRoles();
        }
    }

    public void recalculateAllRoles()
    {
        this.module.getLog().log(LogLevel.DEBUG,"Calculating all roles...");
        Profiler.startProfiling("calculateAllRoles");
        for (RoleProvider roleProvider : providerSet)
        {
            roleProvider.recalculateRoles();
        }
        for (User user : this.module.getCore().getUserManager().getLoadedUsers())
        {
            RolesAttachment rolesAttachment = user.get(RolesAttachment.class);
            if (rolesAttachment == null)
            {
                if (!user.isOnline())
                {
                    continue;
                }
                rolesAttachment = user.attachOrGet(RolesAttachment.class,this.module);
            }
            rolesAttachment.flushResolvedData();
            if (user.isOnline())
            {
                rolesAttachment.getResolvedData(); // recalculates data
                rolesAttachment.apply(); // and applies
            }
        }
        this.module.getLog().log(LogLevel.DEBUG,"All roles are now calculated! ("+Profiler.endProfiling("calculateAllRoles", TimeUnit.MILLISECONDS)+"ms)");
    }

    public RolesAttachment getRolesAttachment(Player player)
    {
        User user;
        if (player instanceof User)
        {
            user = (User)player;
        }
        else
        {
            user = this.module.getCore().getUserManager().getExactUser(player);
        }
        return user.attachOrGet(RolesAttachment.class, this.module);
    }

    public void reapplyAllRoles()
    {
        for (User user : this.module.getCore().getUserManager().getLoadedUsers())
        {
            RolesAttachment rolesAttachment = user.get(RolesAttachment.class);
            if (rolesAttachment == null)
            {
                if (!user.isOnline())
                {
                    continue;
                }
                rolesAttachment = this.getRolesAttachment(user);
            }
            rolesAttachment.flushResolvedData();
            rolesAttachment.apply();
        }
    }

    /**
     * Sets the worlds to mirror world.
     * And then loads the newly created provider/s
     *
     * @param world to world to mirror from
     * @param roles if true mirror roles defined in the configurations
     * @param assignedRoles if true mirror roles assigned to users too
     * @param assignedData if true mirror data assigned to users too
     * @param worlds the worlds to mirror to
     */
    public void setMirror(World world, boolean roles, boolean assignedRoles, boolean assignedData, Collection<World> worlds)
    {
        RolesConfig config = this.module.getConfiguration();
        Iterator<RoleMirror> iterator = config.mirrors.iterator();
        RoleMirror next = null;
        while (iterator.hasNext())
        {
            next = iterator.next();
            if (next.mainWorld.equalsIgnoreCase(world.getName()))
            {
                config.mirrors.remove(next);
                break;
            }
        }
        if (next != null)
        {
            // Delete old provider
            WorldRoleProvider provider = this.getProvider(world);
            this.providerSet.remove(provider);
            for (long m : provider.getMirroredWorlds())
            {
                this.worldRoleProviders.remove(m);
            }
        }
        RoleMirror roleMirror = new RoleMirror(this.module, world.getName());
        for (World mWorld : worlds)
        {
            // Remove all providers of related worlds
            WorldRoleProvider provider = this.getProvider(mWorld);
            if (provider != null)
            {
                this.providerSet.remove(provider);
                for (long m : provider.getMirroredWorlds())
                {
                    this.worldRoleProviders.remove(m);
                }
            }
            roleMirror.setWorld(mWorld.getName(), roles, assignedRoles, assignedData);
        }
        config.mirrors.add(roleMirror);
        config.save();

        for (RoleMirror mirror : config.mirrors)
        {
            long id = this.wm.getWorldId(mirror.mainWorld);
            if (!this.worldRoleProviders.keySet().contains(id))
            {
                WorldRoleProvider provider = new WorldRoleProvider(module, this, mirror, id);
                this.registerWorldRoleProvider(provider);
            }
        }

        this.createMissingWorldRoleProviders();
        this.reloadAllRoles();
        this.recalculateAllRoles();
    }

    /**
     * Removes a mirror from the world
     *
     * @param world
     */
    public void removeMirror(World world, Collection<World> toRemove)
    {
        WorldRoleProvider provider = this.getProvider(world);
        TLongHashSet mirrored = new TLongHashSet(provider.getMirroredWorlds());
        for (World remWorld : toRemove)
        {
            long worldID = this.wm.getWorldId(remWorld);
            if (mirrored.contains(worldID))
            {
                RoleMirror mirrorConfig = provider.getMirrorConfig();
                mirrorConfig.getWorldMirrors().remove(worldID);
                this.worldRoleProviders.remove(worldID);
            }
        }
        this.module.getConfiguration().save();

        this.createMissingWorldRoleProviders();
        this.reloadAllRoles();
        this.recalculateAllRoles();
    }

    /**
     * Deletes the mirrors from the world
     *
     * @param world
     */
    public void deleteMirror(World world)
    {
        WorldRoleProvider provider = this.getProvider(world);
        this.providerSet.remove(provider);
        for (long w : provider.getMirroredWorlds())
        {
            this.worldRoleProviders.remove(w);
        }
        this.module.getConfiguration().mirrors.remove(provider.getMirrorConfig());
        this.module.getConfiguration().save();

        this.createMissingWorldRoleProviders();
        this.reloadAllRoles();
        this.recalculateAllRoles();
    }
}
