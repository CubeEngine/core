package de.cubeisland.cubeengine.log.listeners.worldedit;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bags.BlockBag;
import de.cubeisland.cubeengine.log.Log;

public class LogEditSessionFactory extends EditSessionFactory
{

    private Log module;

    public LogEditSessionFactory(Log module)
    {
        this.module = module;

    }

    public static void initialize(WorldEdit worldEdit, Log module)
    {
        try
        {
            worldEdit.setEditSessionFactory(new LogEditSessionFactory(module));
        }
        catch (Exception ignore)
        {}
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, LocalPlayer player)
    {
        return new LogEditSession(world, maxBlocks, player, module);
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player)
    {
        return new LogEditSession(world, maxBlocks, blockBag, player, module);
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks)
    {
        return new LogEditSession(world, maxBlocks, module);
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag)
    {
        return new LogEditSession(world, maxBlocks, blockBag, module);
    }

}
