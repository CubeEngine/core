package de.cubeisland.engine.log.action.newaction.player.entity.vehicle;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player breaking a vehicle
 */
public class PlayerVehicleBreak extends PlayerVehicleActionType
{
    // return "vehicle-break";
    // return this.lm.getConfig(world).VEHICLE_BREAK_enable;

    // TODO actionType entity / block breakVechicle

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerVehicleBreak
            && ((PlayerVehicleBreak)action).playerUUID.equals(this.playerUUID)
            && ((PlayerVehicleBreak)action).vehicleType == this.vehicleType;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{user} broke a {name#vehicle}",
                                    "{user} broke {2:amount} {name#vehicle}",
                                    this.playerName, this.vehicleType.name(), count);
    }
}
