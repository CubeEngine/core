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
package de.cubeisland.engine.fun.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.fun.Fun;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;

public class InvasionCommand
{
    private final Fun module;

    public InvasionCommand(Fun module)
    {
        this.module = module;
    }

    @Command(desc = "Spawns a mob next to every player on the server",
             indexed = @Grouped(@Indexed(label = "mob")))
    public void invasion(CommandContext context)
    {
        EntityType entityType = Match.entity().mob(context.getString(0, null));
        if (entityType == null)
        {
            context.sendTranslated(NEGATIVE, "EntityType {input} not found", context.getArg(0));
            return;
        }
        final Location helperLocation = new Location(null, 0, 0, 0);
        for (Player player : Bukkit.getOnlinePlayers())
        {
            Location location = player.getTargetBlock(null, this.module.getConfig().command.invasion.distance).getLocation(helperLocation);
            if (location.getBlock().getType() != Material.AIR)
            {
                location = location.clone();
                location.subtract(player.getLocation(helperLocation).getDirection().multiply(2));
            }
            player.getWorld().spawnEntity(location, entityType);
        }
    }
}
