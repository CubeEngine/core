package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.util.matcher.EntityType;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.fun.Fun;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;

public class InvasionCommand
{
    private final Fun module;

    public InvasionCommand(Fun module)
    {
        this.module = module;
    }

    @Command(desc = "spawns the mob next to every player on the server", min = 1, max = 1, usage = "<mob>")
    public void invasion(CommandContext context)
    {
        EntityType entityType = Match.entity().mob(context.getString(0, null));
        if (entityType == null)
        {
            illegalParameter(context, "fun", "&cEntityType %s not found", context.getString(0));
        }
        else
        {
            final Location helperLocation = new Location(null, 0, 0, 0);
            for (Player player : Bukkit.getOnlinePlayers())
            {
                Location location = player.getTargetBlock(null, this.module.getConfig().maxInvasionSpawnDistance).getLocation(helperLocation);
                if (location.getBlock().getType() != Material.AIR)
                {
                    location = location.clone();
                    location.subtract(player.getLocation(helperLocation).getDirection().multiply(2));
                }
                player.getWorld().spawnEntity(location, entityType.getBukkitType());
            }
        }
    }
}
