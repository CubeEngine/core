package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.Material.SOIL;

/**
 * Represents a player trampling crops
 */
public class CropTrample extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    //return "crop-trample";
    // return this.lm.getConfig(world).block.CROP_TRAMPLE_enable;


    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof CropTrample
            && !this.hasAttached()
            && this.playerUUID.equals(((CropTrample)action).playerUUID)
            && 50 > Math.abs(this.date.getTime() - action.date.getTime())
            && this.coord.worldUUID.equals(action.coord.worldUUID)
            && Math.abs(this.coord.y - action.coord.y) == 1;
        // TODO xz check just to make sure?
    }

    @Override
    public String translateAction(User user)
    {
        CropTrample action = this;
        if (this.hasAttached())
        {
            if (this.oldBlock == SOIL)
            {
                // replacing SOIL log with the crop log as the destroyed SOIL is implied
                action = (CropTrample)this.getAttached().get(0);
            }
        }
        return user.getTranslation(POSITIVE, "{user} trampeled down {name#block}", action.playerName, action.oldBlock.name());
    }
}
