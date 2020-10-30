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
package org.cubeengine.libcube.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.adventure.SpongeComponents;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.POSITIVE;

public class ConfirmManager
{
    private ConfirmManager() {}

    private static final int CONFIRM_TIMEOUT = 30000; // 30 seconds

    private static Map<UUID, Long> times = new HashMap<>();

    public static void requestConfirmation(I18n i18n, Component msg, Audience source, Runnable run)
    {
        final UUID uuid = UUID.randomUUID();
        final Component confirm = i18n.translate(source, Style.style(NamedTextColor.GOLD), "Confirm")
            .clickEvent(SpongeComponents.executeCallback(s -> confirm(i18n, source, uuid, run)));
        final Component cancel = i18n.translate(source,  Style.style(NamedTextColor.GOLD), "Cancel")
            .clickEvent(SpongeComponents.executeCallback(s -> cancel(i18n, source, uuid)));
        ConfirmManager.times.put(uuid, System.currentTimeMillis());
        source.sendMessage(Identity.nil(), Component.text().append(msg).append(Component.space()).append(confirm).append(Component.space()).append(cancel).build());
    }

    private static void confirm(I18n i18n, Audience source, UUID uuid, Runnable run)
    {
        Long start = ConfirmManager.times.remove(uuid);
        if (start == null || System.currentTimeMillis() - start > ConfirmManager.CONFIRM_TIMEOUT)
        {
            i18n.send(source, NEGATIVE, "Confirmation Request is no longer valid");
            return;
        }
        run.run();
    }

    private static void cancel(I18n i18n, Audience source, UUID uuid)
    {
        Long remove = ConfirmManager.times.remove(uuid);
        if (remove == null)
        {
            i18n.send(source, NEGATIVE, "Confirmation Request is no longer valid");
            return;
        }
        i18n.send(source, POSITIVE, "Confirmation cancelled!");
    }
}
