package de.cubeisland.cubeengine.roles;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.roles.role.RoleProvider;
import java.util.ArrayList;
import java.util.List;

@Codec("yml")
@DefaultConfig
public class RolesConfig extends Configuration
{
    @Option("default.roles")
    @Comment("The list of roles a user will get when first joining the server.")
    public List<String> defaultRoles;
    //TODO @Option("mirrors")
    public List<RoleProvider> providers = new ArrayList<RoleProvider>();
}
