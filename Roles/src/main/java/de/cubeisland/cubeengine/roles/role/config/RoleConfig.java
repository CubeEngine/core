package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Codec("yml")
@DefaultConfig
public class RoleConfig extends Configuration
{
    @Option("role-name")
    @Comment("The name of this role")
    public String roleName = "defaultName";
    @Option("priority")
    @Comment("Use these as priority or just numbers\n"
        + "ABSULTEZERO(-273) < MINIMUM(0) < LOWEST(125) < LOWER(250) < LOW(375) < NORMAL(500) < HIGH(675) < HIGHER(750) < HIGHEST(1000) < OVER9000(9001)")
    public Priority priority = Priority.ABSULTEZERO;
    @Option("permissions")
    @Comment("the permissions")
    public PermissionTree perms = new PermissionTree();
    @Option("parents")
    @Comment("The roles this role will inherit from.\n"
        + "Any priority of parents will be ignored!")
    public Set<String> parents = new HashSet<String>();
    @Option("metadata")
    @Comment("such as prefix / suffix")
    public Map<String, String> metadata = new LinkedHashMap<String, String>();

    @Override
    public void onLoaded(File loadFrom) {
        if (this.priority == null)
        {
            this.priority = Priority.ABSULTEZERO;
        }
        if (this.parents == null)
        {
            this.parents = new HashSet<String>();
        }
        if (this.metadata == null)
        {
            this.metadata = new LinkedHashMap<String, String>();
        }
    }
}
