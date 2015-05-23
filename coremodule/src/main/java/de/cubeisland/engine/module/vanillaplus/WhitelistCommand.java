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
package de.cubeisland.engine.module.vanillaplus;

import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.filter.Restricted;
import de.cubeisland.engine.butler.parametric.Command;
import de.cubeisland.engine.module.service.command.CommandSender;
import de.cubeisland.engine.module.service.command.ContainerCommand;
import de.cubeisland.engine.module.service.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.service.user.User;
import org.spongepowered.api.data.manipulator.entity.WhitelistData;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.NEUTRAL;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.POSITIVE;

@Command(name = "whitelist", desc = "Allows you to manage your whitelist")
public class WhitelistCommand extends ContainerCommand
{
    private final CoreModule core;

    public WhitelistCommand(CoreModule core)
    {
        super(core);
        this.core = core;
    }

    @Override
    protected boolean selfExecute(CommandInvocation invocation)
    {
        if (invocation.isConsumed())
        {
            return this.getCommand("list").execute(invocation);
        }
        else if (invocation.tokens().size() - invocation.consumed() == 1)
        {
            return this.getCommand("add").execute(invocation);
        }
        return super.execute(invocation);
    }

    @Command(desc = "Adds a player to the whitelist.")
    public void add(CommandSender context, User player)
    {
        if (player.getData(WhitelistData.class).isPresent())
        {
            context.sendTranslated(NEUTRAL, "{user} is already whitelisted.", player);
            return;
        }
        player.offer(core.getGame().getRegistry().getBuilderOf(WhitelistData.class).get());
        context.sendTranslated(POSITIVE, "{user} is now whitelisted.", player);
    }

    @Command(alias = "rm", desc = "Removes a player from the whitelist.")
    public void remove(CommandSender context, User player)
    {
        if (!player.getData(WhitelistData.class).isPresent())
        {
            context.sendTranslated(NEUTRAL, "{user} is not whitelisted.", player);
            return;
        }
        player.getOfflinePlayer().remove(WhitelistData.class);
        context.sendTranslated(POSITIVE, "{user} is not whitelisted anymore.", player.getName());
    }

    @Command(desc = "Lists all the whitelisted players")
    public void list(CommandSender context)
    {
        /* TODO
        Set<org.spongepowered.api.entity.player.User> whitelist = this.core.getGame().getServer().getWhitelistedPlayers();
        if (!this.core.getGame().getServer().hasWhitelist())
        {
            context.sendTranslated(NEUTRAL, "The whitelist is currently disabled.");
        }
        else
        {
            context.sendTranslated(POSITIVE, "The whitelist is enabled!.");
        }
        context.sendMessage(" ");
        if (whitelist.isEmpty())
        {
            context.sendTranslated(NEUTRAL, "There are currently no whitelisted players!");
        }
        else
        {
            context.sendTranslated(NEUTRAL, "The following players are whitelisted:");
            for (org.spongepowered.api.entity.player.User player : whitelist)
            {
                context.sendMessage(" - " + player.getName());
            }
        }
        Set<org.spongepowered.api.entity.player.User> operators = this.core.getGame().getServer().getOperators();
        if (!operators.isEmpty())
        {
            context.sendTranslated(NEUTRAL, "The following players are OP and can bypass the whitelist");
            for (org.spongepowered.api.entity.player.User operator : operators)
            {
                context.sendMessage(" - " + operator.getName());
            }
        }
        */
    }

    @Command(desc = "Enables the whitelisting")
    public void on(CommandSender context)
    {
        if (this.core.getGame().getServer().hasWhitelist())
        {
            context.sendTranslated(NEGATIVE, "The whitelist is already enabled!");
            return;
        }
        this.core.getGame().getServer().setHasWhitelist(true);
        context.sendTranslated(POSITIVE, "The whitelist is now enabled.");
    }

    @Command(desc = "Disables the whitelisting")
    public void off(CommandSender context)
    {
        if (!this.core.getGame().getServer().hasWhitelist())
        {
            context.sendTranslated(NEGATIVE, "The whitelist is already disabled!");
            return;
        }
        this.core.getGame().getServer().setHasWhitelist(false);
        context.sendTranslated(POSITIVE, "The whitelist is now disabled.");
    }

    @Command(desc = "Wipes the whitelist completely")
    @Restricted(value = ConsoleCommandSender.class, msg = "This command is too dangerous for users!")
    public void wipe(CommandSender context)
    {
        // TODO wipe whitelist
        context.sendTranslated(POSITIVE, "The whitelist was successfully wiped!");
    }
}
