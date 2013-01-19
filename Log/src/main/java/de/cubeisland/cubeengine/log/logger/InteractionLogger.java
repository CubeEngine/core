package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Cake;
import org.bukkit.material.Diode;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;

public class InteractionLogger extends Logger<InteractionLogger.InteractionConfig>
{
    private final Location helper = new Location(null, 0, 0, 0);

    public InteractionLogger()
    {
        super(LogAction.INTERACTION);
        this.config = new InteractionConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
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
                case TRAP_DOOR:
                case FENCE_GATE:
                    if (!((block.getType().equals(Material.WOODEN_DOOR) && this.config.logDoor) || (block.getType().equals(Material.TRAP_DOOR) && this.config.logTrapDoor)
                            || (block.getType().equals(Material.FENCE_GATE) && this.config.logfenceGate)))
                    {
                        return;
                    }
                    newData = (byte) (block.getData() ^ 0x4);
                    break;
                case LEVER:
                    if (!this.config.logLever)
                    {
                        return;
                    }
                case STONE_BUTTON:
                case WOOD_BUTTON:
                    if (!this.config.logButtons)
                    {
                        return;
                    }
                    newData = block.getData();
                    newData ^= 0x8;
                    break;
                case CAKE_BLOCK: // data: remaining slices
                    if (!this.config.logCake)
                    {
                        return;
                    }
                    newData = block.getData();
                    newData += 1;
                    break;
                case NOTE_BLOCK:
                    if (!this.config.logNoteBlock)
                    {
                        return;
                    }
                    newData = ((NoteBlock) block.getState()).getRawNote();
                    newData += 1;
                    if (newData == 25)
                    {
                        newData = 0;
                    }
                    break;
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                    if (!this.config.logDiode)
                    {
                        return;
                    }
                    int delay = ((Diode) blockData).getDelay();
                    delay += 1;
                    if (delay == 5)
                    {
                        delay = 1;
                    }
                    ((Diode) blockData).setDelay(delay);
                    newData = blockData.getData();
                //TODO add new blocks in 1.5
                //TODO anvil
            }
            this.module.getLogManager().logBlockChange(user.key, block.getState(), newData);
        }
        else if (event.getAction().equals(Action.PHYSICAL) && this.config.logPressurePlate)
        {
            User user = this.module.getUserManager().getExactUser(event.getPlayer());
            switch (block.getType())
            {
                case WOOD_PLATE:
                case STONE_PLATE:
                    this.module.getLogManager().logBlockChange(user.key, block.getState(), (byte) 1);
            }
        }
    }

    public static class InteractionConfig extends SubLogConfig
    {
        @Option("log-pressureplate")
        public boolean logPressurePlate = false;
        @Option("log-door")
        public boolean logDoor = false;
        @Option("log-trapDoor")
        public boolean logTrapDoor = false;
        @Option("log-fenceGate")
        public boolean logfenceGate = false;
        @Option("log-lever")
        public boolean logLever = false;
        @Option("log-button")
        public boolean logButtons = false;
        @Option("log-cake")
        public boolean logCake = false;
        @Option("log-noteblock")
        public boolean logNoteBlock = false;
        @Option("log-diode")
        public boolean logDiode = false;

        @Override
        public String getName()
        {
            return "interact";
        }
    }
}
