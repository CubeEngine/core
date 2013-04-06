package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreakDoorEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;
import static org.bukkit.Material.AIR;

/**
 * Usually Zombies breaking doors.
 * <p>Events: {@link EntityBreakDoorEvent}</p>
 */
public class EntityBreak extends BlockActionType
{
    public EntityBreak(Log module)
    {
        super(module, "entity-break", BLOCK, ENTITY);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreakDoor(final EntityBreakDoorEvent event)
    {
        if (this.isActive(event.getEntity().getWorld()))
        {
            BlockState state = event.getBlock().getState();
            state = this.adjustBlockForDoubleBlocks(state);
            this.logBlockChange(state.getLocation(),event.getEntity(),BlockData.of(state),AIR,null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&aA &6%s &adestroyed &6%s&a%s!",
                            time,
                            logEntry.getCauserEntity(),
                            logEntry.getOldBlock(),
                            loc);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ENTITY_BREAK_enable;
    }
}
