package de.cubeisland.cubeengine.core.command.reflected.readable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.HelpContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.result.ErrorResult;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.ChatFormat;

public class ReadableCommand extends CubeCommand
{
    private final Object holder;
    private final Method method;
    private final Class<? extends CommandContext> contextType;
    private final Pattern pattern;

    @SuppressWarnings("unchecked")
    public ReadableCommand(Module module, Object holder, Method method, String name, String description, String usage, List<String> aliases, Pattern pattern)
    {
        super(module, name, description, usage, aliases, new ReadableContextFactory(pattern));

        this.holder = holder;
        this.method = method;
        this.method.setAccessible(true);
        this.contextType = (Class<? extends CommandContext>)method.getParameterTypes()[0];
        this.pattern = pattern;

        Alias annotation = method.getAnnotation(Alias.class);
        if (annotation != null)
        {
            this.registerAlias(annotation.names(), annotation.parents(), annotation.prefix(), annotation.suffix());
        }
    }

    public Pattern getPattern()
    {
        return this.pattern;
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
        context.sendTranslated("Description: &f%s", this.getDescription());
        context.sendTranslated("Usage: &f%s", this.getUsage(context));

        if (this.hasChildren())
        {
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
    }
}
