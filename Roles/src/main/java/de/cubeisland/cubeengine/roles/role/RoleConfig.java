package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import java.util.List;
import java.util.Map;

@Codec("yml")
@DefaultConfig
public class RoleConfig extends Configuration
{
    @Option("role-name")
    @Comment("The name of this role")
    public String roleName = "exampleName";
    
    @Option("priority")
    @Comment("Use these as priority or just numbers\n"
    + "ABSULTEZERO(-273) < MINIMUM(0) < LOWEST(125) < LOWER(250) < LOW(375) < NORMAL(500) < HIGH(675) < HIGHER(750) < HIGHEST(1000) < OVER9000(9001)")
    public Priority priority= Priority.ABSULTEZERO;
    
    @Option("permissions")
    @Comment("the permissions")
    public PermissionTree perms;
    
    @Option("parents")
    @Comment("The roles this role will inherit from.\n"
    + "Any priority of parents will be ignored!")
    public List<String> parents;
    
    @Option("metadata")
    @Comment("such as prefix / suffix")
    public Map<String, String> metadata;
}
