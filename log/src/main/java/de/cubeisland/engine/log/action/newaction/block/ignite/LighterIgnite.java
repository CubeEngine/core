package de.cubeisland.engine.log.action.newaction.block.ignite;

import java.util.UUID;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a fire being set using a lighter
 */
public class LighterIgnite extends BlockActionType<BlockIgniteListener>
{
    // return  "lighter-ignite";
    // return this.lm.getConfig(world).block.ignite.LIGHTER_IGNITE_enable;

    public UUID playerUUID;
    public String playerName;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof LighterIgnite
            && ((this.playerUUID == null
              && ((LighterIgnite)action).playerUUID == null)
             || (this.playerUUID != null
              && this.playerUUID.equals(((LighterIgnite)action).playerUUID)));
    }

    @Override
    public String translateAction(User user)
    {
        int amount = 1;
        if (this.hasAttached())
        {
            amount += this.getAttached().size();
        }
        if (this.playerName == null)
        {
            return user.getTranslationN(POSITIVE, amount,
                                        "A fire got set by a lighter",
                                        "{amount} fires got set using lighters",
                                        amount);
        }
        return user.getTranslationN(POSITIVE, amount,
                                    "{user} set fire",
                                    "{user} set {amount} fires",
                                    this.playerName, amount);
    }

    public void setPlayer(Player player)
    {
        this.playerName = player.getName();
        this.playerUUID = player.getUniqueId();
    }
}
