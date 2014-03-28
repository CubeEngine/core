package de.cubeisland.engine.log.action.newaction.player.entity;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player shearing a sheep or mooshroom
 */
public class PlayerEntityShear extends PlayerEntityActionType
{
    // return "entity-shear";
    // return this.lm.getConfig(world).ENTITY_SHEAR_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerEntityShear
            && this.player.equals(((PlayerEntityShear)action).player)
            && ((PlayerEntityShear)action).entityType == this.entityType;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{user} sheared {name#entity}",
                                    "{user} sheared {2:amount} {name#entity}",
                                    this.player.name, this.entityType.name(), count);
    }
}
