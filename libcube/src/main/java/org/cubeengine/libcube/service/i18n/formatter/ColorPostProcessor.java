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
package org.cubeengine.libcube.service.i18n.formatter;

import org.cubeengine.dirigent.context.Context;
import org.cubeengine.dirigent.formatter.PostProcessor;
import org.cubeengine.dirigent.formatter.argument.Arguments;
import org.cubeengine.dirigent.parser.component.Component;
import org.cubeengine.dirigent.parser.component.ResolvedMacro;
import org.cubeengine.libcube.util.ChatFormat;
import org.cubeengine.libcube.service.i18n.formatter.component.StyledComponent;

import static org.cubeengine.libcube.util.ChatFormat.GOLD;

public class ColorPostProcessor implements PostProcessor
{
    private final ChatFormat defaultColor;

    public ColorPostProcessor()
    {
        this(GOLD);
    }

    public ColorPostProcessor(ChatFormat defaultColor)
    {
        this.defaultColor = defaultColor;
    }

    @Override
    public Component process(Component component, Context context, Arguments args)
    {
        if (!(component instanceof ResolvedMacro))
        {
            return component;
        }
        String colorString = ((ResolvedMacro)component).getArguments().get("color");//context.get("color");
        ChatFormat color = defaultColor;
        if (colorString != null)
        {
            try
            {
                color = ChatFormat.valueOf(colorString);
            }
            catch (IllegalArgumentException ignored)
            {
            }
        }
        else if (defaultColor == ChatFormat.RESET)
        {
            return component;
        }
        return new StyledComponent(color.getColor(), component);
    }
}
