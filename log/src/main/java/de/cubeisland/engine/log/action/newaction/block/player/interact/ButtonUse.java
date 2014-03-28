package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player using a button
 */
public class ButtonUse extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "button-use";
    // return this.lm.getConfig(world).BUTTON_USE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof ButtonUse
            && this.player.equals(((PlayerBlockActionType)action).player)
            && this.oldBlock == ((ButtonUse)action).oldBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{user} pressed a {name#block}",
                                    "{user} pressed a {name#block} {amount} times",
                                    this.player.name, this.oldBlock.name(), count);
    }
}
