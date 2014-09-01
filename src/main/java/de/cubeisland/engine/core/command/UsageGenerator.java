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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.command.context.BaseParameter;
import de.cubeisland.engine.command.context.CtxDescriptor;
import de.cubeisland.engine.command.context.Flag;
import de.cubeisland.engine.command.context.Group;
import de.cubeisland.engine.command.context.IndexedParameter;
import de.cubeisland.engine.command.context.NamedParameter;
import de.cubeisland.engine.command.context.ParameterGroup;
import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameterNamed;

import static de.cubeisland.engine.command.context.BaseParameter.STATIC_LABEL;
import static de.cubeisland.engine.core.util.StringUtils.implode;

public class UsageGenerator
{
    public static String generateUsage(CtxDescriptor descriptor, Locale locale, Permissible permissible)
    {
        // TODO translate labels
        StringBuilder sb = new StringBuilder();
        for (Group<? extends IndexedParameter> group : descriptor.getIndexedGroups().list())
        {
            generateIndexedUsage(sb, locale, permissible, group);
        }
        for (Group<? extends NamedParameter> group : descriptor.getNamedGroups().list())
        {
            generateNamedUsage(sb, locale, permissible, group);
        }

        for (Flag flag : descriptor.getFlags())
        {
            if (!(flag instanceof CommandFlag) || ((CommandFlag)flag).checkPermission(permissible))
            {
                sb.append("[-").append(flag.getLongName()).append("] ");
            }
        }
        return sb.toString().trim();
    }

    private static void generateNamedUsage(StringBuilder sb, Locale locale, Permissible permissible,
                                           Group<? extends NamedParameter> group)
    {
        if (group instanceof ParameterGroup)
        {
            boolean groupRequired = group.isRequired();
            sb.append(groupRequired ? '<' : '[');
            for (Group<? extends NamedParameter> subGroup : group.list())
            {
                generateNamedUsage(sb, locale, permissible, subGroup);
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(groupRequired ? '>' : ']');
        }
        else if (group instanceof NamedParameter)
        {
            NamedParameter named = (NamedParameter)group;
            if ((!(named instanceof CommandParameterNamed)) || ((CommandParameterNamed)named).checkPermission(permissible))
            {
                return;
            }
            if (group.isRequired())
            {
                sb.append('<').append(named.getName()).append(" <")
                  .append(implode("|", convertLabels(named))).append(">> ");
            }
            else
            {
                sb.append('[').append(named.getName()).append(" <")
                  .append(implode("|", convertLabels(named))).append(">] ");
            }
        }
        sb.append(' ');
    }

    private static void generateIndexedUsage(StringBuilder sb, Locale locale, Permissible permissible,
                                             Group<? extends IndexedParameter> group)
    {
        if (group instanceof ParameterGroup)
        {
            boolean groupRequired = group.isRequired();
            sb.append(groupRequired ? '<' : '[');
            for (Group<? extends IndexedParameter> subGroup : group.list())
            {
                generateIndexedUsage(sb, locale, permissible, subGroup);
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(groupRequired ? '>' : ']');
        }
        else if (group instanceof IndexedParameter)
        {
            IndexedParameter indexed = (IndexedParameter)group;
            sb.append(convertLabel(indexed.isRequired(), implode("|", convertLabels(indexed))));
        }
        sb.append(' ');
    }

    private static String convertLabel(boolean req, String label)
    {
        if (label.startsWith("!"))
        {
            return label.substring(1);
        }
        else if (label.startsWith("#"))
        {
            return label.substring(1);
        }
        else if (req)
        {
            return "<" + label + ">";
        }
        else
        {
            return "[" + label + "]";
        }
    }

    private static String[] convertLabels(BaseParameter<?> indexedParam)
    {
        List<String> list = new ArrayList<>();
        for (String staticValue : indexedParam.getStaticValues())
        {
            list.add(convertLabel(true, "!" + staticValue));
        }
        if (!STATIC_LABEL.equals(indexedParam.getValueLabel())) // only static labels -> skip
        {
            list.add(convertLabel(true, (list.isEmpty() ? "" : "#") + indexedParam.getValueLabel()));
        }
        return list.toArray(new String[list.size()]);
    }
}
