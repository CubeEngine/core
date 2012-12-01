package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.roles.Roles;
import gnu.trove.map.hash.THashMap;
import java.io.File;

public class RoleConfigManager
{
    private THashMap<String, RoleConfig> configs = new THashMap<String, RoleConfig>();
    private THashMap<String, RoleConfig> globalConfigs = new THashMap<String, RoleConfig>();
    private final File roleFolder;
    private final File globalRoleFolder;

    public RoleConfigManager(Roles rolesModule)
    {
        /**
         * Configuration.load(RoleConfig.class, new File(this.getFolder(),
         * "guest.yml")); Configuration.load(RoleConfig.class, new
         * File(this.getFolder(), "member.yml"));
         * Configuration.load(RoleConfig.class, new File(this.getFolder(),
         * "moderator.yml")); Configuration.load(RoleConfig.class, new
         * File(this.getFolder(), "admin.yml"));
         */
        this.roleFolder = new File(rolesModule.getFolder(), "roles");
        this.globalRoleFolder = new File(rolesModule.getFolder(), "globalroles");
        this.roleFolder.mkdir();
        this.globalRoleFolder.mkdir();
        rolesModule.getLogger().debug("Loading global roles...");
        for (File file : globalRoleFolder.listFiles())
        {
            if (file.getName().endsWith(".yml"))
            {
                RoleConfig config = Configuration.load(RoleConfig.class, file);
                this.globalConfigs.put(config.roleName, config);
            }
        }

        rolesModule.getLogger().debug("Loading roles...");
        for (File file : roleFolder.listFiles())
        {
            if (file.getName().endsWith(".yml"))
            {
                RoleConfig config = Configuration.load(RoleConfig.class, file);
                this.configs.put(config.roleName, config);
            }
        }


    }
}
