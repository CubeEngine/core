package de.cubeisland.cubeengine.roles.role.newRole;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.config.RoleMirror;

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

    public RolesManager(Roles module)
    {
        this.module = module;
        this.wm = module.getCore().getWorldManager();
        this.rolesFolder = new File(module.getFolder(),"roles");
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
        for (RoleProvider roleProvider : providerSet)
        {
            roleProvider.loadConfigurations();
            roleProvider.reloadRoles();
        }
        // Resolve Dependencies and Role-Data
        for (RoleProvider roleProvider : providerSet)
        {
            roleProvider.recalculateRoles();
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

    public File getRolesFolder()
    {
        return rolesFolder;
    }

    public GlobalRoleProvider getGlobalProvider()
    {
        return this.globalRoleProvider;
    }
}
