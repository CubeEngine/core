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
package de.cubeisland.engine.cguard.commands;

import de.cubeisland.engine.cguard.Cguard;
import de.cubeisland.engine.cguard.storage.GuardManager;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.cguard.commands.CommandListener.CommandType.C_PRIVATE;

public class GuardCreateCommands extends ContainerCommand
{
    private GuardManager manager;

    public GuardCreateCommands(Cguard module, GuardManager manager)
    {
        super(module, "create", "Creates various protections");
        this.manager = manager;
    }

// TODO pass parameter for creating
    // flag for creating key-book -> can open chest when in hand

    @Alias(names = "cprivate")
    @Command(names = "private",
    desc = "creates a private protection")
    public void cPrivate(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated("&cThis command can only be used ingame");
            return;
        }
        this.manager.commandListener.setCommandType((User)context.getSender(), C_PRIVATE);
        context.sendTranslated("&aRightclick the block to protect!");
    }

    public void cPublic(CommandContext context)
    {

    }

    public void cDonation(CommandContext context)
    {

    }

    public void cFree(CommandContext context)
    {

    }



    public void cPassword(CommandContext context) // same as private but with pw
    {

    }


}
