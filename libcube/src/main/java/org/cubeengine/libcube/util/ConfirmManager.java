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
package org.cubeengine.libcube.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import javax.inject.Inject;
import com.google.common.base.Preconditions;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.marker.Enable;
import org.cubeengine.libcube.util.Pair;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.task.TaskManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NONE;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.POSITIVE;

public class ConfirmManager
{
    private ConfirmManager() {}

    private static final int CONFIRM_TIMEOUT = 30000; // 30 seconds

    private static Map<UUID, Long> times = new HashMap<>();

    public static void requestConfirmation(I18n i18n, Text msg, CommandSource source, Runnable run)
    {
        Text confirm = i18n.getTranslation(source, NONE, "Confirm");
        Text cancel = i18n.getTranslation(source, NONE, "Cancel");
        UUID uuid = UUID.randomUUID();
        confirm = confirm.toBuilder().color(TextColors.GOLD).onClick(TextActions.executeCallback(s -> confirm(i18n, source, uuid, run))).build();
        cancel = cancel.toBuilder().color(TextColors.GOLD).onClick(TextActions.executeCallback(s -> cancel(i18n, source, uuid))).build();
        times.put(uuid, System.currentTimeMillis());
        source.sendMessage(msg.toBuilder().append(confirm).append(Text.of(" ")).append(cancel).build());
    }

    private static void confirm(I18n i18n, CommandSource source, UUID uuid, Runnable run)
    {
        Long start = times.remove(uuid);
        if (start == null || System.currentTimeMillis() - start > CONFIRM_TIMEOUT)
        {
            i18n.sendTranslated(source, NEGATIVE, "Confirmation Request is no longer valid");
            return;
        }
        run.run();
    }

    private static void cancel(I18n i18n, CommandSource source, UUID uuid)
    {
        Long remove = times.remove(uuid);
        if (remove == null)
        {
            i18n.sendTranslated(source, NEGATIVE, "Confirmation Request is no longer valid");
            return;
        }
        i18n.sendTranslated(source, POSITIVE, "Confirmation cancelled!");
    }
}
