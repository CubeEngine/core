package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import static de.cubeisland.cubeengine.log.LogManager.BlockBreakCause.PLAYER;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 *
 * @author Anselm Brehme
 */
public class BlockBreakLogger extends LogListener
{
    public BlockBreakLogger(Log module)
    {
        super(module, new BreakConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        //TODO check nearby blocks for breaking signs etc
        lm.logBreakBlock(PLAYER, event.getPlayer(), event.getBlock().getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event)
    {
        lm.logBreakBlock(PLAYER, event.getPlayer(), event.getBlockClicked().getState());
    }

    public static class BreakConfig extends LogSubConfiguration
    {
        public BreakConfig()
        {
            this.actions.put(LogAction.PLAYER_BLOCKBREAK, true);

        }
        @Option("actions")
        public Map<LogAction, Boolean> actions = new EnumMap<LogAction, Boolean>(LogAction.class);
        @Option("creeper.log-as-player")
        public boolean logAsPlayer = false;

        @Override
        public String getName()
        {
            return "break";
        }
    }
}
