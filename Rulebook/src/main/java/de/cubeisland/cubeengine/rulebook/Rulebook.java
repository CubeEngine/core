package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;
import de.cubeisland.cubeengine.rulebook.bookManagement.RulebookCommands;
import de.cubeisland.cubeengine.rulebook.bookManagement.RulebookManager;

public class Rulebook extends Module
{
    private RulebookManager rulebookManager;

    @Override
    public void onEnable()
    {
        this.getFileManager().dropResources(RulebookResource.values());
        Permission perm = Permission.BASE.
            createAbstractChild(this.getId()).
            createAbstractChild("command").
            createAbstractChild("get").
              createChild("other");
        this.getCore().getPermissionManager().registerPermission( this, perm);

        this.rulebookManager = new RulebookManager(this);

        this.registerCommand(new RulebookCommands(this));
        this.registerListener(new RulebookListener(this));
    }

    public RulebookManager getRuleBookManager()
    {
        return this.rulebookManager;
    }
}
