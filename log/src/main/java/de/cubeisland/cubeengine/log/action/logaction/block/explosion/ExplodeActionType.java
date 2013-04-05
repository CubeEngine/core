package de.cubeisland.cubeengine.log.action.logaction.block.explosion;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static org.bukkit.Material.AIR;

public class ExplodeActionType extends BlockActionType
{
    public ExplodeActionType(Log module)
    {
        super(module, -1, "EXPLOSION");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        BlockActionType actionType;
        Player player = null;
        if (event.getEntity() instanceof TNTPrimed)
        {
            actionType = this.manager.getActionType(TntExplode.class);
            //((TNTPrimed)event.getEntity()).getSource()
            //TODO get player who ignited if found
        }
        else if (event.getEntity() instanceof Creeper)
        {
            actionType = this.manager.getActionType(CreeperExplode.class);
            Entity target = ((Creeper)event.getEntity()).getTarget();
            player = target instanceof Player ? ((Player)target) : null;
        }
        else if (event.getEntity() instanceof Fireball)
        {
            actionType = this.manager.getActionType(FireballExplode.class);
            //TODO get shooter if shooter is attacking player log player too
        }
        else if (event.getEntity() instanceof EnderDragon)
        {
            //TODO if is attacking player log player too
            actionType = this.manager.getActionType(EnderdragonExplode.class);
        }
        else if (event.getEntity() instanceof WitherSkull)
        {
            //TODO if is attacking player log player too
            actionType = this.manager.getActionType(WitherExplode.class);
        }
        else
        {
            actionType = this.manager.getActionType(EntityExplode.class);
        }
        if (actionType.isActive(event.getEntity().getWorld()))
        {
            for (Block block : event.blockList())
            {
                if ((block.getType().equals(Material.WOODEN_DOOR)
                    || block.getType().equals(Material.IRON_DOOR_BLOCK))
                    && block.getData() >= 8)
                {
                    continue; // ignore upper door_halfs
                }
                actionType.logBlockChange(block.getLocation(),player,BlockData.of(block.getState()),AIR,null);
                this.logAttachedBlocks(block.getState(), player);
                this.logFallingBlocks(block.getState(),player);
            }
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        throw new UnsupportedOperationException();
    }
}
