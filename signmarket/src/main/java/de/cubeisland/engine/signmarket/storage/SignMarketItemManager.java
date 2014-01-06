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
package de.cubeisland.engine.signmarket.storage;

import de.cubeisland.engine.signmarket.Signmarket;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.jooq.DSLContext;

import static de.cubeisland.engine.signmarket.storage.TableSignItem.TABLE_SIGN_ITEM;

public class SignMarketItemManager
{
    private TLongObjectHashMap<SignMarketItemModel> itemInfoModels;

    private Signmarket module;

    private DSLContext dsl;

    public SignMarketItemManager(Signmarket module)
    {
        this.dsl = module.getCore().getDB().getDSL();
        this.module = module;
    }

    public void load()
    {
        this.itemInfoModels = new TLongObjectHashMap<>();
        for (SignMarketItemModel model : this.dsl.selectFrom(TABLE_SIGN_ITEM).fetch())
        {
            this.itemInfoModels.put(model.getKey().longValue(), model);
        }
        this.module.getLog().debug("{} item-models loaded", this.itemInfoModels.size());
    }

    public SignMarketItemModel getInfoModel(long key)
    {
        return this.itemInfoModels.get(key);
    }

    public void store(SignMarketItemModel itemInfo)
    {
        itemInfo.insert();
        this.itemInfoModels.put(itemInfo.getKey().longValue(), itemInfo);
    }

    public void deleteUnusedModels(TLongHashSet usedKeys)
    {
        for (long key : this.itemInfoModels.keys())
        {
            if (!usedKeys.contains(key))
            {
                this.itemInfoModels.remove(key).delete();
                this.module.getLog().debug("deleted unused item-model #{}", key);
            }
        }
    }

    public void delete(SignMarketItemModel itemInfo)
    {
        if (itemInfo.getKey() == null || itemInfo.getKey().longValue() == 0) return; // unsaved model
        this.itemInfoModels.remove(itemInfo.getKey().longValue()).delete();
    }

    public void update(SignMarketItemModel itemInfo)
    {
        itemInfo.update();
    }
}
