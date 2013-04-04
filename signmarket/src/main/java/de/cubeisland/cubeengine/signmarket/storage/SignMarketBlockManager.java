package de.cubeisland.cubeengine.signmarket.storage;

import java.util.Collection;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.signmarket.Signmarket;

import gnu.trove.map.hash.THashMap;

public class SignMarketBlockManager extends SingleKeyStorage<Long, SignMarketBlockModel>
{
    private static final int REVISION = 1;

    private THashMap<Location,SignMarketBlockModel> blockModels = new THashMap<Location, SignMarketBlockModel>();

    public SignMarketBlockManager(Signmarket module)
    {
        super(module.getCore().getDB(), SignMarketBlockModel.class, REVISION);
        this.initialize();
        Collection<SignMarketBlockModel> models;
        for (SignMarketBlockModel model : this.getAll())
        {
            this.blockModels.put(model.getLocation(),model);
        }
    }

    public Collection<SignMarketBlockModel> getLoadedModels()
    {
        return this.blockModels.values();
    }

    @Override
    public void delete(SignMarketBlockModel model)
    {
        this.blockModels.remove(model.getLocation());
        super.delete(model);
        System.out.print("Deleted SMBlockModel!"); // TODO remove
    }

    @Override
    public void store(SignMarketBlockModel blockModel)
    {
        this.blockModels.put(blockModel.getLocation(),blockModel);
        super.store(blockModel);
    }
}
