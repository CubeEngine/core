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
            switch (event.getClickedBlock().getType())
            {
                case WOODEN_DOOR:
                case TRAP_DOOR:
                case FENCE_GATE:
                    if ((block.getType().equals(Material.WOODEN_DOOR) && this.config.logDoor) || (block.getType().equals(Material.TRAP_DOOR) && this.config.logTrapDoor)
                            || (block.getType().equals(Material.FENCE_GATE) && this.config.logfenceGate))
                    {
                        this.module.getLogManager().logInteractLog(user.key.intValue(), user.getLocation(this.helper),
                                block.getType(), ((Openable) blockData).isOpen() ? 1 : 0);
                    }
                    return;
                case LEVER:
                    if (this.config.logLever)
                    {
                        this.module.getLogManager().logInteractLog(user.key.intValue(), user.getLocation(this.helper),
                                block.getType(), ((Lever) blockData).isPowered() ? 1 : 0);
                    }
                    return;
                case STONE_BUTTON:
                case WOOD_BUTTON:
                    if (this.config.logButtons)
                    {
                        this.module.getLogManager().logInteractLog(user.key.intValue(), user.getLocation(this.helper),
                                block.getType(), null);
                    }
                    return;
                case CAKE_BLOCK: // data: remaining slices
                    if (this.config.logCake)
                    {
                        this.module.getLogManager().logInteractLog(user.key.intValue(), user.getLocation(this.helper),
                                block.getType(), ((Cake) blockData).getSlicesRemaining());
                    }
                    return;
                case NOTE_BLOCK:
                    if (this.config.logNoteBlock)
                    {
                        this.module.getLogManager().logInteractLog(user.key.intValue(), user.getLocation(this.helper),
                                block.getType(), (int) ((NoteBlock) block.getState()).getRawNote());
                    }
                    return;
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                    if (this.config.logDiode)
                    {
                        this.module.getLogManager().logInteractLog(user.key.intValue(), user.getLocation(this.helper),
                                block.getType(), ((Diode) blockData).getDelay());
                    }
                    //TODO add new blocks in 1.5
            }
        }
        else if (event.getAction().equals(Action.PHYSICAL) && this.config.logPressurePlate)
        {
            User user = this.module.getUserManager().getExactUser(event.getPlayer());
            switch (block.getType())
            {
                case WOOD_PLATE:
                case STONE_PLATE:
                    this.module.getLogManager().logInteractLog(user.key.intValue(), user.getLocation(this.helper),
                            block.getType(), null);
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
