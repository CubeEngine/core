package de.cubeisland.cubeengine.core.command.sender;

import org.bukkit.block.Block;

public class BlockCommandSender extends WrappedCommandSender
{
    public BlockCommandSender(org.bukkit.command.BlockCommandSender sender)
    {
        super(sender);
    }

    public Block getBlock()
    {
        return ((org.bukkit.command.BlockCommandSender)this.getWrappedSender()).getBlock();
    }
}
