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
package de.cubeisland.engine.core.util.formatter;

import java.util.HashMap;
import java.util.Map;

import de.cubeisland.engine.core.util.ChatFormat;

public class MessageType
{
    private static final Map<String,MessageType> messageTypes = new HashMap<>();

    public final static MessageType POSITIVE = new MessageType("POSITIVE");
    public final static MessageType NEUTRAL = new MessageType("NEUTRAL");
    public final static MessageType NEGATIVE = new MessageType("NEGATIVE");
    public final static MessageType CRITICAL = new MessageType("CRITICAL");
    public final static MessageType NONE = new MessageType("NONE");

    public static MessageType valueOf(String s)
    {
        return messageTypes.get(s.toUpperCase());
    }

    private final String name;

    private MessageType additional = null;

    private MessageType(String name)
    {
        this.name = name;
        messageTypes.put(name, this);
    }

    public final String getName()
    {
        return name;
    }

    public MessageType getAdditional()
    {
        return additional;
    }

    public MessageType and(MessageType additional)
    {
        MessageType type = new MessageType(this.name);
        type.additional = additional;
        return additional;
    }

    public MessageType and(ChatFormat cf)
    {
        return this.and(of(cf));
    }

    public static MessageType of(ChatFormat cf)
    {
        return new MessageType(cf.name());
    }
}
