package de.cubeisland.cubeengine.itemrepair.repair.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.Block;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;

public class RepairBlockPersister extends SingleKeyStorage<Long,RepairBlockModel>
{
    private Map<Block,RepairBlockModel> models = new HashMap<Block, RepairBlockModel>();
    private final Module module;

    public RepairBlockPersister(Module module)
    {
        super(module.getCore().getDB(), RepairBlockModel.class, 1);
        this.module = module;
        this.initialize();
    }

    public void deleteByBlock(Block block)
    {
        RepairBlockModel repairBlockModel = this.models.get(block);
        if (repairBlockModel != null)
        {
            this.delete(repairBlockModel);
        }
        else
        {
            this.module.getLog().warning("Could not delete model by block!");
        }
    }

    @Override
    public Collection<RepairBlockModel> getAll()
    {
        Collection<RepairBlockModel> all = super.getAll();
        for (RepairBlockModel repairBlockModel : all)
        {
            this.models.put(repairBlockModel.getBlock(this.module.getCore().getWorldManager()),repairBlockModel);
        }
        return all;
    }
}
