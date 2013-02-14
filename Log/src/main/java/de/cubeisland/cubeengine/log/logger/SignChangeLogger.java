package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.logger.config.SignChangeConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

public class SignChangeLogger extends Logger<SignChangeConfig>
{
    public SignChangeLogger(Log module)
    {
        super(module, SignChangeConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event)
    {
        World world = event.getBlock().getWorld();
        SignChangeConfig config = this.configs.get(world);
        if (config.enabled)
        {
            this.logSignChange(event.getPlayer(), event.getLines(), ((Sign)event.getBlock().getState()).getLines(), event.getBlock().getLocation());
        }
    }

    public void logSignChange(Player player, String[] newLines, String[] oldlines, Location location)
    {
        if (newLines != null && oldlines != null)
        {
            for (int i = 0; i < 4; ++i)
            {
                if (newLines[0].equals(oldlines[0])
                        && newLines[1].equals(oldlines[1])
                        && newLines[2].equals(oldlines[2])
                        && newLines[3].equals(oldlines[3]))
                {
                    return; //No change -> return
                }
            }
        }
        User user = module.getUserManager().getExactUser(player);
        if (user == null)
        {
            this.module.getLogManager().logSignLog(0, location, oldlines, newLines);
        }
        else
        {
            this.module.getLogManager().logSignLog(user.key.intValue(), location, oldlines, newLines);
        }
    }

    public void logSignPlaceWithData(Player player, Sign state)
    {
        for (String line : state.getLines())
        {
            if (!line.isEmpty())
            {
                this.logSignChange(player, state.getLines(), null, state.getLocation()); // Only log if there are lines
                return;
            }
        }
    }

    public void logSignBreak(Player player, Sign state)
    {
        for (String line : state.getLines())
        {
            if (!line.isEmpty())
            {
                this.logSignChange(player, null, state.getLines(), state.getLocation()); // Only log if there are lines
                return;
            }
        }
    }
}
