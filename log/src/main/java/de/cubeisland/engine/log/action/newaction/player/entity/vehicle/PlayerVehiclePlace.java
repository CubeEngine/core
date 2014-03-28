package de.cubeisland.engine.log.action.newaction.player.entity.vehicle;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player placing a vehicle like minecart or boat
 */
public class PlayerVehiclePlace extends PlayerVehicleActionType
{
    // return "vehicle-place";
    // return this.lm.getConfig(world).VEHICLE_PLACE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerVehicleBreak
            && this.player.equals(((PlayerVehicleBreak)action).player)
            && ((PlayerVehicleBreak)action).vehicleType == this.vehicleType;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{user} placed a {name#vehicle}",
                                    "{user} placed {2:amount} {name#vehicle}",
                                    this.player.name, this.vehicleType.name(), count);
    }
}
