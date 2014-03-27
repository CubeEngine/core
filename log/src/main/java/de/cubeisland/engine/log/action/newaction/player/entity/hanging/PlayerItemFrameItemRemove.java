package de.cubeisland.engine.log.action.newaction.player.entity.hanging;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player removing an item from an item-frame
 */
public class PlayerItemFrameItemRemove extends PlayerHangingActionType
{
    // return "remove-item";
    // return this.lm.getConfig(world).ITEM_REMOVE_FROM_FRAME;

    public ItemStack item;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerItemFrameItemRemove
            && ((PlayerItemFrameItemRemove)action).playerUUID.equals(this.playerUUID);
    }

    @Override
    public String translateAction(User user)
    {
        int amount = 1;
        if (this.hasAttached())
        {
            amount += this.getAttached().size();
        }
        return user.getTranslationN(POSITIVE, amount,
                                    "{user} removed {name#item} from an itemframe",
                                    "{user} removed {2:amount} items from itemframes",
                                    this.playerName, this.item.getType().name(), amount);
    }

    // TODO redo/rollback
}
