package de.cubeisland.cubeengine.travel.interactions;

import java.util.HashMap;
import java.util.Map;

import de.cubeisland.cubeengine.core.command.ArgBounds;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.travel.Travel;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;

public class WarpAdminCommand extends ContainerCommand
{
    private static final Long ACCEPT_TIMEOUT = 20000l;

    private final Map<String, Pair<Long, ParameterizedContext>> acceptEntries;
    private final TelePointManager tpManager;
    private final Travel module;


    public WarpAdminCommand(Travel module)
    {
        super(module, "admin", "Teleport to another users home");
        this.module = module;
        this.tpManager = module.getTelepointManager();
        this.acceptEntries = new HashMap<String, Pair<Long, ParameterizedContext>>();

        this.setUsage("[User] [Home]");
        this.getContextFactory().setArgBounds(new ArgBounds(0, 2));
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {

        return null;
    }
}
