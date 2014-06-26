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
package de.cubeisland.engine.core.webapi.sender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.util.formatter.MessageType;

public abstract class ApiCommandSender implements CommandSender
{
    private final Core core;
    private ObjectMapper mapper;
    private final List<String> messages = new ArrayList<>();

    public ApiCommandSender(Core core, ObjectMapper mapper)
    {
        this.core = core;
        this.mapper = mapper;
    }

    public Core getCore()
    {
        return this.core;
    }

    @Override
    public void sendMessage(String message)
    {
        this.messages.add(message);
    }

    @Override
    public void sendMessage(String[] strings)
    {
        for (String string : strings)
        {
            this.sendMessage(string);
        }
    }

    @Override
    public Server getServer()
    {
        return ((BukkitCore)this.core).getServer();
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value)
    {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin)
    {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks)
    {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks)
    {
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment)
    {}

    @Override
    public void recalculatePermissions()
    {}

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return Collections.emptySet();
    }

    @Override
    public void setOp(boolean value)
    {}

    @Override
    public boolean isAuthorized(de.cubeisland.engine.core.permission.Permission perm)
    {
        return this.hasPermission(perm.getFullName());
    }

    @Override
    public String getTranslation(MessageType type, String message, Object... params)
    {
        return core.getI18n().translate(getLocale(), type, message, params);
    }

    @Override
    public void sendTranslated(MessageType type, String message, Object... params)
    {
        this.sendMessage(this.getTranslation(type, message, params));
    }

    @Override
    public void sendTranslatedN(MessageType type, int n, String singular, String plural, Object... params)
    {
        this.sendMessage(this.getTranslationN(type, n, singular, plural, params));
    }

    @Override
    public String getTranslationN(MessageType type, int n, String singular, String plural, Object... params)
    {
        return core.getI18n().translateN(getLocale(), type, n, singular, plural, params);
    }

    /**
     * Clears the accumulated messages and returns them as JsonNode
     */
    public JsonNode flush()
    {
        JsonNode jsonNode = mapper.valueToTree(this.messages);
        messages.clear();
        return jsonNode;
    }
}
