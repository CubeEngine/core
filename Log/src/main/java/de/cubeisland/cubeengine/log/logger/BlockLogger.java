package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public abstract class BlockLogger<T extends SubLogConfig> extends Logger<T>
{
    public BlockLogger(Log module, Class<T> configClass)
    {
        super(module,configClass);
    }

    public void logBlockChange(BlockChangeCause cause, World world, Player player, BlockState oldState, BlockState newState)
    {
        if (oldState == newState)
        {
            if (oldState != null && newState != null
                    && (oldState.getType().equals(newState.getType())
                    && oldState.getRawData() == newState.getRawData()))
            {
                return;
            }
        }
        if (oldState != null && oldState.getTypeId() == 0)
        {
            oldState = null;
        }
        if (newState != null && newState.getTypeId() == 0)
        {
            newState = null;
        }
        if (cause == BlockChangeCause.PLAYER || player != null)
        {
            User user = this.module.getUserManager().getExactUser(player);
            this.module.getLogManager().logBlockLog(cause, user.getKey().intValue(), world, newState, oldState);
        }
        else
        {
            this.module.getLogManager().logBlockLog(cause, cause.getId(), world,  newState, oldState);
        }
    }

    public static enum BlockChangeCause
    {
        PLAYER(
            -1),
        LAVA(
            -2),
        WATER(
            -3),
        EXPLOSION(
            -4),
        FIRE(
            -5),
        ENDERMAN(
            -6),
        FADE(
            -7),
        FORM(
            -7),
        DECAY(
            -8),
        GROW(
            -8),
        WITHER(
            -9);

        private BlockChangeCause(int causeID)
        {
            this.causeID = causeID;
        }

        final private int causeID;

        public int getId()
        {
            return causeID;
        }
    }
}
