package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import de.cubeisland.cubeengine.log.storage.SignChangeLog;
import de.cubeisland.cubeengine.log.storage.SignChangeLogManager;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

public class SignChangeLogger extends Logger<SignChangeLogger.SignChangeConfig>
{
    private SignChangeLogManager signChangeLogManager;

    public SignChangeLogger()
    {
        super(LogAction.SIGNCHANGE);
        this.config = new SignChangeConfig();
        this.signChangeLogManager = new SignChangeLogManager(module.getDatabase());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event)
    {
        this.logSignChange(event.getPlayer(), event.getLines(), event.getBlock().getState());
    }

    public void logSignChange(Player player, String[] lines, BlockState state)
    {
        String[] oldlines = ((Sign)state).getLines();
        for (int i = 0; i < 4; ++i)
        {
            if (lines[0].equals(oldlines[0])
                && lines[1].equals(oldlines[1])
                && lines[2].equals(oldlines[2])
                && lines[3].equals(oldlines[3]))
            {
                return; //No change -> return
            }
        }
        User user = module.getUserManager().getExactUser(player);
        if (user == null)
        {
            this.signChangeLogManager.store(new SignChangeLog(0, state, oldlines, lines));
        }
        else
        {
            this.signChangeLogManager.store(new SignChangeLog(user.key, state, oldlines, lines));
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
