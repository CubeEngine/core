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

import org.bukkit.permissions.Permission;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.core.Core;

public class ApiServerSender extends ApiCommandSender
{
    public ApiServerSender(Core core, ObjectMapper mapper)
    {
        super(core, mapper);
    }

    @Override
    public String getName()
    {
        return "ApiCommandSender";
    }

    @Override
    public String getDisplayName()
    {
        return "ApiCommandSender";
    }

    @Override
    public Locale getLocale()
    {
        return getCore().getI18n().getDefaultLanguage().getLocale();
    }

    @Override
    public boolean isOp()
    {
        return true;
    }

    @Override
    public boolean isPermissionSet(String name)
    {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission perm)
    {
        return true;
    }

    @Override
    public boolean hasPermission(String name)
    {
        return true;
    }

    @Override
    public boolean hasPermission(Permission perm)
    {
        return true;
    }

    @Override
    public UUID getUniqueId()
    {
        return NON_PLAYER_UUID;
    }
}
