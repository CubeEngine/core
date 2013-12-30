package de.cubeisland.engine.powertools;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;

public class PowertoolsPerm extends PermissionContainer<Powertools>
{
    public PowertoolsPerm(Powertools module)
    {
        super(module);
        
        bindToModule(POWERTOOL_USE);
        
        this.registerAllPermissions();
    }

    public static final Permission POWERTOOL_USE = Permission.createPermission("powertool.use");
}
