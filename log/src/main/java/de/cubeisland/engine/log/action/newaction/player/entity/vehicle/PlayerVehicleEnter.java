package de.cubeisland.engine.log.action.newaction.player.entity.vehicle;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player entering a vehicle
 */
public class PlayerVehicleEnter extends PlayerVehicleActionType
{
    // return "vehicle-enter";
    // return this.lm.getConfig(world).VEHICLE_ENTER_enable;

    // TODO entity vehicle enter

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerVehicleEnter
            && this.player.equals(((PlayerVehicleEnter)action).player)
            && ((PlayerVehicleEnter)action).vehicleUUID.equals(this.vehicleUUID);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{user} entered a {name#vehicle}",
                                    "{user} entered a {name#vehicle} {amount} times",
                                    this.player.name, this.vehicleType.name(), count);
    }
}
