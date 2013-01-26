package de.cubeisland.cubeengine.log.logger.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import de.cubeisland.cubeengine.log.Log;

public class LogEditSession extends EditSession {

    private LocalPlayer player;
    private Log module;

    public LogEditSession(LocalWorld world, int maxBlocks, LocalPlayer player, Log module) {
        super(world, maxBlocks);
        this.player =player;
        this.module = module;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player, Log module) {
        super(world, maxBlocks, blockBag);
        this.player =player;
        this.module = module;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, Log module) {
        super(world,maxBlocks);
        this.module = module;
        this.player = null;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, Log module) {
        super(world,maxBlocks,blockBag);
        this.module = module;
        this.player = null;
    }

    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block) {
        //TODO do log the changes
        return super.rawSetBlock(pt, block);
    }
}
