package de.cubeisland.engine.kits;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;

public class KitsPerm extends PermissionContainer<Kits>
{
    public KitsPerm(Kits module)
    {
        super(module);
        
        bindToModule(COMMAND, KITS);
        
        this.registerAllPermissions();
    }

    public static final Permission COMMAND = Permission.createAbstractPermission("command");
    public static final Permission KITS = Permission.createAbstractPermission("kits");
    public static final Permission COMMAND_KIT_GIVE_FORCE = COMMAND.createChild("kit.give.force");
}
