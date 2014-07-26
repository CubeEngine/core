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

import java.util.Locale;

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.command.context.ContextDescriptor;
import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;

import static de.cubeisland.engine.core.util.StringUtils.implode;

public class UsageGenerator
{
    public static String generateUsage(ContextDescriptor descriptor, Locale locale, Permissible permissible)
    {
        // TODO translate labels
        StringBuilder sb = new StringBuilder();
        int inGroup = 0;
        for (CommandParameterIndexed iParam : descriptor.getIndexedParameters())
        {
            if (iParam.getCount() == 1 || iParam.getCount() < 0)
            {
                sb.append(convertLabel(iParam.isGroupRequired(), implode("|", convertLabels(iParam))));
                sb.append(' ');
                inGroup = 0;
            }
            else if (iParam.getCount() > 1)
            {
                sb.append(iParam.isGroupRequired() ? '<' : '[');
                sb.append(convertLabel(iParam.isRequired(), implode("|", convertLabels(iParam))));
                sb.append(' ');
                inGroup = iParam.getCount() - 1;
            }
            else if (iParam.getCount() == 0)
            {
                sb.append(convertLabel(iParam.isRequired(), implode("|", convertLabels(iParam))));
                inGroup--;
                if (inGroup == 0)
                {
                    sb.append(iParam.isGroupRequired() ? '>' : ']');
                }
                sb.append(' ');
            }
        }
        for (CommandParameter nParam : descriptor.getParameters())
        {
            if (nParam.checkPermission(permissible))
            {
                if (nParam.isRequired())
                {
                    sb.append('<').append(nParam.getName()).append(" <").append(nParam.getLabel()).append(">> ");
                }
                else
                {
                    sb.append('[').append(nParam.getName()).append(" <").append(nParam.getLabel()).append(">] ");
                }
            }
        }
        for (CommandFlag flag : descriptor.getFlags())
        {
            if (flag.checkPermission(permissible))
            {
                sb.append("[-").append(flag.getLongName()).append("] ");
            }
        }
        return sb.toString().trim();
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

    private static String[] convertLabels(CommandParameterIndexed indexedParam)
    {
        String[] labels = indexedParam.getLabels().clone();
        String[] rawLabels = indexedParam.getLabels();
        for (int i = 0; i < rawLabels.length; i++)
        {
            if (rawLabels.length == 1)
            {
                labels[i] = convertLabel(true, "#" + rawLabels[i]);
            }
            else
            {
                labels[i] = convertLabel(true, rawLabels[i]);
            }
        }
        return labels;
    }
}
