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

import org.cubeengine.dirigent.Component;
import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.cubeengine.libcube.service.i18n.formatter.component.HoverComponent;
import org.cubeengine.libcube.service.i18n.formatter.component.StyledComponent;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import static org.spongepowered.api.text.format.TextColors.DARK_GREEN;
import static org.spongepowered.api.text.format.TextColors.GOLD;

@Names({"user","sender","tamer"})
public class CommandSenderFormatter extends ReflectedFormatter
{
    @Format
    public Component format(String string)
    {
        return new StyledComponent(DARK_GREEN, string);
    }

    @Format
    public Component format(CommandSource sender)
    {
        return this.format(sender.getName());
    }

    @Format
    public Component format(Tamer tamer)
    {
        return this.format(tamer.getName());
    }

    @Format
    public Component format(User user)
    {
        return HoverComponent.hoverText(Text.of(GOLD, user.getUniqueId().toString()), this.format(user.getName()));
    }
}
