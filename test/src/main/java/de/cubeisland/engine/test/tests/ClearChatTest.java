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

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;

public class ClearChatTest extends Test
{
    private final de.cubeisland.engine.test.Test module;
    private static final int MAX_CHAT_LINES = 100;

    public ClearChatTest(de.cubeisland.engine.test.Test module)
    {
        this.module = module;
    }

    @Override
    public void onEnable()
    {
        module.getCore().getCommandManager().registerCommands(module, this, ReflectedCommand.class);
        this.setSuccess(true);
    }

    @Command(alias = "cls", desc = "Clears the chat")
    public void clearscreen(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            for (int i = 0; i < MAX_CHAT_LINES; ++i)
            {
                context.sendMessage(" ");
            }
        }
        else
        {
            context.sendMessage(ChatFormat.RED + "You better don't do this.");
        }
    }
}
