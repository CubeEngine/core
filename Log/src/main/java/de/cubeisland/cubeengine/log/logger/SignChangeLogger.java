package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import de.cubeisland.cubeengine.log.storage.LogManager;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

public class SignChangeLogger extends Logger<SignChangeLogger.SignChangeConfig>
{
    public SignChangeLogger()
    {
        super(LogAction.SIGNCHANGE);
        this.config = new SignChangeConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event)
    {
        this.logSignChange(event.getPlayer(), event.getLines(), event.getBlock().getState());
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
            this.lm.logSignLog(0, state.getLocation(), oldlines, newLines);
        }
        else
        {
            this.lm.logSignLog(user.key, state.getLocation(), oldlines, newLines);
        }
    }

    public static class SignChangeConfig extends SubLogConfig
    {
        public SignChangeConfig()
        {
            this.enabled = false;
        }

        @Override
        public String getName()
        {
            return "sign-changes";
        }
    }
}
