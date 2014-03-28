package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player igniting TnT with a lighter
 */
public class TntPrime extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "tnt-prime";
    // return this.lm.getConfig(world).block.TNT_PRIME_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return false;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{user} ignited one TNT",
                                    "{user} ignited {amount} TNT",
                                    this.player.name, count);
    }
}
