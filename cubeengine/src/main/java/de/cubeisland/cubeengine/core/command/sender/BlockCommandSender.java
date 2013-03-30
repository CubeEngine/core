package de.cubeisland.cubeengine.core.command.sender;

import de.cubeisland.cubeengine.core.Core;
import org.bukkit.block.Block;

public class BlockCommandSender extends WrappedCommandSender
{
    public BlockCommandSender(Core core, org.bukkit.command.BlockCommandSender sender)
    {
        super(core, sender);
    }

    public Block getBlock()
    {
        return ((org.bukkit.command.BlockCommandSender)this.getWrappedSender()).getBlock();
    }
}
