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

import static org.cubeengine.libcube.service.i18n.Properties.SOURCE;
import static org.cubeengine.libcube.service.i18n.formatter.component.StyledComponent.colored;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.cubeengine.dirigent.context.Context;
import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.cubeengine.dirigent.parser.component.Component;
import org.cubeengine.i18n.I18nService;
import org.cubeengine.libcube.service.i18n.formatter.component.HoverComponent;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Identifiable;

import java.util.UUID;

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
        return colored(NamedTextColor.DARK_GREEN, string);
    }

    @Format
    public Component format(Subject sender, Context context)
    {
        final String name = sender.getFriendlyIdentifier().orElse(sender.getIdentifier());
        if (sender == context.get(SOURCE))
        {
            TextComponent componentName = net.kyori.adventure.text.Component.text(name).color(NamedTextColor.YELLOW);
            if (sender instanceof Identifiable)
            {
                final UUID uuid = ((Identifiable) sender).getUniqueId();
                componentName = componentName.append(
                    net.kyori.adventure.text.Component.text(", ")).append(
                    net.kyori.adventure.text.Component.text(uuid.toString(), NamedTextColor.GOLD));
            }
            return HoverComponent.hoverText(componentName, colored(NamedTextColor.RED, i18n.translate("You")));
        }
        return this.format(name);
    }

    @Format
    public Component format(User user)
    {
        return HoverComponent.hoverText(
            net.kyori.adventure.text.Component.text(user.getUniqueId().toString()).color(NamedTextColor.GOLD), this.format(user.getName()));
    }

    @Format
    public Component format(GameProfile gameProfile)
    {
        return HoverComponent.hoverText(
            net.kyori.adventure.text.Component.text(gameProfile.getUniqueId().toString()).color(NamedTextColor.GOLD), this.format(gameProfile.getName().orElse(gameProfile.getUniqueId().toString())));
    }

    @Format
    public Component format(Tamer tamer)
    {
        return this.format(tamer.getName());
    }
}
