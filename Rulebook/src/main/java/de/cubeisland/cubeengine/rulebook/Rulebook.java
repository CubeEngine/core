package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.rulebook.bookManagement.RulebookCommands;
import de.cubeisland.cubeengine.rulebook.bookManagement.RulebookManager;

public class Rulebook extends Module
{
    private RulebookManager rulebookManager;

    @Override
    public void onEnable()
    {
        this.getFileManager().dropResources(RulebookResource.values());
        this.getCore().getPermissionManager().registerPermission( this, "cubeengine.rulebook.command.get.other", PermDefault.OP );

        this.rulebookManager = new RulebookManager(this);

        this.registerCommand(new RulebookCommands(this));
        this.registerListener(new RulebookListener(this));
    }

    public RulebookManager getRuleBookManager()
    {
        return this.rulebookManager;
    }
}
