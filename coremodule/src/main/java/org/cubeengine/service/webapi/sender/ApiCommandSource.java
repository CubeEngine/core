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
package org.cubeengine.service.webapi.sender;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cubeengine.service.command.AbstractCommandSource;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.source.RconSource;

public abstract class ApiCommandSource extends AbstractCommandSource implements RconSource
{
    private RemoteConnection connection;
    private ObjectMapper mapper;
    private final List<String> messages = new ArrayList<>();
    private boolean loggedIn;

    public ApiCommandSource(RemoteConnection connection, ObjectMapper mapper)
    {
        this.connection = connection;
        this.mapper = mapper;
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

    @Override
    protected void sendMessage0(Text text)
    {
        messages.add(Texts.toPlain(text)); // TODO color support?
    }

    @Override
    public boolean getLoggedIn()
    {
        return loggedIn;
    }

    @Override
    public void setLoggedIn(boolean loggedIn)
    {
        this.loggedIn = loggedIn;
    }

    @Override
    public RemoteConnection getConnection()
    {
        return connection;
    }
}
