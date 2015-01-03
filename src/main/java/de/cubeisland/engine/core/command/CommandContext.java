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
package de.cubeisland.engine.core.command;

import de.cubeisland.engine.command.CommandBase;
import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.methodic.context.ParameterizedContext;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class CommandContext extends ParameterizedContext
{
    private CommandBase cmd;

    public CommandContext(CommandInvocation call, CommandBase cmd)
    {
        super(call);
        if (!(call.getCommandSource() instanceof CommandSender))
        {
            throw new IllegalArgumentException("CommandSource is not a CommandSender");
        }
        // TODO make sure Invocation has a Cmd with Module
        // TODO make sure CmdSoure is a CmdSender
        this.cmd = cmd;
    }

    @SuppressWarnings("unchecked")
    public <T extends CommandBase> T getCommand()
    {
        return (T)cmd;
    }

    @Override
    public CommandSender getSource()
    {
        return (CommandSender)super.getSource();
    }

    public Core getCore()
    {
        return this.getCommand().getDescriptor().valueFor(ModuleProvider.class).getCore();
    }

    public Module getModule()
    {
        return this.getInvocation().valueFor(ModuleProvider.class);
    }

    public void sendMessage(String message)
    {
        ((CommandSender)this.getInvocation().getCommandSource()).sendMessage(message);
    }

    public void sendTranslated(MessageType type, String message, Object... args)
    {
        ((CommandSender)this.getInvocation().getCommandSource()).sendTranslated(type, message, args);
    }

    public void sendTranslatedN(MessageType type, int count, String sMessage, String pMessage, Object... args)
    {
        ((CommandSender)this.getInvocation().getCommandSource()).sendTranslatedN(type, count, sMessage, pMessage, args);
    }

    public void ensurePermission(Permission permission) throws PermissionDeniedException
    {
        if (!permission.isAuthorized((CommandSender)this.getInvocation().getCommandSource()))
        {
            throw new PermissionDeniedException(permission);
        }
    }

    public String getUsage()
    {
        return this.cmd.getDescriptor().getUsage(this.getInvocation());
    }
}
