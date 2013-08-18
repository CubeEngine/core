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
package de.cubeisland.engine.test.tests;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.util.time.Duration;

public class ParseTimeTest extends Test
{
    private final de.cubeisland.engine.test.Test module;

    public ParseTimeTest(de.cubeisland.engine.test.Test module)
    {
        this.module = module;
    }

    @Override
    public void onEnable()
    {
        module.getCore().getCommandManager().registerCommands(module, this, ReflectedCommand.class);
        this.setSuccess(true);
    }

    @Command(desc = "Time-parsing")
    public void parsetime(CommandContext context)
    {
        LinkedList<String> list = new LinkedList<>();
        int i = 0;
        while (context.hasArg(i))
        {
            list.add(context.getString(i));
            i++;
        }
        Duration dura = new Duration(list.toArray(new String[0]));
        context.sendMessage("ms: " + dura.toTimeUnit(TimeUnit.MILLISECONDS));
        context.sendMessage(dura.format());
    }
}
