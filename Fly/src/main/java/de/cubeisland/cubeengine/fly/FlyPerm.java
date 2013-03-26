package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

import org.bukkit.permissions.Permissible;

import java.util.Locale;

public class FlyPerm extends PermissionContainer
{
    public FlyPerm(Module module)
    {
        super(module);
        this.registerAllPermissions();
    }

    private static final Permission FLY = BASEPERM.createAbstractChild("fly");
    public static final Permission FLY_FEATHER = FLY.createChild("feather");

}
