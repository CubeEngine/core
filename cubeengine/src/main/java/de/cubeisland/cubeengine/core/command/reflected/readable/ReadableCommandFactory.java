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
            module.getLog().log(LogLevel.WARNING, "The pattern of a readable command failed to compile! ''{0}.{1}''", arr(holder.getClass().getSimpleName(), method.getName()));
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
            if (node.isEmpty())
            {
                cmd.setGeneratedPermissionDefault(annotation.permDefault());
            }
            else
            {
                de.cubeisland.cubeengine.core.permission.Permission perm =
                    module.getBasePermission().createAbstractChild("command");
                perm = perm.createChild(node,annotation.permDefault());
                module.getCore().getPermissionManager().registerPermission(module, perm);
                cmd.setPermission(perm.getName());
                cmd.setPermission(node);
            }
        }
        return cmd;
    }
}
