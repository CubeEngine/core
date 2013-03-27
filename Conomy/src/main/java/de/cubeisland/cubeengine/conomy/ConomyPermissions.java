package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

import org.bukkit.permissions.Permissible;

import java.util.Locale;

import static de.cubeisland.cubeengine.core.permission.PermDefault.OP;

public class ConomyPermissions extends PermissionContainer
{
    public ConomyPermissions(Module module)
    {
        super(module);
    }

    private static final Permission CONOMY = Permission.BASE.createAbstractChild("conomy");
    private static final Permission ACCOUNT = CONOMY.createAbstractChild("account");

    public static final Permission ACCOUNT_ALLOWUNDERMIN = ACCOUNT.createChild("allowundermin");
    public static final Permission ACCOUNT_SHOWHIDDEN = ACCOUNT.createChild("showhidden");

    public static final Permission COMMAND_PAY_FORCE = CONOMY.createChild("command.pay.force");
}
