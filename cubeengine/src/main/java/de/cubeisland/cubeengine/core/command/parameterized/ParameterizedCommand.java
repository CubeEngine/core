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
package de.cubeisland.cubeengine.core.command.parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.module.Module;

import static de.cubeisland.cubeengine.core.util.StringUtils.startsWithIgnoreCase;

public abstract class ParameterizedCommand extends CubeCommand
{
    protected ParameterizedCommand(Module module, String name, String description, ParameterizedContextFactory parser)
    {
        super(module, name, description, parser);
    }

    protected ParameterizedCommand(Module module, String name, String description, String usageMessage, List<String> aliases, ParameterizedContextFactory parser)
    {
        super(module, name, description, usageMessage, aliases, parser);
    }

    @Override
    public ParameterizedContextFactory getContextFactory()
    {
        return (ParameterizedContextFactory)super.getContextFactory();
    }

    public void addParameter(CommandParameter param)
    {
        this.getContextFactory().addParameter(param);
    }

    public void addFlag(CommandFlag flag)
    {
        this.getContextFactory().addFlag(flag);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args)
    {
        if (args.length == 0)
        {
            return null;
        }

        List<String> result = null;
        String token = args[args.length - 1];
        final ParameterizedContextFactory contextFactory = this.getContextFactory();
        if (args.length >= 2)
        {
            CommandParameter param = contextFactory.getParameter(args[args.length - 2]);
            if (param != null)
            {
                Completer completer = param.getCompleter();
                if (completer != null)
                {
                    result = completer.complete(sender, token);
                }
            }
        }

        final boolean mayBeFlag = (token.length() > 0 && token.charAt(0) == '-');
        if (result == null && !mayBeFlag)
        {
            List<String> params = new ArrayList<String>(0);
            for (CommandParameter entry : contextFactory.getParameters())
            {
                if (startsWithIgnoreCase(entry.getName(), token))
                {
                    params.add(entry.getName());
                }
            }
            if (!params.isEmpty())
            {
                result = params;
            }
        }
        if (result == null && mayBeFlag)
        {
            List<String> flags = new ArrayList<String>();
            if (!token.isEmpty() && token.charAt(0) == '-')
            {
                token = token.substring(1).toLowerCase(Locale.ENGLISH);
                for (CommandFlag flag : contextFactory.getFlags())
                {
                    final String name = flag.getLongName().toLowerCase(Locale.ENGLISH);
                    if (startsWithIgnoreCase(name, token))
                    {
                        flags.add("-" + name);
                    }
                }
                if (!flags.isEmpty())
                {
                    result = flags;
                }
            }
        }

        if (result != null)
        {
            Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        }

        return result;
    }
}
