package de.cubeisland.engine.log.action.newaction.death;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.entity.EntityBlockActionType.EntitySection;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType.PlayerSection;

public class KillAction extends ActionTypeBase<DeathListener>
{
    public PlayerSection playerKiller = null;
    public EntitySection entityKiller = null;
    public DamageCause otherKiller = null; // TODO converter ?
    public boolean projectile = false;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        if (action instanceof KillAction)
        {
            if (this.playerKiller != null && ((KillAction)action).playerKiller != null)
            {
                return this.playerKiller.equals(((KillAction)action).playerKiller);
            }
            if (this.entityKiller != null && ((KillAction)action).entityKiller != null)
            {
                return this.entityKiller.isSameType(((KillAction)action).entityKiller);
            }
            if (this.otherKiller != null && ((KillAction)action).otherKiller != null)
            {
                return this.otherKiller == ((KillAction)action).otherKiller;
            }
        }
        return false;
    }

    @Override
    public String translateAction(User user)
    {
        return null;
    }
}
