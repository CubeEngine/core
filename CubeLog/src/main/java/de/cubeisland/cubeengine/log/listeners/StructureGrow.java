package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

import static de.cubeisland.cubeengine.log.LogManager.BlockChangeCause.GROW;

public class StructureGrow extends LogListener
{
    public StructureGrow(Log module)
    {
        super(module, new StructureGrowConfig());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event)
    {
        Player player = null;
        if (event.isFromBonemeal())
        {
            player = event.getPlayer();
        }
        for (BlockState block : event.getBlocks())
        {
            if (player == null)
            {
                lm.logChangeBlock(GROW, null, event.getWorld().getBlockAt(block.getLocation()).getState(), block);
            }
            else
            {
                lm.logChangeBlock(GROW, player, event.getWorld().getBlockAt(block.getLocation()).getState(), block);
            }
        }
    }

    public static class StructureGrowConfig extends LogSubConfiguration
    {
        public StructureGrowConfig()
        {
            this.actions.put(LogAction.NATURALSTRUCTUREGROW, false);
            this.actions.put(LogAction.BONEMEALSTRUCTUREGROW, false);
            this.enabled = true;
        }

        @Override
        public String getName()
        {
            return "grow";
        }
    }
}