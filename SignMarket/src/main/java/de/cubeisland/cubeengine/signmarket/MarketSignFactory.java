package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockModel;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketInfoModel;
import gnu.trove.map.hash.THashMap;
import org.bukkit.Location;

public class MarketSignFactory
{
    private THashMap<Location,MarketSign> marketSigns = new THashMap<Location, MarketSign>();
    private final Signmarket module;

    public MarketSignFactory(Signmarket module) {
        this.module = module;
    }

    public MarketSign getSignAt(Location location)
    {
        MarketSign result = this.marketSigns.get(location);
        if (result == null)
        {
            Long marketSignId = this.module.getSmblockManager().getMarketSignID(location);
            if (marketSignId == null)
            {
                return null;
            }
            SignMarketInfoModel infoModel = this.module.getSminfoManager().get(marketSignId);
            result = new MarketSign(module,location);
            result.setInfoModel(infoModel);
            result.setBlockModel(new SignMarketBlockModel(marketSignId,location));
            this.marketSigns.put(location,result);
        }
        return result;
    }

}
