/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
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
