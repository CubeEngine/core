package de.cubeisland.cubeengine.signmarket.storage;

import java.util.Collection;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.signmarket.Signmarket;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

public class SignMarketItemManager extends SingleKeyStorage<Long, SignMarketItemModel>
{
    private static final int REVISION = 1;

    private TLongObjectHashMap<SignMarketItemModel> itemInfoModels = new TLongObjectHashMap<SignMarketItemModel>();
    private final Signmarket module;

    public SignMarketItemManager(Signmarket module)
    {
        super(module.getCore().getDB(), SignMarketItemModel.class, REVISION);
        this.module = module;
        this.initialize();
        for (SignMarketItemModel model : this.getAll())
        {
            this.itemInfoModels.put(model.key, model);
        }
    }

    public SignMarketItemModel getInfoModel(long key)
    {
        return this.itemInfoModels.get(key);
    }

    public Collection<SignMarketItemModel> getModels()
    {
        return this.itemInfoModels.valueCollection();
    }

    @Override
    public void store(SignMarketItemModel itemInfo)
    {
        super.store(itemInfo);
        this.itemInfoModels.put(itemInfo.key, itemInfo);
    }

    public void deleteUnusedModels(TLongHashSet usedKeys)
    {
        for (long key : this.itemInfoModels.keys())
        {
            if (!usedKeys.contains(key))
            {
                this.deleteByKey(key);
                this.itemInfoModels.remove(key);
                this.module.getLog().log(LogLevel.DEBUG,"Removed unused sign ID " + key);
            }
        }
    }

    @Override
    public void delete(SignMarketItemModel itemInfo) {
        this.itemInfoModels.remove(itemInfo.key);
        super.delete(itemInfo);
        System.out.print("Deleted SMItemModel!"); // TODO remove
    }
}
