package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockModel;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketInfoModel;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class MarketSignFactory
{
    private THashMap<Location, MarketSign> marketSigns = new THashMap<Location, MarketSign>();

    private final Signmarket module;

    public MarketSignFactory(Signmarket module)
    {
        this.module = module;
        this.loadInAllSigns();
    }

    public void loadInAllSigns()
    {
        TLongObjectHashMap<MarketSign> loadedSigns = new TLongObjectHashMap<MarketSign>();
        this.module.getSminfoManager().getAll();
        for (SignMarketBlockModel blockModel : this.module.getSmblockManager().getAll())
        {
            MarketSign sign = new MarketSign(module,blockModel.getLocation());
            sign.setBlockModel(blockModel);
            loadedSigns.put(blockModel.key,sign);
        }
        for (SignMarketInfoModel infoModel : this.module.getSminfoManager().getAll())
        {
            MarketSign sign = loadedSigns.get(infoModel.key);
            if (sign == null)
            {
                this.module.getLogger().warning("Inconsistent Data! MarketSignInfo without BlockInfo!");
            }
            else
            {
                sign.setInfoModel(infoModel);
                this.marketSigns.put(sign.getLocation(),sign);
            }
        }
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
        marketSign.setBlockModel(new SignMarketBlockModel(0L, location));
        this.marketSigns.put(location, marketSign);
        return marketSign;
    }

    public void delete(MarketSign marketSign)
    {
        this.marketSigns.remove(marketSign.getLocation());
        marketSign.deleteFromDatabase();
    }

    public void syncSign(MarketSign marketSign)
    {
        if (marketSign.isAdminSign() && marketSign.hasStock())
        {
            ItemStack item = marketSign.getItem();
            for (MarketSign sign : this.marketSigns.values())
            {
                if (sign.equals(marketSign))
                    continue;
                if (sign.isValidSign(null))
                {
                    if (sign.isAdminSign() && sign.hasStock())
                    {
                        if (item.isSimilar(sign.getItem()))
                        {
                            if (marketSign.isNotSaved())
                            {
                                marketSign.setStock(sign.getStock());
                                return;
                            }
                            else
                            {
                                sign.setStock(marketSign.getStock());
                                sign.updateSign();
                            }
                        }
                    }
                }
            }
        }
    }
}
