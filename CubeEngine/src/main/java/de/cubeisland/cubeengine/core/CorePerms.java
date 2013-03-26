package de.cubeisland.cubeengine.core;

import java.util.Locale;

import org.bukkit.permissions.Permissible;

import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;

import static de.cubeisland.cubeengine.core.permission.PermDefault.FALSE;
import static de.cubeisland.cubeengine.core.permission.PermDefault.OP;

public enum CorePerms implements Permission
{
    SPAM,
    COMMAND_CLEARPASSWORD_ALL,
    COMMAND_CLEARPASSWORD_OTHER,
    COMMAND_SETPASSWORD_OTHER,
    COMMAND_OP_NOTIFY,
    COMMAND_DEOP_NOTIFY,
    COMMAND_DEOP_OTHER(FALSE),
    COMMAND_VERSION_PLUGINS,
    COMMAND_RELOAD_NOTIFY(PermDefault.TRUE);

    private String permission;
    private PermDefault def;
    private static final String BASE = "cubeengine.core.";

    private CorePerms()
    {
        this(OP);
    }

    private CorePerms(PermDefault def)
    {
        this.permission = BASE + this.name().toLowerCase(Locale.ENGLISH).replace('_', '.');
        this.def = def;
    }

    @Override
    public boolean isAuthorized(Permissible player)
    {
        return player.hasPermission(this.permission);
    }

    @Override
    public String getPermission()
    {
        return this.permission;
    }

    @Override
    public PermDefault getPermissionDefault()
    {
        return this.def;
    }
}
