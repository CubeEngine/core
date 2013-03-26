package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

import static de.cubeisland.cubeengine.core.permission.PermDefault.FALSE;

public class CorePerms extends PermissionContainer
{
    public CorePerms(Module module)
    {
        super(module);
    }

    private static final Permission CORE = BASEPERM.createAbstractChild("core");
    private static final Permission COMMAND = CORE.createAbstractChild("command");
    private static final Permission CLEARPASSWORD = COMMAND.createAbstractChild("clearpassword");
    public static final Permission COMMAND_CLEARPASSWORD_ALL = CLEARPASSWORD.createChild("all");
    public static final Permission COMMAND_CLEARPASSWORD_OTHER = CLEARPASSWORD.createChild("other");

    public static final Permission COMMAND_SETPASSWORD_OTHER = COMMAND.createChild("other");
    public static final Permission COMMAND_OP_NOTIFY = COMMAND.createChild("op.notify");

    private static final Permission DEOP = COMMAND.createAbstractChild("deop");
    public static final Permission COMMAND_DEOP_NOTIFY = DEOP.createChild("notify");
    public static final Permission COMMAND_DEOP_OTHER = DEOP.createChild("other",FALSE);

    public static final Permission COMMAND_VERSION_PLUGINS = COMMAND.createChild("version.plugins");

    public static final Permission SPAM = CORE.createChild("spam");
}
