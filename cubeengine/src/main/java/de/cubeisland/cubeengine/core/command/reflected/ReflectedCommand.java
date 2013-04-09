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
package de.cubeisland.cubeengine.core.command.reflected;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.HelpContext;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedCommand;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContextFactory;
import de.cubeisland.cubeengine.core.command.result.ErrorResult;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.ChatFormat;

public class ReflectedCommand extends ParameterizedCommand
{
    private final Object holder;
    private final Method method;
    private final Class<? extends CommandContext> contextType;

    @SuppressWarnings("unchecked")
    public ReflectedCommand(Module module, Object holder, Method method, String name, String description, String usage, List<String> aliases, ParameterizedContextFactory factory)
    {
        super(module, name, description, usage, aliases, factory);

        this.holder = holder;
        this.method = method;
        this.method.setAccessible(true);
        this.contextType = (Class<? extends CommandContext>)method.getParameterTypes()[0];

        Alias annotation = method.getAnnotation(Alias.class);
        if (annotation != null)
        {
            this.registerAlias(annotation.names(), annotation.parents(), annotation.prefix(), annotation.suffix());
        }
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (this.contextType.isInstance(context))
        {
            try
            {
                Object result = this.method.invoke(this.holder, context);
                if (result instanceof CommandResult)
                {
                    return (CommandResult)result;
                }
            }
            catch (InvocationTargetException e)
            {
                if (e.getCause() instanceof Exception)
                {
                    throw (Exception)e.getCause();
                }
                return new ErrorResult(e);
            }
        }
        return null;
    }

    @Override
    public void help(HelpContext context) throws Exception
    {
        context.sendTranslated("&7Description: &f%s", this.getDescription());
        context.sendTranslated("&7Usage: &f%s", this.getUsage(context));

        if (this.hasChildren())
        {
            context.sendMessage(" ");
            context.sendTranslated("The following sub commands are available:");
            context.sendMessage(" ");

            final CommandSender sender = context.getSender();
            for (CubeCommand command : context.getCommand().getChildren())
            {
                if (command.testPermissionSilent(sender))
                {
                    context.sendMessage(ChatFormat.YELLOW + command.getName() + ChatFormat.WHITE + ": " + ChatFormat.GREY + sender.translate(command.getDescription()));
                }
            }
        }
        context.sendMessage(" ");
        context.sendTranslated("&7Detailed help: &9%s", "http://engine.cubeisland.de/commands/" + this.implodeCommandParentNames("/"));
    }
}
