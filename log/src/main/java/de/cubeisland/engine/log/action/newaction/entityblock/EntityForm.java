package de.cubeisland.engine.log.action.newaction.entityblock;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.EntityBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents an Entity forming a block
 * <p>This will usually be a SnowGolem making snow
 */
public class EntityForm extends EntityBlockActionType<EntityBlockListener>
{
    // return "entity-form";
    // return this.lm.getConfig(world).block.form.ENTITY_FORM_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof EntityBreakBlock
            && ((EntityBreakBlock)action).entityType == this.entityType
            && ((EntityBreakBlock)action).newBlock == this.newBlock;
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            int count = this.countUniqueEntities();
            return user.getTranslationN(POSITIVE, count,
                        "{text:One} {name#entity} formed {name#block} x{amount}!",
                        "{3:amount} {name#entity} formed {name#block} x{amount}!",
                        this.entityType.name(), this.oldBlock.name(), this.getAttached().size() + 1, count);
        }
        else
        {
            return user.getTranslation(POSITIVE, "A {name#entity} formed {name#block}",
                                       this.entityType.name(), this.oldBlock.name());
        }
    }
}
