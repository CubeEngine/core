package de.cubeisland.cubeengine.core.command.sender;

import de.cubeisland.cubeengine.core.permission.Permission;

public interface CommandSender extends org.bukkit.command.CommandSender
{
    String getName();

    boolean isAuthorized(Permission perm);

    String getLanguage();

    void sendMessage(String message);

    void sendMessage(String category, String message, Object... params);
}
