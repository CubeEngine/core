package de.cubeisland.engine.log.action.newaction.block.ignite;

import org.bukkit.Location;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a fire spreading
 */
public class FireSpread extends BlockActionType<BlockIgniteListener>
{
    //return  "fire-spread";
    //return this.lm.getConfig(world).block.spread.FIRE_SPREAD_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof FireSpread;
        // TODO attach if loc or source in common of any action attached
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "Fire spread to this block",
                                    "Fire spread to {amount} blocks",
                                    count);
    }

    public void setSource(Location source)
    {
        // TODO
    }
}
