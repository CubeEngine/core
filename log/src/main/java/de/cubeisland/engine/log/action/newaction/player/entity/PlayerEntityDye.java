package de.cubeisland.engine.log.action.newaction.player.entity;

import org.bukkit.DyeColor;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player dyeing an entity
 */
public class PlayerEntityDye extends PlayerEntityActionType
{
    // return "entity-dye";
    // return this.lm.getConfig(world).ENTITY_DYE_enable;

    private DyeColor color; // TODO converter ?

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerEntityDye
            && ((PlayerEntityDye)action).playerUUID.equals(this.playerUUID)
            && ((PlayerEntityDye)action).entityType == this.entityType
            && ((PlayerEntityDye)action).color == this.color;
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
                                    "{user} dyed a {name#entity} in {input#color}",
                                    "{user} dyed {3:amount} {name#entity} in {input#color}",
                                    this.playerName, this.entityType.name(), this.color.name(), amount);
    }

    public void setColor(DyeColor color)
    {
        this.color = color;
    }
}
