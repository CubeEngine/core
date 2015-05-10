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

import java.util.Locale;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.service.Permission;
import de.cubeisland.engine.core.user.User;

public class ApiUser extends ApiCommandSender
{
    private final User user;

    public ApiUser(Core core, User user, ObjectMapper mapper)
    {
        super(core, mapper);
        this.user = user;
    }

    @Override
    public Locale getLocale()
    {
        return user.getLocale();
    }

    @Override
    public String getName()
    {
        return "Api:" + user.getName();
    }

    @Override
    public boolean hasPermission(String name)
    {
        return user.hasPermission(name);
    }

    @Override
    public UUID getUniqueId()
    {
        return user.getUniqueId();
    }

    @Override
    public String getDisplayName()
    {
        return user.getDisplayName().toString();
    }
}
