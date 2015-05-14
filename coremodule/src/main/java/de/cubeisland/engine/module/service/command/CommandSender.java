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
package de.cubeisland.engine.module.service.command;

import java.util.Locale;
import java.util.UUID;
import de.cubeisland.engine.butler.CommandSource;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.core.util.formatter.MessageType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.Translation;

public interface CommandSender extends CommandSource
{
    UUID NON_PLAYER_UUID = new UUID(0, 0);

    CoreModule getCore();

    @Override
    String getName();

    String getDisplayName();

    boolean hasPermission(String perm);

    @Override
    Locale getLocale();

    Translation getTranslation(MessageType type, String message, Object... args);

    Translation getTranslationN(MessageType type, int n, String singular, String plural, Object... args);

    void sendTranslated(MessageType type, String message, Object... args);

    void sendTranslatedN(MessageType type, int n, String singular, String plural, Object... args);

    @Override
    UUID getUniqueId();

    void sendMessage(String msg);
    void sendMessage(Text msg);
}
