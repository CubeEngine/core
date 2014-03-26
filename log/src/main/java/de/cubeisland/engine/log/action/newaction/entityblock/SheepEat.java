package de.cubeisland.engine.log.action.newaction.entityblock;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.EntityBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a Sheep eating grass
 */
public class SheepEat extends EntityBlockActionType<EntityBlockListener>
{
    // return "sheep-eat";
    // return this.lm.getConfig(world).block.SHEEP_EAT_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof SheepEat;
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            int count = this.countUniqueEntities();
            return user.getTranslationN(POSITIVE, count,
                        "{text:One sheep} ate {text:grass} x{amount}!",
                        "{1:amount} {text:sheep} ate {text:grass} x{amount}!",
                        this.getAttached().size() + 1, count);
        }
        else
        {
            return user.getTranslation(POSITIVE, "A {text#sheep} ate {text:grass}");
        }
    }
}
