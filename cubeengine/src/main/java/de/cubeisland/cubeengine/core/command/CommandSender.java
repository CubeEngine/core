package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.permission.Permission;

import java.util.Locale;

public interface CommandSender extends org.bukkit.command.CommandSender
{
    Core getCore();

    String getName();

    String getDisplayName();

    boolean isAuthorized(Permission perm);

    Locale getLocale();

    void sendMessage(String message);

    String translate(String message, Object... params);

    void sendTranslated(String message, Object... params);
}
