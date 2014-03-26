package de.cubeisland.engine.log.action.newaction.entityblock;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.EntityBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents an Enderman placing a block
 */
public class EndermanPlace extends EntityBlockActionType<EntityBlockListener>
{
    // return "enderman-place";
    // return this.lm.getConfig(world).block.enderman.ENDERMAN_PLACE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof EndermanPlace && ((EndermanPlace)action).newBlock == this.newBlock;
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            int endermanCount = this.countUniqueEntities();
            return user.getTranslationN(POSITIVE, endermanCount,
                        "{text:One Enderman} placed {name#block} x{amount}!",
                        "{2:amount} {text:Enderman} placed {name#block} x{amount}!",
                        this.oldBlock.name(), this.getAttached().size() + 1, endermanCount);
        }
        else
        {
            return user.getTranslation(POSITIVE, "An {text:Enderman} placed {name#block}", this.oldBlock.name());
        }
    }
}
