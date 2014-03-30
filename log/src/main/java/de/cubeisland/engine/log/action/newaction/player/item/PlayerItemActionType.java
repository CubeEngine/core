package de.cubeisland.engine.log.action.newaction.player.item;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.log.action.newaction.player.PlayerActionType;

public abstract class PlayerItemActionType<ListenerType> extends PlayerActionType<ListenerType>
{
    public ItemStack item; // TODO item format

    public void setItemstack(ItemStack result)
    {
        this.item = result;
    }
}
