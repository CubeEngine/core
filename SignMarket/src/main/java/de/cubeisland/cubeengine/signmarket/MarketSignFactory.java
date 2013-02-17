package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockModel;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketInfoModel;
import gnu.trove.map.hash.THashMap;
import org.bukkit.Location;

public class MarketSignFactory
{
    private THashMap<Location, MarketSign> marketSigns = new THashMap<Location, MarketSign>();
    private final Signmarket module;

    public MarketSignFactory(Signmarket module)
    {
        this.module = module;
    }

    public MarketSign getSignAt(Location location)
    {
        if (location == null)
            return null;
        MarketSign result = this.marketSigns.get(location);
        if (result == null)
        {
            Long marketSignId = this.module.getSmblockManager().getMarketSignID(location);
            if (marketSignId == null)
            {
                return null;
            }
            SignMarketInfoModel infoModel = this.module.getSminfoManager().get(marketSignId);
            if (infoModel == null)
                return null;
            result = new MarketSign(module, location);
            result.setInfoModel(infoModel);
            result.setBlockModel(new SignMarketBlockModel(marketSignId, location));
            this.marketSigns.put(location, result);
        }
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
}
