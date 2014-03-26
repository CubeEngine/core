package de.cubeisland.engine.log.action.newaction.entityblock;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.EntityBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;


/**
 * Represents an Entity breaking a block
 * <p>This will usually be a Zombie destroying doors
 */
public class EntityBreakBlock extends EntityBlockActionType<EntityBlockListener>
{
    // return "entity-break";
    // return this.lm.getConfig(world).block.ENTITY_BREAK_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof EntityBreakBlock && ((EntityBreakBlock)action).entityType == this.entityType;
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            int count = this.countUniqueEntities();
            return user.getTranslationN(POSITIVE, count,
                        "{text:One} {name#entity} destroyed {name#block} x{amount}!",
                        "{3:amount} {name#entity} destroyed {name#block} x{amount}!",
                        this.entityType.name(), this.oldBlock.name(), this.getAttached().size() + 1, count);
        }
        else
        {
            return user.getTranslation(POSITIVE, "A {name#entity} destroyed {name#block}",
                                       this.entityType.name(), this.oldBlock.name());
        }
    }
}
