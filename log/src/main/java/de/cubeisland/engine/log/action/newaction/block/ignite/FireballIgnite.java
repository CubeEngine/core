package de.cubeisland.engine.log.action.newaction.block.ignite;


import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.entity.EntityType.GHAST;
import static org.bukkit.entity.EntityType.PLAYER;

/**
 * Represents a fireball setting a block on fire
 */
public class FireballIgnite extends BlockActionType<BlockIgniteListener>
{
    // return "fireball-ignite";
    // return this.lm.getConfig(world).block.ignite.FIREBALL_IGNITE_enable;

    public UUID shooterUUID;
    public EntityType shooterType;

    public UUID playerUUID;
    public String playerName;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof FireballIgnite
            // No Shooter or same Shooter
            && ((this.shooterUUID == null && ((FireballIgnite)action).shooterUUID == null)
             || (this.shooterUUID != null && this.shooterUUID.equals(((FireballIgnite)action).shooterUUID)))
            // No Player or same Player
            && ((this.playerUUID == null && ((FireballIgnite)action).playerUUID == null)
             || (this.playerUUID != null && this.playerUUID.equals(((FireballIgnite)action).playerUUID)));
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (shooterType == PLAYER)
        {
            return user.getTranslationN(POSITIVE, count,
                                        "{user} shot a fireball setting this block on fire",
                                        "{user} shot fireballs setting {amount} blocks on fire",
                                        this.playerName, count);
        }
        if (shooterType == GHAST)
        {
            if (playerName == null)
            {
                return user.getTranslationN(POSITIVE, count,
                                            "A Ghast shot a fireball setting this block on fire",
                                            "A Ghast shot fireballs setting {amount} blocks on fire",
                                            count);
            }
            return user.getTranslationN(POSITIVE, count,
                                        "A Ghast shot a fireball at {user} setting this block on fire",
                                        "A Ghast shot fireballs at {user} setting {amount} blocks on fire",
                                        this.playerName, count);
        }
        return user.getTranslationN(POSITIVE, count,
                                    "A fire got set by a fireball",
                                    "{amount} fires got set by fireballs",
                                    count);

    }

    public void setShooter(Entity entity)
    {
        this.shooterUUID = entity.getUniqueId();
        this.shooterType = entity.getType();
    }

    public void setPlayer(Player player)
    {
        this.playerName = player.getName();
        this.playerUUID = player.getUniqueId();
    }
}
