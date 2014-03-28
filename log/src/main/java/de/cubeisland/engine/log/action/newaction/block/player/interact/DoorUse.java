package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player using a door
 */
public class DoorUse extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "door-use";
    // return this.lm.getConfig(world).block.DOOR_USE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof DoorUse
            && this.player.equals(((PlayerBlockActionType)action).player)
            && this.oldBlock == ((DoorUse)action).oldBlock;
    }

    @Override
    public String translateAction(User user)
    {
        // TODO plurals
        if (!((logEntry.getOldBlock().data & 0x4) == 0x4))
        {
            return user.getTranslation(POSITIVE, "{user} opened the {name#block}", this.player.name, this.oldBlock
                .name());
        }
        else
        {
            return user.getTranslation(POSITIVE, "{user} closed the {name#block}", this.player.name, this.oldBlock
                .name());
        }
    }
}
