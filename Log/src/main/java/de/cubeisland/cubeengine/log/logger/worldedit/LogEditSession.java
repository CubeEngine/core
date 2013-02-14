package de.cubeisland.cubeengine.log.logger.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import de.cubeisland.cubeengine.log.Log;
import org.bukkit.World;
import org.bukkit.block.BlockState;

public class LogEditSession extends EditSession
{

    private LocalPlayer player;
    private Log module;

    public LogEditSession(LocalWorld world, int maxBlocks, LocalPlayer player, Log module)
    {
        super(world, maxBlocks);
        this.player = player;
        this.module = module;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player, Log module)
    {
        super(world, maxBlocks, blockBag);
        this.player = player;
        this.module = module;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, Log module)
    {
        super(world, maxBlocks);
        this.module = module;
        this.player = null;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, Log module)
    {
        super(world, maxBlocks, blockBag);
        this.module = module;
        this.player = null;
    }

    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block)
    {
        if (player instanceof BukkitPlayer)
        {
            if (player.getWorld() instanceof BukkitWorld)
            {
                World world = ((BukkitWorld)player.getWorld()).getWorld();
                WorldEditLogger logger = module.getLoggerManager().getLogger(WorldEditLogger.class);
                if (logger.getConfig(world).enabled)
                {
                    BlockState oldState = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getState();
                    boolean success = super.rawSetBlock(pt, block);
                    if (success)
                    {
                        logger.logWorldEditChange(((BukkitPlayer)player).getPlayer(), oldState, world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getState());
                    }
                    return success;
                }
            }
        }
        return super.rawSetBlock(pt, block);
    }
}
