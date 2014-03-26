package de.cubeisland.engine.log.action.newaction;

import org.bukkit.Material;
import org.bukkit.block.BlockState;

public abstract class BlockActionType<ListenerType> extends ActionTypeBase<ListenerType>
{
    public Material oldBlock;
    public int oldData;
    public Material newBlock;
    public int newData;

    public void setOldBlock(BlockState state)
    {



        this.setNewBlock(state.getType());
        // TODO data
        // TODO additional data
    }

    public void setNewBlock(BlockState state)
    {
        this.setOldBlock(state.getType());
        // TODO data
        // TODO additional data
    }

    public void setOldBlock(Material mat)
    {
        this.oldBlock = mat;
    }

    public void setNewBlock(Material mat)
    {
        this.newBlock = mat;
    }
}
