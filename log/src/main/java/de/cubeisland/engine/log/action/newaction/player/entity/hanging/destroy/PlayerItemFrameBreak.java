package de.cubeisland.engine.log.action.newaction.player.entity.hanging.destroy;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player breaking an itemframe
 */
public class PlayerItemFrameBreak extends PlayerHangingBreak
{
    // return "hanging-break";
    // return this.lm.getConfig(world).HANGING_BREAK_enable;

    public ItemStack item; // TODO item format

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerItemFrameBreak
            && ((PlayerItemFrameBreak)action).playerUUID.equals(this.playerUUID)
            && ((PlayerItemFrameBreak)action).item == null
            && this.item == null;
    }

    @Override
    public String translateAction(User user)
    {
        // TODO indirect
        if (this.hasAttached())
        {
            int amount = this.getAttached().size() + 1;
            return user.getTranslation(POSITIVE, "{amount} empty {text:itemframes} got removed by {user}", amount, this.playerName);
        }
        if (this.item == null)
        {
            return user.getTranslation(POSITIVE, "{text:itemframe} got removed by {user}", this.playerName);
        }
        else
        {
            return user.getTranslation(POSITIVE, "{user} broke an {text:item-frame} containing {name#item}", this.playerName, this.item.getType().name());
        }
    }
}
