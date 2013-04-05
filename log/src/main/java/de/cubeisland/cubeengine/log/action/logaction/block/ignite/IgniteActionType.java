package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockIgniteEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class IgniteActionType extends BlockActionType
{
    public IgniteActionType(Log module, String name)
    {
        super(module, -1, name);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event)
    {
        BlockState blockState = event.getBlock().getState();
        switch (event.getCause())
        {
        case FIREBALL:
            //TODO get targeted player
            this.logIgnite(this.manager.getActionType(FireballIgnite.class),blockState,null);
            break;
        case LAVA:
            this.logIgnite(this.manager.getActionType(LavaIgnite.class),blockState,null);
            break;
        case LIGHTNING:
            this.logIgnite(this.manager.getActionType(LightningIgnite.class),blockState,null);
            break;
        case FLINT_AND_STEEL:
            this.logIgnite(this.manager.getActionType(Lighter.class),blockState,event.getPlayer());
            break;
        case ENDER_CRYSTAL:
        case EXPLOSION:
            this.logIgnite(this.manager.getActionType(OtherIgnite.class),blockState,null);
            break;
        }
    }

    public void logIgnite(BlockActionType igniteType, BlockState state, Entity causer)
    {
        if (igniteType.isActive(state.getWorld()))
        {
            BlockData data = BlockData.of(state);
            data.material = Material.FIRE;
            igniteType.logBlockChange(state.getLocation(),causer,BlockData.of(state),data,null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        throw new UnsupportedOperationException();
    }
}
