package de.cubeisland.cubeengine.roles;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.roles.config.RoleMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Codec("yml")
@DefaultConfig
public class RolesConfig extends Configuration
{
    @Option("disable-permission-in-offlinemode")
    @Comment("If this is set to true no permissions will be assigned to any user if the server runs in offline-mode")
    public boolean doNotAssignPermIfOffline = true;
    @Option("default.roles")
    @Comment("The list of roles a user will get when first joining the server.")
    public Map<String, List<String>> defaultRoles = new HashMap<String, List<String>>();
    @Option("mirrors")
    public List<RoleMirror> mirrors = new ArrayList<RoleMirror>();
}
