package de.cubeisland.cubeengine.core.command.reflected.readable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommandFactory;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.module.Module;

import static de.cubeisland.cubeengine.core.util.Misc.arr;

public class ReadableCommandFactory extends ReflectedCommandFactory<ReadableCommand>
{
    public Class<ReadableCommand> getCommandType()
    {
        return ReadableCommand.class;
    }

    protected Class<? extends Annotation> getAnnotationType()
    {
        return ReadableCmd.class;
    }

    protected ReadableCommand buildCommand(Module module, Object holder, Method method, Annotation rawAnnotation)
    {
        ReadableCmd annotation = (ReadableCmd)rawAnnotation;

        String[] commandNames = annotation.names();
        if (commandNames.length == 0)
        {
            commandNames = arr(method.getName());
        }

        String name = commandNames[0].trim().toLowerCase(Locale.ENGLISH);
        List<String> aliases = new ArrayList<String>(commandNames.length - 1);
        for (int i = 1; i < commandNames.length; ++i)
        {
            aliases.add(commandNames[i].toLowerCase(Locale.ENGLISH));
        }

        Pattern pattern;
        try
        {
            pattern = Pattern.compile(annotation.pattern(), annotation.patternFlags());
        }
        catch (PatternSyntaxException e)
        {
            module.getLogger().log(LogLevel.WARNING, "The pattern of a readable command failed to compile! ''{0}.{1}''", arr(holder.getClass().getSimpleName(), method.getName()));
            return null;
        }

        ReadableCommand cmd = new ReadableCommand(
            module,
            holder,
            method,
            name,
            annotation.desc(),
            annotation.usage(),
            aliases,
            pattern
        );
        cmd.setLoggable(annotation.loggable());
        if (annotation.checkPerm())
        {
            String node = annotation.permNode();
            if (node == null || node.isEmpty())
            {
                cmd.setGeneratedPermissionDefault(annotation.permDefault());
            }
            else
            {
                module.getCore().getPermissionManager().registerPermission(module, node, annotation.permDefault());
                cmd.setPermission(node);
            }
        }
        return cmd;
    }
}
