package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.logger.config.InteractionConfig;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.NoteBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Diode;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;

public class InteractionLogger extends Logger<InteractionConfig>
{
    public InteractionLogger(Log module) {
        super(module, InteractionConfig.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    @SuppressWarnings("deprecation")
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        World world = event.getClickedBlock().getWorld();
        InteractionConfig config = this.configs.get(world);
        if (config.enabled)
        {
            Block block = event.getClickedBlock();

            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            {
                MaterialData blockData = block.getState().getData();
                User user = this.module.getUserManager().getExactUser(event.getPlayer());
                byte newData = -1;
                switch (event.getClickedBlock().getType())
                {
                    //toggle 0x4

                    case WOODEN_DOOR:
                        if (!(block.getType().equals(Material.WOODEN_DOOR) && config.logDoor))
                        {
                            return;

                        }
                        if (((org.bukkit.material.Door)blockData).isTopHalf())
                        {
                            block = block.getRelative(BlockFace.DOWN);
                            blockData = block.getState().getData();
                        }
                        //this is not working correctly for top half doors
                        ((Openable)blockData).setOpen(!((Openable)blockData).isOpen());
                        newData = blockData.getData();
                        break;
                    case TRAP_DOOR:
                    case FENCE_GATE:
                        if (!(block.getType().equals(Material.TRAP_DOOR) && config.logTrapDoor)
                                || (block.getType().equals(Material.FENCE_GATE) && config.logfenceGate))
                        {
                            return;
                        }
                        break;
                    case LEVER:
                        if (!config.logLever)
                        {
                            return;
                        }
                        newData = block.getData();
                        newData ^= 0x8;
                        break;
                    case STONE_BUTTON:
                    case WOOD_BUTTON:
                        if (!config.logButtons)
                        {
                            return;
                        }
                        newData = block.getData();
                        newData &= 0x8;
                        break;
                    case CAKE_BLOCK: // data: remaining slices
                        if (!config.logCake)
                        {
                            return;
                        }
                        newData = block.getData();
                        newData += 1;
                        break;
                    case NOTE_BLOCK:
                        if (!config.logNoteBlock)
                        {
                            return;
                        }
                        newData = ((NoteBlock)block.getState()).getRawNote();
                        newData += 1;
                        if (newData == 25)
                        {
                            newData = 0;
                        }
                        break;
                    case DIODE_BLOCK_OFF:
                    case DIODE_BLOCK_ON:
                        if (!config.logDiode)
                        {
                            return;
                        }
                        int delay = ((Diode)blockData).getDelay();
                        delay += 1;
                        if (delay == 5)
                        {
                            delay = 1;
                        }
                        ((Diode)blockData).setDelay(delay);
                        newData = blockData.getData();
                        break;
                    default:
                        return;
                        //TODO add new blocks in 1.5
                        //TODO anvil
                }
                this.module.getLogManager().logBlockChange(user.key, world, block.getState(), newData);
            }
            else if (event.getAction().equals(Action.PHYSICAL) && config.logPressurePlate)
            {
                User user = this.module.getUserManager().getExactUser(event.getPlayer());
                switch (block.getType())
                {
                    case WOOD_PLATE:
                    case STONE_PLATE:
                        this.module.getLogManager().logBlockChange(user.key, world, block.getState(), (byte)1);
                }
            }
        }
    }
}
