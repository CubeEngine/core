package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player accessing an {@link org.bukkit.inventory.InventoryHolder}
 */
public class ContainerAccess extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // TODO no rollback/redo

    // return "container-access";
    // return this.lm.getConfig(world).container.CONTAINER_ACCESS_enable;


    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof ContainerAccess
            && this.playerUUID.equals(((ContainerAccess)action).playerUUID)
            && this.coord.compareTo(action.coord);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{user} looked into a {name#container}",
                                    "{user} looked into {2:amount} {name#container}",
                                    this.playerName, this.oldBlock.name(), count);
    }
}
