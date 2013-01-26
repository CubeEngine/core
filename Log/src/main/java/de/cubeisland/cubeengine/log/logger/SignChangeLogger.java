package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.logger.config.SignChangeConfig;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

public class SignChangeLogger extends Logger<SignChangeConfig>
{
    public SignChangeLogger(Log module) {
        super(module, SignChangeConfig.class);
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event)
    {
        World world = event.getBlock().getWorld();
        SignChangeConfig config = this.configs.get(world);
        if (config.enabled)
        {
            this.logSignChange(event.getPlayer(), event.getLines(), event.getBlock().getState());
        }
    }

    public void logSignChange(Player player, String[] newLines, BlockState state)
    {
        String[] oldlines = ((Sign)state).getLines();
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
        User user = module.getUserManager().getExactUser(player);
        if (user == null)
        {
            this.module.getLogManager().logSignLog(0, state.getLocation(), oldlines, newLines);
        }
        else
        {
            this.module.getLogManager().logSignLog(user.key.intValue(), state.getLocation(), oldlines, newLines);
        }
    }


}
