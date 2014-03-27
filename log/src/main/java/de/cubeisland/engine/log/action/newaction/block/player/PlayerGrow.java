package de.cubeisland.engine.log.action.newaction.block.player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.BlockListener;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.Material.AIR;

/**
 * Represents a player letting a tree or mushroom grow using bonemeal
 */
public class PlayerGrow extends PlayerBlockActionType<BlockListener>
{
    // return "player-grow";
    // return this.lm.getConfig(world).block.grow.PLAYER_GROW_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerGrow
            && ((PlayerGrow)action).playerUUID.equals(this.playerUUID)
            && ((PlayerGrow)action).oldBlock == this.oldBlock
            && ((PlayerGrow)action).newBlock == this.newBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int amount = 1;
        if (this.hasAttached())
        {
            amount += this.getAttached().size();
        }
        if (this.oldBlock == AIR)
        {
            return user.getTranslationN(POSITIVE, amount,
                                        "{user} let grow {name#block}",
                                        "{user} let grow {2:amount}x {name#block}",
                                        this.playerName, this.newBlock.name(), amount);
        }
        return user.getTranslationN(POSITIVE, amount,
                                    "{user} let grow {name#block} into {name#block}",
                                    "{user} let grow {3:amount}x {name#block} into {name#block}",
                                    this.playerName, this.newBlock.name(), this.oldBlock.name(), amount);
    }
}
