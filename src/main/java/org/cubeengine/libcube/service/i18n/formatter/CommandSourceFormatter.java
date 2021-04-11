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
import org.cubeengine.dirigent.parser.component.ComponentGroup;
import org.cubeengine.i18n.I18nService;
import org.cubeengine.libcube.service.i18n.formatter.component.HoverComponent;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Identifiable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Names({"player", "user", "sender", "tamer"})
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
    public Component format(ListOfNames names)
    {
        final List<String> list = names.getNames();
        final Iterator<String> iterator = list.iterator();
        if (iterator.hasNext()) {
            List<Component> components = new ArrayList<>(list.size() * 2 - 1);
            components.add(colored(NamedTextColor.DARK_GREEN, iterator.next()));
            while (iterator.hasNext()) {
                components.add(new org.cubeengine.libcube.service.i18n.formatter.component.TextComponent(
                    net.kyori.adventure.text.Component.text(", ")));
                components.add(colored(NamedTextColor.DARK_GREEN, iterator.next()));
            }
            return new ComponentGroup(components);
        } else {
            return new org.cubeengine.libcube.service.i18n.formatter.component.TextComponent(
                net.kyori.adventure.text.Component.empty());
        }
    }

    @Format
    public Component format(Subject sender, Context context)
    {
        final String name = sender.friendlyIdentifier().orElse(sender.identifier());
        if (sender == context.get(SOURCE))
        {
            TextComponent componentName = net.kyori.adventure.text.Component.text(name).color(NamedTextColor.YELLOW);
            if (sender instanceof Identifiable)
            {
                final UUID uuid = ((Identifiable) sender).uniqueId();
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
            net.kyori.adventure.text.Component.text(user.uniqueId().toString()).color(NamedTextColor.GOLD), this.format(user.name()));
    }

    @Format
    public Component format(GameProfile gameProfile)
    {
        return HoverComponent.hoverText(
            net.kyori.adventure.text.Component.text(gameProfile.uniqueId().toString()).color(NamedTextColor.GOLD), this.format(gameProfile.name().orElse(gameProfile.uniqueId().toString())));
    }

    @Format
    public Component format(Tamer tamer)
    {
        return this.format(tamer.name());
    }

    public static class ListOfNames {
        private final List<String> names;

        public ListOfNames(List<String> names)
        {
            this.names = names;
        }

        public List<String> getNames()
        {
            return names;
        }
    }
}
