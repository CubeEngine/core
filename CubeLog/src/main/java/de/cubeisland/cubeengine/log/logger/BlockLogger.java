package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.LogAction;
import de.cubeisland.cubeengine.log.Logger;
import de.cubeisland.cubeengine.log.SubLogConfig;
import de.cubeisland.cubeengine.log.storage.BlockLog;
import de.cubeisland.cubeengine.log.storage.BlockLogManager;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public abstract class BlockLogger<T extends SubLogConfig> extends Logger<T>
{
    private BlockLogManager blockLogManager;

    public BlockLogger()
    {
        super(LogAction.BLOCKCHANGE);
        this.blockLogManager = new BlockLogManager(module.getDatabase());
    }

    public void logBlockChange(BlockChangeCause cause, Player player, BlockState oldState, BlockState newState)
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
        if (cause == BlockChangeCause.PLAYER)
        {
            User user = module.getUserManager().getExactUser(player);
            this.blockLogManager.store(new BlockLog(user.getKey(), newState, oldState));
        }
        else
        {
            this.blockLogManager.store(new BlockLog(cause.getId(), newState, oldState));
        }
    }

    public static enum BlockChangeCause
    {
        PLAYER(-1),
        LAVA(-2),
        WATER(-3),
        EXPLOSION(-4),
        FIRE(-5),
        ENDERMAN(-6),
        FADE(-7), FORM(-7),
        DECAY(-8), GROW(-8),
        WITHER(-9);

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
