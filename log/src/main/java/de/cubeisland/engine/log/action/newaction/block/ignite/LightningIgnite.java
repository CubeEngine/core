package de.cubeisland.engine.log.action.newaction.block.ignite;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

public class LightningIgnite extends BlockActionType<BlockIgniteListener>
{
    // return "lightning-ignite";
    // return this.lm.getConfig(world).block.ignite.LIGHTNING_IGNITE_enable;


    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof LightningIgnite;
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
                                    "A fire got set by a lightning strike",
                                    "{amount} fires got set by lightning strikes",
                                    amount);
    }
}
