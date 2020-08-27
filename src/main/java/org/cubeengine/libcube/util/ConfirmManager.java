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

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.POSITIVE;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.command.CommandCause;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmManager
{
    private ConfirmManager() {}

    private static final int CONFIRM_TIMEOUT = 30000; // 30 seconds

    private static Map<UUID, Long> times = new HashMap<>();

    public static void requestConfirmation(I18n i18n, Component msg, Audience source, Runnable run)
    {
        Component confirm = i18n.translate(source, Style.empty(), "Confirm");
        Component cancel = i18n.translate(source,  Style.empty(), "Cancel");
        UUID uuid = UUID.randomUUID();
        // TODO callback commands are gone :( ?
//        confirm = confirm.color(NamedTextColor.GOLD).clickEvent(TextActions.executeCallback(s -> confirm(i18n, source, uuid, run))).build();
//        cancel = cancel.color(NamedTextColor.GOLD).clickEvent(TextActions.executeCallback(s -> cancel(i18n, source, uuid))).build();
//        times.put(uuid, System.currentTimeMillis());
//        source.sendMessage(msg.toBuilder().append(Text.of(" ")).append(confirm).append(Text.of(" ")).append(cancel).build());
    }

    private static void confirm(I18n i18n, CommandCause source, UUID uuid, Runnable run)
    {
        Long start = times.remove(uuid);
        if (start == null || System.currentTimeMillis() - start > CONFIRM_TIMEOUT)
        {
            i18n.send(source.getAudience(), NEGATIVE, "Confirmation Request is no longer valid");
            return;
        }
        run.run();
    }

    private static void cancel(I18n i18n, CommandCause source, UUID uuid)
    {
        Long remove = times.remove(uuid);
        if (remove == null)
        {
            i18n.send(source.getAudience(), NEGATIVE, "Confirmation Request is no longer valid");
            return;
        }
        i18n.send(source.getAudience(), POSITIVE, "Confirmation cancelled!");
    }
}
