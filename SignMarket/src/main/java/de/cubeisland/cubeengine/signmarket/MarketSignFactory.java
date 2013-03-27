package de.cubeisland.cubeengine.signmarket;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockManager;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockModel;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketItemManager;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketItemModel;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.TLongHashSet;

public class MarketSignFactory
{
    private THashMap<Location, MarketSign> marketSigns = new THashMap<Location, MarketSign>();

    private SignMarketItemManager signMarketItemManager;
    private SignMarketBlockManager signMarketBlockManager;

    private final Signmarket module;
    private final Conomy conomy;

    public MarketSignFactory(Signmarket module, Conomy conomy)
    {
        this.module = module;
        this.conomy = conomy;
        this.signMarketItemManager = new SignMarketItemManager(module);
        this.signMarketBlockManager = new SignMarketBlockManager(module);
        this.loadInAllSigns();
    }

    public void loadInAllSigns()
    {
        TLongHashSet usedItemKeys = new TLongHashSet();
        for (SignMarketBlockModel blockModel : this.signMarketBlockManager.getLoadedModels())
        {
            MarketSign marketSign = new MarketSign(module, this.conomy, blockModel.getLocation());
            SignMarketItemModel itemModel = this.signMarketItemManager.getInfoModel(blockModel.itemKey);
            if (itemModel == null)
            {
                this.module.getLog().warning("Inconsistent Data! BlockInfo withoit Marketsigninfo!");
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

    public MarketSign createSignAt(User user, Location location)
    {
        MarketSign marketSign = this.getSignAt(location);
        if (marketSign != null)
        {
            this.module.getLog().warning("Tried to create sign at occupied position!");
            return marketSign;
        }
        marketSign = new MarketSign(this.module, conomy, location);
        if (this.module.getConfig().allowAdminNoStock)
        {
            marketSign.setAdminSign();
            marketSign.setStock(null);
        }
        else if (this.module.getConfig().allowAdminStock)
        {
            marketSign.setAdminSign();
            marketSign.setStock(null);
        }
        else
        {
            marketSign.setOwner(user);
            marketSign.setStock(0);
        }
        if (marketSign.isAdminSign())
        {
            marketSign.setSize(this.module.getConfig().maxAdminStock);
        }
        else
        {
            marketSign.setSize(this.module.getConfig().maxUserStock);
        }
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
        if (marketSign.hasStock() && !marketSign.hasDemand()) // skip sync if no stock OR limited demand
        {
            for (MarketSign sign : this.marketSigns.values())
            {
                if (sign.hasDemand()) // skip if limited demand
                    continue;
                if (marketSign.getOwner() == sign.getOwner() && marketSign != sign) // same owner (but not same sign)
                {
                    if (marketSign.getItemInfo().canSync(sign.getItemInfo())) // both have stock AND same item
                    {
                        SignMarketItemModel itemModel = marketSign.setItemInfo(sign.getItemInfo()); // change itemInfo (registers the sign in the itemInfo automaticly)
                        if (marketSign.syncOnMe)
                        {
                            marketSign.setStock(itemModel.stock);
                            marketSign.setSize(itemModel.size);
                            marketSign.syncOnMe = false;
                        }
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
                    }
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

    public SignMarketItemManager getSignMarketItemManager() {
        return signMarketItemManager;
    }
}
