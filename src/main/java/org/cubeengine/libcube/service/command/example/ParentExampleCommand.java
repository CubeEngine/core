/*
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
package org.cubeengine.libcube.service.command.example;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.cubeengine.libcube.service.command.annotation.Command;
import org.cubeengine.libcube.service.command.annotation.Default;
import org.cubeengine.libcube.service.command.DefaultParameterProvider;
import org.cubeengine.libcube.service.command.DispatcherCommand;
import org.cubeengine.libcube.service.command.annotation.Flag;
import org.cubeengine.libcube.service.command.annotation.Named;
import org.cubeengine.libcube.service.command.annotation.Option;
import org.cubeengine.libcube.service.command.annotation.ParserFor;
import org.cubeengine.libcube.service.command.annotation.Using;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

@Command(name = "example", desc = "base command")
@Using({ParentExampleCommand.StringProvider.class, ParentExampleCommand.ThisWorldProvider.class})
public class ParentExampleCommand extends DispatcherCommand {

    @Inject
    public ParentExampleCommand(ChildExampleCommand1 cmd1, ChildExampleCommand2 cmd2) {
        super(cmd1, cmd2);
    }

    @Command(desc = "Command with force flag")
    public void flagCommand(CommandCause cause, String firstParam, @Flag boolean force)
    {
        cause.sendMessage(Identity.nil(), Component.text(firstParam + " force:" + force));
    }

    @Command(desc = "Command with optional String")
    public void optionCommand(CommandCause cause, @Option String ommitMe)
    {
        cause.sendMessage(Identity.nil(), Component.text(ommitMe == null ? "optional!" : ommitMe));
    }

    @Command(desc = "Command with defaulted String")
    public void defaultCommand(CommandCause cause, @Default(StringProvider.class) String defaulting)
    {
        cause.sendMessage(Identity.nil(), Component.text(defaulting));
    }

    @Command(desc = "Command with named String")
    public void namedParameter(CommandCause cause, @Named("in") String world)
    {
        if (world == null) {
            world = "no world";
        }
        cause.sendMessage(Identity.nil(), Component.text("in " + world));
    }

    @Command(desc = "Command defaulted named World")
    public void defaultedNamedParameter(CommandCause cause, @Default(ThisWorldProvider.class) @Named("in") ServerWorld world)
    {
        cause.sendMessage(Identity.nil(), Component.text("in " + world.getKey()));
    }

    @Command(desc = "Command only for Players")
    public void restrictedToPlayer(ServerPlayer player)
    {
        player.sendMessage(Identity.nil(), Component.text("Congratulations! You are a player."));
    }

    @ParserFor(String.class)
    public static class StringProvider implements DefaultParameterProvider<String>
    {
        @Override
        public String apply(CommandCause cause)
        {
            return "defaulted";
        }
    }

    @ParserFor(String.class)
    public static class ThisWorldProvider implements DefaultParameterProvider<ServerWorld>
    {
        @Override
        public ServerWorld apply(CommandCause cause)
        {
            return cause.first(ServerPlayer.class).map(ServerPlayer::getWorld).orElseThrow(IllegalStateException::new);
        }
    }
}
