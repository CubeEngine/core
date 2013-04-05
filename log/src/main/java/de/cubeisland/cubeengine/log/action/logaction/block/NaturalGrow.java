package de.cubeisland.cubeengine.log.action.logaction.block;

import java.util.List;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.player.PlayerGrow;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class NaturalGrow extends BlockActionType
{
    public NaturalGrow(Log module)
    {
        super(module, 0x23, "natural-grow");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event)
    {
        Player player = event.getPlayer();
        BlockActionType actionType;
        if (player == null)
        {
            actionType = this;
        }
        else
        {
            actionType = this.manager.getActionType(PlayerGrow.class);
        }
        this.logGrow(actionType,event.getWorld(),event.getBlocks(),player);
    }

    private void logGrow(BlockActionType actionType, World world, List<BlockState> blocks, Player player)
    {
        if (actionType.isActive(world))
        {
            for (BlockState newBlock : blocks)
            {
                BlockState oldBlock = world.getBlockAt(newBlock.getLocation()).getState();
                if (!(oldBlock.getTypeId() == newBlock.getTypeId() && oldBlock.getRawData() == newBlock.getRawData()))
                {
                    actionType.logBlockChange(player, oldBlock, newBlock, null);
                }
            }
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            user.sendTranslated("%s&6%dx %s &agrew naturally%s&a!",
                                time,amount,logEntry.getNewBlock(),loc);
        }
        else
        {
            if (logEntry.hasReplacedBlock())
            {
                user.sendTranslated("%s&6%s &agrew naturally into &6%s%s&a!",
                                    time,logEntry.getNewBlock(),
                                    logEntry.getOldBlock(),loc);
            }
            else
            {
                user.sendTranslated("%s&6%s &agrew naturally%s!",
                                    time,logEntry.getNewBlock(),loc);
            }
        }
    }
}
