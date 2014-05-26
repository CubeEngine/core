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

import java.util.Map.Entry;

import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.command.reflected.context.Flag;
import de.cubeisland.engine.core.command.reflected.context.Flags;
import de.cubeisland.engine.core.command.reflected.context.NParams;
import de.cubeisland.engine.core.command.reflected.context.Named;

public class CommandArgsTest extends Test
{
    private final de.cubeisland.engine.test.Test module;

    public CommandArgsTest(de.cubeisland.engine.test.Test module)
    {
        this.module = module;
    }

    @Override
    public void onEnable()
    {
        module.getCore().getCommandManager().registerCommands(module, this, ReflectedCommand.class);
        this.setSuccess(true);
    }

    @Command(desc = "This command prints out the args he gets")
    @Flags(@Flag(name = "a"))
    @NParams(@Named(names = "param"))
    public void testArgs(ParameterizedContext context)
    {
        context.sendMessage("Arg dump:");
        context.sendMessage(" ");

        for (Object arg : context.getArgs())
        {
            context.sendMessage("Arg: '" + arg + "'");
        }

        for (String flag : context.getFlags())
        {
            context.sendMessage("Flag: -" + flag);
        }

        for (Entry<String, Object> entry : context.getParams().entrySet())
        {
            context.sendMessage("Param: " + entry.getKey() + " => '" + entry.getValue().toString() + "'");
        }
    }
}
