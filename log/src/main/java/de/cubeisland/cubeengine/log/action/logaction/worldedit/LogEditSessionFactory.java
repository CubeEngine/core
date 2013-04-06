package de.cubeisland.cubeengine.log.action.logaction.worldedit;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.ActionType_old;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.bukkit.BukkitWorld;

public class LogEditSessionFactory extends EditSessionFactory
{
    private final Log module;
    private final EditSessionFactory oldFactory;

    public LogEditSessionFactory(Log module, EditSessionFactory oldFactory)
    {
        this.module = module;
        this.oldFactory = oldFactory;
    }

    public static void initialize(WorldEdit worldEdit, Log module)
    {
        try
        {
            worldEdit.setEditSessionFactory(new LogEditSessionFactory(module, worldEdit.getEditSessionFactory()));
        }
        catch (Exception ignore)
        {}
    }

    private boolean ignoreWorldEdit(LocalWorld world)
    {
        return world instanceof BukkitWorld && this.module.getLogManager().isIgnored(((BukkitWorld)world).getWorld(), ActionType_old.WORLDEDIT);
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, LocalPlayer player)
    {
        if (this.ignoreWorldEdit(world))
        {
            return this.oldFactory.getEditSession(world, maxBlocks, player);
        }
        else
        {
            return new LogEditSession(world, maxBlocks, player, this.module);
        }
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player)
    {
        if (this.ignoreWorldEdit(world))
        {
            return this.oldFactory.getEditSession(world, maxBlocks, blockBag, player);
        }
        else
        {
            return new LogEditSession(world, maxBlocks, blockBag, player, this.module);
        }
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks)
    {
        if (this.ignoreWorldEdit(world))
        {
            return this.oldFactory.getEditSession(world, maxBlocks);
        }
        else
        {
            return new LogEditSession(world, maxBlocks, this.module);
        }
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag)
    {
        if (this.ignoreWorldEdit(world))
        {
            return this.oldFactory.getEditSession(world, maxBlocks, blockBag);
        }
        else
        {
            return new LogEditSession(world, maxBlocks, blockBag, this.module);
        }
    }

}
