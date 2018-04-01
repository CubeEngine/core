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

import org.cubeengine.i18n.I18nService;
import org.cubeengine.dirigent.context.Context;
import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.cubeengine.dirigent.parser.component.Component;
import org.cubeengine.libcube.service.i18n.formatter.component.HoverComponent;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

import static org.cubeengine.libcube.service.i18n.Properties.SOURCE;
import static org.cubeengine.libcube.service.i18n.formatter.component.StyledComponent.colored;
import static org.spongepowered.api.text.Text.builder;
import static org.spongepowered.api.text.Text.of;
import static org.spongepowered.api.text.format.TextColors.*;

@Names({"player", "user","sender","tamer"})
public class CommandSourceFormatter extends ReflectedFormatter
{
    private final I18nService i18n;

    public CommandSourceFormatter(I18nService i18n) {
        this.i18n = i18n;
    }

    @Format
    public Component format(String string)
    {
        return colored(DARK_GREEN, string);
    }

    @Format
    public Component format(CommandSource sender, Context context)
    {
        if (sender == context.get(SOURCE))
        {
            Text.Builder b = builder().append(of(YELLOW, sender.getName()));
            if (sender instanceof Identifiable)
            {
                b.append(of(", ")).append(of(GOLD, ((Identifiable) sender).getUniqueId()));
            }
            return HoverComponent.hoverText(b.build(), colored(RED, i18n.translate("You")));
        }
        return this.format(sender.getName());
    }

    @Format
    public Component format(User user)
    {
        return HoverComponent.hoverText(of(GOLD, user.getUniqueId().toString()), this.format(user.getName()));
    }

    @Format
    public Component format(Tamer tamer)
    {
        return this.format(tamer.getName());
    }
}
