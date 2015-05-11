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
package de.cubeisland.engine.module.webapi.sender;

import java.util.Locale;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.cubeisland.engine.module.core.i18n.I18n;
import de.cubeisland.engine.module.core.sponge.SpongeCore;

public class ApiServerSender extends ApiCommandSender
{
    public ApiServerSender(SpongeCore core, ObjectMapper mapper)
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
        return getCore().getModularity().start(I18n.class).getDefaultLanguage().getLocale();
    }

    @Override
    public boolean hasPermission(String name)
    {
        return true;
    }

    @Override
    public UUID getUniqueId()
    {
        return NON_PLAYER_UUID;
    }
}
