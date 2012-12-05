package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public List<String> parents = new ArrayList<String>();
    @Option("metadata")
    @Comment("such as prefix / suffix")
    public Map<String, String> metadata = new LinkedHashMap<String, String>();

    @Override
    public void onLoaded()
    {
        if (this.priority == null)
        {
            this.priority = Priority.ABSULTEZERO;
        }
        if (this.parents == null)
        {
            this.parents = new ArrayList<String>();
        }
        if (this.metadata == null)
        {
            this.metadata = new LinkedHashMap<String, String>();
        }
    }
}
