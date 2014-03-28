package de.cubeisland.engine.log.action.newaction.block.entity.explosion;

import org.bukkit.entity.Player;

import de.cubeisland.engine.log.action.newaction.block.entity.EntityBlockActionType;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType.PlayerSection;

public abstract class ExplosionActionType extends EntityBlockActionType<ExplodeListener>
{
    public PlayerSection player;

    public void setPlayer(Player player)
    {
        this.player = new PlayerSection(player);
    }
}
