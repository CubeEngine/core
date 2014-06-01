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
package de.cubeisland.engine.core.module.service;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.command.CommandSender;

public interface Permission
{
    String getName();
    boolean isEnabled();
    boolean hasSuperPermsCompat();

    boolean has(World world, OfflinePlayer player, String permission);
    boolean has(CommandSender sender, String permission);

    boolean add(World world, OfflinePlayer player, String permission);
    boolean addTemporary(World world, Player player, String permission);

    boolean remove(World world, OfflinePlayer player, String permission);
    boolean removeTemporary(World world, OfflinePlayer player, String permission);

    boolean has(World world, String role, String permission);
    boolean add(World world, String role, String permission);
    boolean remove(World world, String role, String permission);

    boolean hasRole(World world, OfflinePlayer player, String role);
    boolean addRole(World world, OfflinePlayer player, String role);
    boolean removeRole(World world, OfflinePlayer player, String role);

    String[] getRoles(World world, OfflinePlayer player);
    String getDominantRole(World world, OfflinePlayer player);

    boolean hasRoleSupport();
    String[] getRoles(World world);
    String[] getGlobalRoles();
}
