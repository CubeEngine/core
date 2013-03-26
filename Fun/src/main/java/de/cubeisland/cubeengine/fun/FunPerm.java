package de.cubeisland.cubeengine.fun;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

import java.util.Locale;
import org.bukkit.permissions.Permissible;

public class FunPerm extends PermissionContainer
{
    public FunPerm(Module module)
    {
        super(module);
        this.registerAllPermissions();
    }

    private static final Permission FUN = BASEPERM.createAbstractChild("fun");
    private static final Permission COMMAND = FUN.createAbstractChild("command");

    private static final Permission COMMAND_EXPLOSION = COMMAND.createAbstractChild("explosion");
    public static final Permission COMMAND_EXPLOSION_OTHER = COMMAND_EXPLOSION.createChild("other");
    public static final Permission COMMAND_EXPLOSION_PLAYER_DAMAGE = COMMAND_EXPLOSION.createChild("player.damage");
    public static final Permission COMMAND_EXPLOSION_BLOCK_DAMAGE = COMMAND_EXPLOSION.createChild("block.damage");
    public static final Permission COMMAND_EXPLOSION_FIRE = COMMAND_EXPLOSION.createChild("fire");

    private static final Permission COMMAND_HAT = COMMAND.createAbstractChild("hat");
    public static final Permission COMMAND_HAT_OTHER = COMMAND_HAT.createChild("other");
    public static final Permission COMMAND_HAT_ITEM = COMMAND_HAT.createChild("item");
    public static final Permission COMMAND_HAT_QUIET = COMMAND_HAT.createChild("quit");
    public static final Permission COMMAND_HAT_NOTIFY = COMMAND_HAT.createChild("notify",PermDefault.TRUE);

    private static final Permission COMMAND_LIGHTNING = COMMAND.createAbstractChild("lightning");
    public static final Permission COMMAND_LIGHTNING_PLAYER_DAMAGE = COMMAND_LIGHTNING.createChild("player.damage");
    public static final Permission COMMAND_LIGHTNING_UNSAFE = COMMAND_LIGHTNING.createChild("unsafe");

    public static final Permission COMMAND_THROW = COMMAND.createAbstractChild("throw");
    public static final Permission COMMAND_THROW_UNSAFE = COMMAND_THROW.createChild("unsafe");
}
