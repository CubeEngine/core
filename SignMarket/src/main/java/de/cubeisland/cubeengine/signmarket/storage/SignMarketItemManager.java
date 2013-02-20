package de.cubeisland.cubeengine.signmarket.storage;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.signmarket.Signmarket;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

import java.util.Collection;

public class SignMarketItemManager extends SingleKeyStorage<Long, SignMarketItemModel>
{
    private static final int REVISION = 1;

    private TLongObjectHashMap<SignMarketItemModel> itemInfoModels = new TLongObjectHashMap<SignMarketItemModel>();

    public SignMarketItemManager(Signmarket module)
    {
        super(module.getDatabase(), SignMarketItemModel.class, REVISION);
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

    public void storeModel(SignMarketItemModel itemInfo)
    {
        this.store(itemInfo);
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
            }
        }
    }

    public void deleteModel(SignMarketItemModel itemInfo) {
        this.itemInfoModels.remove(itemInfo.key);
        this.delete(itemInfo);
    }
}
