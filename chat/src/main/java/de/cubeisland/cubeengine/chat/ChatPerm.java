package de.cubeisland.cubeengine.chat;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

import org.bukkit.permissions.Permissible;

import java.util.Locale;

public class ChatPerm extends PermissionContainer
{
    public ChatPerm(Module module)
    {
        super(module);
        this.registerAllPermissions();
    }

    private static final Permission CHAT = Permission.BASE.createAbstractChild("chat");
    public static final Permission COLOR = CHAT.createChild("color");
}
