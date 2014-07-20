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
package de.cubeisland.engine.core.ban;

import java.util.Date;

import org.bukkit.Bukkit;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.util.ChatFormat.*;

public class UserBan extends Ban<String>
{
    private final String target;

    public UserBan(String target, String source, String reason)
    {
        this(target, source, reason, new Date(System.currentTimeMillis()), null);
    }

    public UserBan(String target, String source, String reason, Date expires)
    {
        this(target, source, reason, new Date(System.currentTimeMillis()), expires);
    }

    public UserBan(String target, String source, String reason, Date created, Date expires)
    {
        super(source, reason, created, expires);
        expectNotNull(target, "The user must not be null!");
        this.target = target;
    }

    @Override
    public String getTarget()
    {
        return this.target;
    }

    @Override
    public String toString()
    {
        String name = Bukkit.getOfflinePlayer(target).getName();
        return DARK_GREEN + (name == null ? "Unknown" : name) + YELLOW + "(" + GOLD +  this.getTarget() + YELLOW + ")";
    }
}
