package de.cubeisland.engine.log.action.newaction.block;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.Material.AIR;

/**
 * Represents trees or mushrooms growing
 */
public class NaturalGrow extends BlockActionType<BlockListener>
{
    // return "natural-grow";
    // return this.lm.getConfig(world).block.grow.NATURAL_GROW_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof NaturalGrow
            && ((NaturalGrow)action).oldBlock == this.oldBlock
            && ((NaturalGrow)action).newBlock == this.newBlock;
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
                                        "{name#block} grew naturally",
                                        "{1:amount}x {name#block} grew naturally",
                                        this.newBlock.name(), amount);
        }
        return user.getTranslationN(POSITIVE, amount,
                                    "{name#block} grew naturally into {name#block}",
                                    "{2:amount}x {name#block} grew naturally into {name#block}",
                                    this.newBlock.name(), this.oldBlock.name(), amount);
    }
}
