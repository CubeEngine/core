/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.signmarket;

import org.bukkit.Location;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.signmarket.storage.SignMarketBlockManager;
import de.cubeisland.engine.signmarket.storage.SignMarketBlockModel;
import de.cubeisland.engine.signmarket.storage.SignMarketItemManager;
import de.cubeisland.engine.signmarket.storage.SignMarketItemModel;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.TLongHashSet;

public class MarketSignFactory
{
    private final THashMap<Location, MarketSign> marketSigns = new THashMap<>();

    private final SignMarketItemManager signMarketItemManager;
    private final SignMarketBlockManager signMarketBlockManager;

    private final Signmarket module;

    public MarketSignFactory(Signmarket module)
    {
        this.module = module;
        this.signMarketItemManager = new SignMarketItemManager(module);
        this.signMarketBlockManager = new SignMarketBlockManager(module);
    }

    public void loadInAllSigns()
    {
        this.signMarketItemManager.load();
        this.signMarketBlockManager.load();
        TLongHashSet usedItemKeys = new TLongHashSet();
        for (SignMarketBlockModel blockModel : this.signMarketBlockManager.getLoadedModels())
        {
            SignMarketItemModel itemModel = this.signMarketItemManager.getInfoModel(blockModel.getItemkey().longValue());
            if (itemModel == null)
            {
                this.module.getLog().warn("Inconsistent Data! BlockInfo without Marketsigninfo!");
                continue;
            }
            MarketSign marketSign = new MarketSign(module, itemModel, blockModel);
            usedItemKeys.add(blockModel.getItemkey().intValue());
            this.marketSigns.put(blockModel.getLocation(),marketSign);
        }
        this.signMarketItemManager.deleteUnusedModels(usedItemKeys);
    }

    public MarketSign getSignAt(Location location)
    {
        if (location == null)
        {
            return null;
        }
        if (this.module.getConfig().disableInWorlds.contains(location.getWorld().getName()))
        {
            return null;
        }
        return this.marketSigns.get(location);
    }

    public MarketSign createSignAt(User user, Location location)
    {
        if (this.module.getConfig().disableInWorlds.contains(location.getWorld().getName()))
        {
            return null;
        }
        MarketSign marketSign = this.getSignAt(location);
        if (marketSign != null)
        {
            this.module.getLog().warn("Tried to create sign at occupied position!");
            return marketSign;
        }
        marketSign = new MarketSign(this.module, location);
        if (module.perms().SIGN_CREATE_ADMIN_CREATE.isAuthorized(user) && module.getConfig().enableAdmin)
        {
            marketSign.setAdminSign();
        }
        else if (module.perms().SIGN_CREATE_USER_CREATE.isAuthorized(user) && module.getConfig().enableUser)
        {
            marketSign.setOwner(user);
        }
        else
        {
            user.sendTranslated(MessageType.NEGATIVE, "You are not allowed to create Admin or User MarketSigns!");
            return null;
        }
        if (marketSign.isAdminSign())
        {
            if (this.module.getConfig().allowAdminNoStock)
            {
                marketSign.setAdminSign();
                marketSign.setNoStock();
            }
            else if (this.module.getConfig().allowAdminStock)
            {
                marketSign.setAdminSign();
                marketSign.setNoStock();
            }
            marketSign.setSize(this.module.getConfig().maxAdminStock);
        }
        else
        {
            marketSign.setSize(this.module.getConfig().maxUserStock);
        }
        this.marketSigns.put(marketSign.getLocation(), marketSign);
        return marketSign;
    }

    /**
     * Deletes a marketSign forever!
     * This will delete the blockModel and the itemModel if it is no longer referenced.
     *
     * @param marketSign
     */
    public void delete(MarketSign marketSign)
    {
        this.marketSigns.remove(marketSign.getLocation());
        this.signMarketBlockManager.delete(marketSign.getBlockInfo());
        this.module.getLog().debug("{} deleted block-model #{}", marketSign.isAdminSign() ? "Server" : marketSign.getOwner().getName(), marketSign.getBlockInfo().getKey());
        SignMarketItemModel itemInfo = marketSign.getItemInfo();
        itemInfo.removeSign(marketSign);
        if (itemInfo.isNotReferenced())
        {
            this.signMarketItemManager.delete(itemInfo);
            this.module.getLog().debug("{} deleted item-model #{}", marketSign.isAdminSign() ? "Server" : marketSign.getOwner().getName(), itemInfo.getKey());
        }
    }

    public void syncAndSaveSign(MarketSign marketSign)
    {
        if (marketSign.getItemInfo().getKey().longValue() == 0 || marketSign.getItemInfo().getReferenced().size() == 1) // de-synced sign OR possibly sync-able sign
        {
            for (MarketSign sign : this.marketSigns.values())
            {
                if (sign.hasDemand()) // skip if limited demand
                {
                    continue;
                }
                if ((marketSign.getRawOwner() == sign.getRawOwner() && marketSign != sign)  // same owner (but not same sign)
                    && marketSign.canSync(sign)) // both have stock AND same item -> doSync
                {
                    // apply the found item-info to the marketsign
                    SignMarketItemModel itemModel = marketSign.setItemInfo(sign.getItemInfo());
                    if (marketSign.syncOnMe) // stock OR stock-size change
                    {
                        marketSign.setStock(itemModel.getStock().intValue());
                        marketSign.setSize(itemModel.getSize().intValue());
                        marketSign.syncOnMe = false;
                    }
                    this.saveOrUpdate(marketSign);
                    this.module.getLog().debug("block-model #{} synced onto the item-model #{} (size: {})" ,
                                               marketSign.getBlockInfo().getKey(), marketSign.getItemInfo().getKey(), marketSign.getItemInfo().getReferenced().size());
                    if (itemModel.getKey().longValue() != 0 && itemModel.isNotReferenced())
                    {
                        this.signMarketItemManager.delete(itemModel); // delete if no more referenced
                        this.module.getLog().debug("{] deleted item-model #{}", marketSign.isAdminSign() ? "Server" : marketSign.getOwner().getName(), marketSign.getItemInfo().getKey());
                    }
                    marketSign.getItemInfo().updateSignTexts(); // update all signs that use the same itemInfo
                    return;
                }
            }
            // no sync -> new ItemModel
        }
        this.saveOrUpdate(marketSign);
        marketSign.getItemInfo().updateSignTexts(); // update all signs that use the same itemInfo
    }

    private void saveOrUpdate(MarketSign marketSign)
    {
        if (marketSign.isValidSign(null))
        {
            if (marketSign.getItemInfo().getKey().longValue() == 0) // itemInfo not saved in database
            {
                this.signMarketItemManager.store(marketSign.getItemInfo());
                this.module.getLog().debug("{} stored item-model #{}", marketSign.isAdminSign() ? "Server" : marketSign.getOwner().getName(), marketSign.getItemInfo().getKey());
                // set freshly assigned itemData reference in BlockInfo
                marketSign.getBlockInfo().setItemkey(marketSign.getItemInfo().getKey());
            }
            else // update
            {
                this.signMarketItemManager.update(marketSign.getItemInfo());
            }
            if (marketSign.getBlockInfo().getKey().longValue() == 0) // blockInfo not saved in database
            {
                this.signMarketBlockManager.store(marketSign.getBlockInfo());
                this.module.getLog().debug("{} stored block-model #{}", marketSign.isAdminSign() ? "Server" : marketSign.getOwner().getName(), marketSign.getBlockInfo().getKey());
            }
            else // update
            {
                this.signMarketBlockManager.update(marketSign.getBlockInfo());
            }
        }
    }
}
