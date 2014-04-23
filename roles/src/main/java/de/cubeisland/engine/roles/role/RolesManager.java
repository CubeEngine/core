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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.core.util.Triplet;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.RolesConfig;
import de.cubeisland.engine.roles.config.MirrorConfig;
import de.cubeisland.engine.roles.config.RoleConfig;
import org.jooq.DSLContext;

public class RolesManager
{
    protected final Roles module;
    private final Path rolesFolder;
    protected final DSLContext dsl;

    private GlobalRoleProvider globalRoleProvider;

    private Map<World, WorldRoleProvider> worldRoleProviders = new HashMap<>();
    private Set<RoleProvider> providerSet = new LinkedHashSet<>();

    protected Map<World, World> assignedUserDataMirrors = new HashMap<>();
    protected Map<World, World> assignedRolesMirrors = new HashMap<>();

    public RolesManager(Roles module)
    {
        this.module = module;
        this.dsl = module.getCore().getDB().getDSL();
        this.rolesFolder = module.getFolder().resolve("roles");
    }

    public void initRoleProviders()
    {
        try
        {
            Files.createDirectories(this.rolesFolder);

            this.assignedUserDataMirrors = new HashMap<>();
            this.assignedRolesMirrors = new HashMap<>();

            this.globalRoleProvider = new GlobalRoleProvider(this);

            this.providerSet = new LinkedHashSet<>();
            this.providerSet.add(this.globalRoleProvider);

            this.createWorldRoleProviders(); // Create all WorldProviders according to their mirrors
            // Load In All Configurations & Create Role-Objects
            // AND Resolve Dependencies and Role-Data
            this.reloadAllRoles();
        }
        catch (IOException ex)
        {
            this.module.getLog().error(ex, "Failed to initialize the role providers!");
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
        this.worldRoleProviders = new HashMap<>();
        for (MirrorConfig mirror : this.module.getConfiguration().mirrors)
        {
            WorldRoleProvider provider = new WorldRoleProvider(this, mirror);
            this.registerWorldRoleProvider(provider); // mirrors
        }
        this.createMissingWorldRoleProviders();// World without mirrors
    }

    private void registerWorldRoleProvider(WorldRoleProvider provider)
    {
        Map<World, Triplet<Boolean, Boolean, Boolean>> worldMirrors = provider.getWorldMirrors();
        this.module.getLog().debug("Loading role-provider for {}", provider.getMainWorld().getName());
        for (World world : worldMirrors.keySet())
        {
            if (this.worldRoleProviders.containsKey(world))
            {
                this.module.getLog().error("The world {} is mirrored multiple times! Check your configuration under mirrors.{}",
                                           world.getName(), provider.getMainWorld().getName());
                continue;
            }
            this.module.getLog().debug("  {}:", world.getName());
            if (worldMirrors.get(world).getFirst()) // Roles are mirrored add to provider...
            {
                this.module.getLog().debug("   - roles are mirrored");
                this.worldRoleProviders.put(world, provider);
                this.providerSet.add(provider);
            }
            if (worldMirrors.get(world).getSecond())
            {
                this.module.getLog().debug("   - assigned roles are mirrored");
                this.assignedRolesMirrors.put(world, provider.getMainWorld());
            }
            else
            {
                this.assignedRolesMirrors.put(world, world);
            }
            if (worldMirrors.get(world).getThird()) // specific user perm/metadata is mirrored
            {
                this.module.getLog().debug("   - assigned user-data is mirrored");
                this.assignedUserDataMirrors.put(world, provider.getMainWorld());
            }
            else
            {
                this.assignedUserDataMirrors.put(world, world);
            }
        }
    }

    private void createMissingWorldRoleProviders()
    {
        for (World world : this.module.getCore().getWorldManager().getWorlds())
        {
            if (this.worldRoleProviders.get(world) == null)
            {
                WorldRoleProvider provider = new WorldRoleProvider(this, world);
                this.worldRoleProviders.put(world, provider);
                this.providerSet.add(provider);
                if (assignedRolesMirrors.get(world) == null)
                {
                    this.assignedRolesMirrors.put(world, world);
                }
                if (assignedUserDataMirrors.get(world) == null)
                {
                    this.assignedUserDataMirrors.put(world, world);
                }
                this.module.getLog().debug("Loading role-provider without mirror: {}", world.getName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public WorldRoleProvider getProvider(World world)
    {
        WorldRoleProvider worldRoleProvider = this.worldRoleProviders.get(world);
        if (worldRoleProvider == null)
        {
            this.module.getLog().warn("No RoleProvider for {}! Reloading...", world.getName());
            this.module.getConfiguration().reload();
            this.module.getRolesManager().initRoleProviders();
            this.module.getRolesManager().recalculateAllRoles();
            worldRoleProvider = this.worldRoleProviders.get(world);
        }
        return worldRoleProvider;

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
                rolesAttachment.getCurrentDataHolder().apply(); // recalculates data + apply
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
            user = this.module.getCore().getUserManager().getExactUser(player.getUniqueId());
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
        Iterator<MirrorConfig> iterator = config.mirrors.iterator();
        MirrorConfig next = null;
        while (iterator.hasNext())
        {
            next = iterator.next();
            if (next.mainWorld.equals(world))
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
            for (World m : provider.getMirroredWorlds())
            {
                this.worldRoleProviders.remove(m);
            }
        }
        MirrorConfig roleMirror = new MirrorConfig(module, world);
        for (World mWorld : worlds)
        {
            // Remove all providers of related worlds
            WorldRoleProvider provider = this.getProvider(mWorld);
            if (provider != null)
            {
                this.providerSet.remove(provider);
                for (World m : provider.getMirroredWorlds())
                {
                    this.worldRoleProviders.remove(m);
                }
            }
            roleMirror.setWorld(mWorld, roles, assignedRoles, assignedData);
        }
        config.mirrors.add(roleMirror);
        config.save();

        for (MirrorConfig mirror : config.mirrors)
        {
            if (!this.worldRoleProviders.keySet().contains(mirror.mainWorld))
            {
                this.registerWorldRoleProvider(new WorldRoleProvider(this, mirror));
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
        for (World remWorld : toRemove)
        {
            if (provider.getMirroredWorlds().contains(remWorld))
            {
                MirrorConfig mirrorConfig = provider.getMirrorConfig();
                mirrorConfig.getWorldMirrors().remove(remWorld);
                this.worldRoleProviders.remove(remWorld);
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
        for (World w : provider.getMirroredWorlds())
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
