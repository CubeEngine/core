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
package org.cubeengine.service.webapi.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.command.source.ConsoleSource;

public class ApiServerSender extends ApiCommandSource
{
    private ConsoleSource source;

    public ApiServerSender(RemoteConnection connection, ObjectMapper mapper, ConsoleSource source)
    {
        super(connection, mapper);
        this.source = source;
    }

    @Override
    protected Subject internalSubject()
    {
        if (this.getLoggedIn())
        {
            return source;
        }
        return  null; // TODO handle sources without valid auth
    }

    @Override
    public String getName()
    {
        return "CubeEngine:ApiCommandSource";
    }

    @Override
    public String getIdentifier()
    {
        return getName();
    }
}
