package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockManager;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockModel;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketItemManager;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketItemModel;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.bukkit.Location;

public class MarketSignFactory
{
    private THashMap<Location, MarketSign> marketSigns = new THashMap<Location, MarketSign>();

    private SignMarketItemManager signMarketItemManager;
    private SignMarketBlockManager signMarketBlockManager;

    private final Signmarket module;

    public MarketSignFactory(Signmarket module)
    {
        this.module = module;
        this.signMarketItemManager = new SignMarketItemManager(module);
        this.signMarketBlockManager = new SignMarketBlockManager(module);
        this.loadInAllSigns();
    }

    public void loadInAllSigns()
    {
        TLongHashSet usedItemKeys = new TLongHashSet();
        for (SignMarketBlockModel blockModel : this.signMarketBlockManager.getLoadedModels())
        {
            MarketSign marketSign = new MarketSign(module,blockModel.getLocation());
            SignMarketItemModel itemModel = this.signMarketItemManager.getInfoModel(blockModel.itemKey);
            if (itemModel == null)
            {
                this.module.getLogger().warning("Inconsistent Data! BlockInfo withoit Marketsigninfo!");
            }
            marketSign.setBlockInfo(blockModel);
            marketSign.setItemInfo(itemModel);
            usedItemKeys.add(blockModel.itemKey);
            this.marketSigns.put(blockModel.getLocation(),marketSign);
        }
        this.signMarketItemManager.deleteUnusedModels(usedItemKeys);
    }

    public MarketSign getSignAt(Location location)
    {
        if (location == null)
            return null;
        MarketSign result = this.marketSigns.get(location);
        return result;
    }

    public MarketSign createSignAt(Location location)
    {
        MarketSign marketSign = this.getSignAt(location);
        if (marketSign != null)
        {
            this.module.getLogger().warning("Tried to create sign at occupied position!");
            return marketSign;
        }
        marketSign = new MarketSign(this.module, location);
        this.marketSigns.put(marketSign.getLocation(), marketSign);
        return marketSign;
    }

    public void delete(MarketSign marketSign)
    {
        this.marketSigns.remove(marketSign.getLocation());
        this.signMarketBlockManager.deleteModel(marketSign.getBlockInfo());
        SignMarketItemModel itemInfo = marketSign.getItemInfo();
        itemInfo.removeSign(marketSign);
        if (itemInfo.isNotReferenced())
        {
            this.signMarketItemManager.deleteModel(itemInfo);
        }

    }

    public void syncAndSaveSign(MarketSign marketSign)
    {
        for (MarketSign sign : this.marketSigns.values())
        {
            if (marketSign.getOwner() == sign.getOwner() && marketSign != sign) // same owner (but not same sign)
            {
                if (marketSign.getItemInfo().canSync(sign.getItemInfo())) // both have stock AND same item
                {
                    SignMarketItemModel itemModel = marketSign.setItemInfo(sign.getItemInfo()); // change itemInfo (registers the sign in the itemInfo automaticly)
                    if (marketSign.getBlockInfo().key == -1) // blockInfo not saved in database
                    {
                        this.signMarketBlockManager.storeModel(marketSign.getBlockInfo());
                    }
                    else // update
                    {
                        this.signMarketBlockManager.update(marketSign.getBlockInfo());
                    }
                    if (itemModel.isNotReferenced())
                    {
                        this.signMarketItemManager.delete(itemModel); // delete if no more referenced
                    }
                    marketSign.getItemInfo().updateSigns(); // update all signs that use the same itemInfo
                    return;
                }
            }
        }
        if (marketSign.getItemInfo().key == -1) // itemInfo not saved in database
        {
            this.signMarketItemManager.storeModel(marketSign.getItemInfo());
            marketSign.getBlockInfo().itemKey = marketSign.getItemInfo().key; // set correct reference in blockInfo
        }
        else // update
        {
            this.signMarketItemManager.update(marketSign.getItemInfo());
        }
        if (marketSign.getBlockInfo().key == -1) // blockInfo not saved in database
        {
            this.signMarketBlockManager.storeModel(marketSign.getBlockInfo());
        }
        else // update
        {
            this.signMarketBlockManager.update(marketSign.getBlockInfo());
        }
        marketSign.getItemInfo().updateSigns(); // update all signs that use the same itemInfo
    }
}
