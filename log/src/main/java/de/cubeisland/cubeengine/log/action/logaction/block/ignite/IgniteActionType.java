package de.cubeisland.cubeengine.log.action.logaction.block.ignite;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockIgniteEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;

public abstract class IgniteActionType extends BlockActionType
{
    public IgniteActionType(Log module, int id, String name)
    {
        super(module, id, name);
    }

    public abstract void onIgnite(BlockIgniteEvent event);

    public void logIgnite(BlockState state, Entity causer)
    {
        BlockData data = BlockData.of(state);
        data.material = Material.FIRE;
        this.logBlockChange(state.getLocation(),causer,BlockData.of(state),data,null);
    }
}
