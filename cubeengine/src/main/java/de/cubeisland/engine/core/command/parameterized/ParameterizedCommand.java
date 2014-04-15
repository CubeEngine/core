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
package de.cubeisland.engine.core.command.parameterized;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;

import static de.cubeisland.engine.core.command.parameterized.ParameterizedTabContext.Type.*;
import static de.cubeisland.engine.core.util.StringUtils.startsWithIgnoreCase;

public abstract class ParameterizedCommand extends CubeCommand
{
    protected ParameterizedCommand(Module module, String name, String description, ParameterizedContextFactory parser, Permission permission)
    {
        super(module, name, description, parser, permission);
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
    
    public List<String> tabComplete(ParameterizedTabContext context)
    {
        if (context.last == ParameterizedTabContext.Type.NOTHING)
        {
            return null;
        }
        final ParameterizedContextFactory cFactory = this.getContextFactory();
        if (context.last == PARAM_VALUE)
        {
            return tabCompleteParamValue(context, cFactory);
        }
        List<String> result = new ArrayList<>();
        List<String> args = context.getArgs();
        String last = args.get(args.size() - 1);
        if (context.last == FLAG_OR_INDEXED)
        {
            tabCompleteFlags(context, cFactory, result, last);
            tabCompleteIndexed(context, cFactory, result, args.size() - 1, last);
        }
        else if (context.last == INDEXED_OR_PARAM)
        {
            tabCompleteIndexed(context, cFactory, result, args.size() - 1, last);
            tabCompleteParam(context, cFactory, result, last);
        }
        else if (context.last == ANY)
        {
            tabCompleteIndexed(context, cFactory, result, args.size() - 1, last);
            tabCompleteParam(context, cFactory, result, last);
            tabCompleteFlags(context, cFactory, result, last);
        }
        return result;
    }

    private List<String> tabCompleteParamValue(ParameterizedTabContext context, ParameterizedContextFactory cFactory)
    {
        Iterator<Entry<String, String>> iterator = context.getParams().entrySet().iterator();
        Entry<String, String> lastParameter;
        do
        {
            lastParameter = iterator.next();
        }
        while (iterator.hasNext());
        Completer completer = cFactory.getParameter(lastParameter.getKey()).getCompleter();
        if (completer != null)
        {
            return completer.complete(context, lastParameter.getValue());
        }
        return null; // TODO check if bukkit wont do player tab completion
    }

    private void tabCompleteParam(ParameterizedTabContext context, ParameterizedContextFactory cFactory, List<String> result, String last)
    {
        for (CommandParameter parameter : cFactory.getParameters())
        {
            if (!context.hasParam(parameter.getName()))
            {
                if (startsWithIgnoreCase(parameter.getName(), last))
                {
                    result.add(parameter.getName());
                }
                if (!last.isEmpty())
                {
                    for (String alias : parameter.getAliases())
                    {
                        if (alias.length() > 2 && startsWithIgnoreCase(alias, last))
                        {
                            result.add(alias);
                        }
                    }
                }
            }
        }
    }

    private void tabCompleteIndexed(ParameterizedTabContext context, ParameterizedContextFactory cFactory,
                                    List<String> result, int index, String last)
    {
        CommandParameterIndexed indexed = cFactory.getIndexed(index);
        if (indexed != null)
        {
            Completer indexedCompleter = indexed.getCompleter();
            if (indexedCompleter != null)
            {
                result.addAll(indexedCompleter.complete(context, last));
            }
        }
    }

    private void tabCompleteFlags(ParameterizedTabContext context, ParameterizedContextFactory cFactory, List<String> result, String last)
    {
        if (!last.isEmpty())
        {
            last = last.substring(1);
        }
        for (CommandFlag commandFlag : cFactory.getFlags())
        {
            if (!context.hasFlag(commandFlag.getName()) && startsWithIgnoreCase(commandFlag.getLongName(), last))
            {
                result.add("-" + commandFlag.getLongName());
            }
        }
    }

    @Override
    public final List<String> tabComplete(CommandContext context)
    {
        if (context instanceof ParameterizedTabContext)
        {
            return this.tabComplete((ParameterizedTabContext)context);
        }
        return super.tabComplete(context);
    }

    @Override
    protected String getUsage0(Locale locale, Permissible permissible)
    {
        StringBuilder sb = new StringBuilder(super.getUsage0(locale, permissible)).append(' ');
        for (CommandParameter param : this.getContextFactory().getParameters())
        {
            if (param.checkPermission(permissible))
            {
                if (param.isRequired())
                {
                    sb.append('<').append(param.getName()).append(" <").append(param.getLabel()).append(">> ");
                }
                else
                {
                    sb.append('[').append(param.getName()).append(" <").append(param.getLabel()).append(">] ");
                }
            }
        }
        for (CommandFlag flag : this.getContextFactory().getFlags())
        {
            if (flag.checkPermission(permissible))
            {
                sb.append("[-").append(flag.getLongName()).append("] ");
            }
        }
        return sb.toString().trim();
    }
}
