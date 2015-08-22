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

import java.util.Locale;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.user.User;
import org.spongepowered.api.text.Text;

public class ApiUser extends ApiCommandSender
{
    private final User user;

    public ApiUser(I18n i18n, User user, ObjectMapper mapper)
    {
        super(i18n, mapper);
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
    public Text getDisplayName()
    {
        return user.getDisplayName();
    }
}
