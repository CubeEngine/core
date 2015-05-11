/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 * <p/>
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.module.webapi.sender;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.i18n.I18n;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.core.util.formatter.MessageType;

public abstract class ApiCommandSender implements CommandSender
{
    private final CoreModule core;
    private ObjectMapper mapper;
    private final List<String> messages = new ArrayList<>();

    public ApiCommandSender(CoreModule core, ObjectMapper mapper)
    {
        this.core = core;
        this.mapper = mapper;
    }

    @Override
    public CoreModule getCore()
    {
        return this.core;
    }

    @Override
    public void sendMessage(String message)
    {
        this.messages.add(message);
    }

    @Override
    public String getTranslation(MessageType type, String message, Object... params)
    {
        return core.getModularity().start(I18n.class).translate(getLocale(), type, message, params);
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
        return core.getModularity().start(I18n.class).translateN(getLocale(), type, n, singular, plural, params);
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
