package de.cubeisland.cubeengine.roles;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.roles.role.config.RoleMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Codec("yml")
@DefaultConfig
public class RolesConfig extends Configuration
{
    @Option("default.roles")
    @Comment("The list of roles a user will get when first joining the server.")
    public Map<String,List<String>> defaultRoles = new HashMap<String, List<String>>();
    @Option("mirrors")
    public List<RoleMirror> mirrors = new ArrayList<RoleMirror>();
}
