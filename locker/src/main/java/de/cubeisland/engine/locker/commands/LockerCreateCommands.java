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
package de.cubeisland.engine.locker.commands;

import de.cubeisland.engine.locker.Locker;
import de.cubeisland.engine.locker.commands.CommandListener.CommandType;
import de.cubeisland.engine.locker.storage.LockManager;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;

import static de.cubeisland.engine.locker.commands.CommandListener.CommandType.*;
import static de.cubeisland.engine.locker.commands.LockerCommands.isNotUser;

public class LockerCreateCommands extends ContainerCommand
{
    private final LockManager manager;

    public LockerCreateCommands(Locker module, LockManager manager)
    {
        super(module, "create", "Creates various protections");
        this.manager = manager;
    }

    private void setCreateProtection(CommandSender sender, CommandType type, String password, boolean createKeyBook)
    {
        this.manager.commandListener.setCommandType(sender, type, password, createKeyBook);
        if (createKeyBook)
        {
            sender.sendTranslated("&aRightclick what you want to protect with a book in your hand!");
        }
        else
        {
            sender.sendTranslated("&aRightclick what you want to protect!");
        }
    }

    @Alias(names = "cprivate")
    @Command(names = "private",
    desc = "creates a private protection", max = 1, usage = "[password]",
    flags = @Flag(name = "key", longName = "keybook"))
    public void cPrivate(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        this.setCreateProtection(context.getSender(), C_PRIVATE, context.getString(0), context.hasFlag("key"));
    }

    @Alias(names = "cpublic")
    @Command(names = "public",
             desc = "creates a public protection")
    public void cPublic(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        this.setCreateProtection(context.getSender(), C_PUBLIC, null, false);
    }

    @Alias(names = "cdonation")
    @Command(names = "donation",
             desc = "creates a donation protection", max = 1, usage = "[password]",
             flags = @Flag(name = "key", longName = "keybook"))
    public void cDonation(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        this.setCreateProtection(context.getSender(), C_DONATION, context.getString(0), context.hasFlag("key"));
    }

    @Alias(names = "cfree")
    @Command(names = "free",
             desc = "creates a free protection", max = 1, usage = "[password]",
             flags = @Flag(name = "key", longName = "keybook"))
    public void cFree(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        this.setCreateProtection(context.getSender(), C_FREE, context.getString(0), context.hasFlag("key"));
    }

    @Alias(names = "cpassword")
    @Command(names = "password",
             desc = "creates a donation protection", min = 1, max = 1, usage = "<password>",
             flags = @Flag(name = "key", longName = "keybook"))
    public void cPassword(ParameterizedContext context) // same as private but with pw
    {
        if (isNotUser(context.getSender())) return;
        this.setCreateProtection(context.getSender(), C_PRIVATE, context.getString(0), context.hasFlag("key"));
    }

    @Alias(names = "cguarded")
    @Command(names = "guarded",
             desc = "creates a guarded protection", min = 0, max = 1, usage = "[password]",
             flags = @Flag(name = "key", longName = "keybook"))
    public void cguarded(ParameterizedContext context) // same as private but with pw
    {
        if (isNotUser(context.getSender())) return;
        this.setCreateProtection(context.getSender(), C_GUARDED, context.getString(0), context.hasFlag("key"));
    }
}
