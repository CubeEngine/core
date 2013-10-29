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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.core.util.Triplet;
import de.cubeisland.engine.core.world.WorldManager;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.RolesConfig;
import de.cubeisland.engine.roles.config.RoleConfig;
import de.cubeisland.engine.roles.config.RoleMirror;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.jooq.DSLContext;

public class RolesManager
{
    protected Roles module;
    private WorldManager wm;

    private GlobalRoleProvider globalRoleProvider;
    private TLongObjectHashMap<WorldRoleProvider> worldRoleProviders;
    private Set<RoleProvider> providerSet;
    protected TLongLongHashMap assignedUserDataMirrors;
    protected TLongLongHashMap assignedRolesMirrors;

    private final Path rolesFolder;

    protected DSLContext dsl;

    public RolesManager(Roles module)
    {
        this.module = module;
        this.dsl = module.getCore().getDB().getDSL();
        this.wm = module.getCore().getWorldManager();
        this.rolesFolder = module.getFolder().resolve("roles");
    }

    public void initRoleProviders()
    {
        try
        {
            Files.createDirectories(this.rolesFolder);

            this.assignedUserDataMirrors = new TLongLongHashMap();
            this.assignedRolesMirrors = new TLongLongHashMap();

            this.globalRoleProvider = new GlobalRoleProvider(module,this);

            this.providerSet = new LinkedHashSet<>();
            this.providerSet.add(this.globalRoleProvider);

            this.createWorldRoleProviders(); // Create all WorldProviders according to their mirrors
            // Load In All Configurations & Create Role-Objects
            // AND Resolve Dependencies and Role-Data
            this.reloadAllRoles();
        }
        catch (IOException e)
        {
            this.module.getLog().error("Failed to initialize the role providers!");
            this.module.getLog().debug(e.getLocalizedMessage(), e);
        }
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
        this.worldRoleProviders = new TLongObjectHashMap<>();
        for (RoleMirror mirror : this.module.getConfiguration().mirrors)
        {
            Long mainWorldID = wm.getWorldId(mirror.mainWorld);
            if (mainWorldID == null)
            {
                this.module.getLog().warn("Ignoring unknown world: {}", mirror.mainWorld);
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
        this.module.getLog().debug("Loading role-provider for {}", provider.getMainWorld());
        for (long worldId : worldMirrors.keys())
        {
            if (this.worldRoleProviders.containsKey(worldId))
            {
                this.module.getLog().error("The world {} is mirrored multiple times! Check your configuration under mirrors.{}",
                                           this.module.getCore().getWorldManager().getWorld(worldId).getName(), provider.getMainWorld());
                continue;
            }
            this.module.getLog().debug("  {}:", wm.getWorld(worldId).getName());
            if (worldMirrors.get(worldId).getFirst()) // Roles are mirrored add to provider...
            {
                this.module.getLog().debug("   - roles are mirrored");
                this.worldRoleProviders.put(worldId, provider);
                this.providerSet.add(provider);
            }
            if (worldMirrors.get(worldId).getSecond())
            {
                this.module.getLog().debug("   - assigned roles are mirrored");
                this.assignedRolesMirrors.put(worldId, provider.getMainWorldId());
            }
            else
            {
                this.assignedRolesMirrors.put(worldId, worldId);
            }
            if (worldMirrors.get(worldId).getThird()) // specific user perm/metadata is mirrored
            {
                this.module.getLog().debug("   - assigned user-data is mirrored");
                this.assignedUserDataMirrors.put(worldId, provider.getMainWorldId());
            }
            else
            {
                this.assignedUserDataMirrors.put(worldId, worldId);
            }
        }
    }

    private void createMissingWorldRoleProviders()
    {
        for (long worldId : this.module.getCore().getWorldManager().getAllWorldIds().toArray())
        {
            if (this.worldRoleProviders.get(worldId) == null)
            {
                WorldRoleProvider provider = new WorldRoleProvider(module, this, worldId);
                this.worldRoleProviders.put(worldId, provider);
                this.providerSet.add(provider);
                if (assignedRolesMirrors.get(worldId) == 0)
                {
                    this.assignedRolesMirrors.put(worldId, worldId);
                }
                if (assignedUserDataMirrors.get(worldId) == 0)
                {
                    this.assignedUserDataMirrors.put(worldId, worldId);
                }
                this.module.getLog().debug("Loading role-provider without mirror: {}", wm.getWorld(worldId).getName());
            }
        }
    }

    public WorldRoleProvider getProvider(long worldId)
    {
        WorldRoleProvider worldRoleProvider = this.worldRoleProviders.get(worldId);
        if (worldRoleProvider == null)
        {
            if (this.wm.getWorld(worldId) != null) // make sure world exists
            {
                this.module.getLog().warn("No RoleProvider for {}! Reloading...", this.wm.getWorld(worldId).getName());
                this.module.getConfiguration().reload();
                this.module.getRolesManager().initRoleProviders();
                this.module.getRolesManager().recalculateAllRoles();
            }
            // else return null;
        }
        return worldRoleProvider;
    }

    @SuppressWarnings("unchecked")
    public <Provider extends RoleProvider> Provider getProvider(World world)
    {
        if (world == null)
        {
            return (Provider)this.globalRoleProvider;
        }
        return (Provider)this.getProvider(this.wm.getWorldId(world));
    }

    public Path getRolesFolder()
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
        this.module.getLog().debug("Calculating all roles...");
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
            rolesAttachment.reload();
            if (user.isOnline())
            {
                rolesAttachment.getCurrentResolvedData(); // recalculates data
                rolesAttachment.apply(); // and applies
            }
        }
        this.module.getLog().debug("All roles are now calculated! ({} ms)", Profiler.endProfiling("calculateAllRoles", TimeUnit.MILLISECONDS));
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
            user = this.module.getCore().getUserManager().getExactUser(player.getName());
        }
        return user.attachOrGet(RolesAttachment.class, this.module);
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
