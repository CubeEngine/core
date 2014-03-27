package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.Material.REDSTONE_COMPARATOR_ON;

/**
 * Represents a player changing a comparator state
 */
public class ComparatorChange extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "comparator-change";
    // return this.lm.getConfig(world).block.COMPARATPR_CHANGE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof ComparatorChange
            && this.playerUUID.equals(((ComparatorChange)action).playerUUID)
            && this.coord.compareTo(action.coord);
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} changed the comparator state {amount} times", this.playerName, this.countAttached());
        }
        if (this.newBlock == REDSTONE_COMPARATOR_ON)
        {
            return user.getTranslation(POSITIVE, "{user} activated the comparator", this.playerName);
        }
        return user.getTranslation(POSITIVE, "{user} deactivated the comparator", this.playerName);
    }
}
