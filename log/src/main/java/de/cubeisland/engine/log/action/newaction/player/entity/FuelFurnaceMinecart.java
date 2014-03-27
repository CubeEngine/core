package de.cubeisland.engine.log.action.newaction.player.entity;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player fueling a furnace-minecart
 */
public class FuelFurnaceMinecart extends PlayerEntityActionType
{
    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof FuelFurnaceMinecart
            && ((FuelFurnaceMinecart)action).playerUUID.equals(this.playerUUID)
            && ((FuelFurnaceMinecart)action).entityUUID.equals(this.entityUUID);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{user} gave fuel to a furnace-minecart",
                                    "{user} gave fuel to a furnace-minecart {amount} times",
                                    this.playerName, count);
    }
}
