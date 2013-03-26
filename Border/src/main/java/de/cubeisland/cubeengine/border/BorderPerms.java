package de.cubeisland.cubeengine.border;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

import org.bukkit.permissions.Permissible;

import java.util.Locale;

public class BorderPerms extends PermissionContainer
{
    public BorderPerms(Module module)
    {
        super(module);
        this.registerAllPermissions();
    }

    private static final Permission BORDER = BASEPERM.createAbstractChild("border");
    public static final Permission BYPASS = BORDER.createChild("bypass");
}
