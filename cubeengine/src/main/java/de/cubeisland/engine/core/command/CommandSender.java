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
package de.cubeisland.engine.core.command;

import java.util.Locale;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.formatter.MessageType;

public interface CommandSender extends org.bukkit.command.CommandSender
{
    Core getCore();

    String getName();

    String getDisplayName();

    boolean isAuthorized(Permission perm);

    Locale getLocale();

    void sendMessage(String message);

    String translate(MessageType type, String message, Object... params);

    void sendTranslated(MessageType type, String message, Object... params);

    void sendTranslatedN(MessageType type, int n, String singular, String plural, Object... params);

    String translateN(MessageType type, int n, String singular, String plural, Object... params);
}
