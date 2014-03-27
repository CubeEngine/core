package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player stepping on a plate
 */
public class PlateStep extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "plate-step";
    // return this.lm.getConfig(world).PLATE_STEP_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlateStep
            && this.playerUUID.equals(((PlateStep)action).playerUUID)
            && this.coord.compareTo(action.coord)
            && this.oldBlock == ((PlateStep)action).oldBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                   "{user} stepped on a {name#block}",
                                   "{user} stepped on a {name#block} {amount} times",
                                   this.playerName, this.oldBlock.name(), count);
    }
}
