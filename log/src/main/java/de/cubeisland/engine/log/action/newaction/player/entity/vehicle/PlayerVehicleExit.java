package de.cubeisland.engine.log.action.newaction.player.entity.vehicle;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player exiting a vehicle
 */
public class PlayerVehicleExit extends PlayerVehicleActionType
{
    // return "vehicle-exit";
    // return this.lm.getConfig(world).VEHICLE_EXIT_enable;

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
                                    "{user} exited a {name#vehicle}",
                                    "{user} exited a {name#vehicle} {amount} times",
                                    this.player.name, this.vehicleType.name(), count);
    }
}
