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
package de.cubeisland.engine.service.i18n.formatter;

import de.cubeisland.engine.service.i18n.formatter.component.StyledComponent;
import org.cubeengine.dirigent.Component;
import org.cubeengine.dirigent.formatter.Context;
import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.util.command.CommandSource;

import static org.spongepowered.api.text.format.TextColors.DARK_GREEN;

@Names({"user","sender","tamer"})
public class CommandSenderFormatter extends ReflectedFormatter
{
    @Format
    public Component format(String string, Context context)
    {
        return new StyledComponent(DARK_GREEN, string);
    }

    @Format
    public Component format(CommandSource sender, Context context)
    {
        return this.format(sender.getName(), context);
    }

    @Format
    public Component format(de.cubeisland.engine.butler.CommandSource sender, Context context)
    {
        return this.format(sender.getName(), context);
    }

    @Format
    public Component format(Tamer tamer, Context context) // includes OfflinePlayer as it implements AnimalTamer
    {
        return this.format(tamer.getName(), context);
    }

}
