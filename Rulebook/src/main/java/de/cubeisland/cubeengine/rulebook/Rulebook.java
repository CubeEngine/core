package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.rulebook.bookManagement.RulebookCommands;
import de.cubeisland.cubeengine.rulebook.bookManagement.RulebookManager;

public class Rulebook extends Module
{
    private RulebookManager rulebookManager;

    @Override
    public void onEnable()
    {
        this.getCore().getFileManager().dropResources(RulebookResource.values());
        this.getCore().getPermissionManager().registerPermission( this, Permission.BASE + '.' + this.getId() + ".command.get.other", PermDefault.OP );

        this.rulebookManager = new RulebookManager(this);

        this.getCore().getCommandManager().registerCommand(new RulebookCommands(this));
        this.getCore().getEventManager().registerListener(this, new RulebookListener(this));
    }

    public RulebookManager getRuleBookManager()
    {
        return this.rulebookManager;
    }
}
