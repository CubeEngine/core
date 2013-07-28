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

import de.cubeisland.engine.core.storage.SingleKeyStorage;
import de.cubeisland.engine.signmarket.Signmarket;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

public class SignMarketItemManager extends SingleKeyStorage<Long, SignMarketItemModel>
{
    private static final int REVISION = 1;

    private TLongObjectHashMap<SignMarketItemModel> itemInfoModels;

    private Signmarket module;

    public SignMarketItemManager(Signmarket module)
    {
        super(module.getCore().getDB(), SignMarketItemModel.class, REVISION);
        this.module = module;
        this.initialize();
    }

    public void load()
    {
        this.itemInfoModels = new TLongObjectHashMap<>();
        for (SignMarketItemModel model : this.getAll())
        {
            this.itemInfoModels.put(model.key, model);
        }
        this.module.getLog().debug("{} item-models loaded", this.itemInfoModels.size());
    }

    public SignMarketItemModel getInfoModel(long key)
    {
        return this.itemInfoModels.get(key);
    }

    @Override
    public void store(SignMarketItemModel itemInfo)
    {
        super.store(itemInfo);
        this.itemInfoModels.put(itemInfo.key, itemInfo);
        this.module.getLog().debug("stored item-model #{}", itemInfo.key);
    }

    public void deleteUnusedModels(TLongHashSet usedKeys)
    {
        for (long key : this.itemInfoModels.keys())
        {
            if (!usedKeys.contains(key))
            {
                this.deleteByKey(key);
                this.itemInfoModels.remove(key);
                this.module.getLog().debug("deleted unused item-model #{}", key);
            }
        }
    }

    @Override
    public void delete(SignMarketItemModel itemInfo)
    {
        this.itemInfoModels.remove(itemInfo.key);
        super.delete(itemInfo);
        this.module.getLog().debug("deleted item-model #{}", itemInfo.key);
    }
}
