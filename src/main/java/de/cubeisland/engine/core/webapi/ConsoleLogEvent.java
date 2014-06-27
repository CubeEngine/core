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
package de.cubeisland.engine.core.webapi;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class ConsoleLogEvent extends AbstractAppender
{
    private final ApiServer server;
    private final ObjectMapper mapper = new ObjectMapper();

    public ConsoleLogEvent(ApiServer server)
    {
        super("api-console", null, PatternLayout.createLayout(null, null, null, null, null));
        this.server = server;
    }

    @Override
    public void append(LogEvent logEvent)
    {
        ObjectNode node = mapper.createObjectNode();
        node.put("msg", logEvent.getMessage().getFormattedMessage());
        this.server.fireEvent("console", node);
    }

    @Override
    public boolean isFiltered(LogEvent event)
    {
        return false;
    }
}
