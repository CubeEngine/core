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
package de.cubeisland.cubeengine.signmarket.storage;

import java.util.Collection;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.signmarket.Signmarket;

import gnu.trove.map.hash.THashMap;

public class SignMarketBlockManager extends SingleKeyStorage<Long, SignMarketBlockModel>
{
    private static final int REVISION = 1;

    private THashMap<Location,SignMarketBlockModel> blockModels;

    private Signmarket module;

    public SignMarketBlockManager(Signmarket module)
    {
        super(module.getCore().getDB(), SignMarketBlockModel.class, REVISION);
        this.module = module;
        this.initialize();
    }

    public void load()
    {
        this.blockModels = new THashMap<Location, SignMarketBlockModel>();
        for (SignMarketBlockModel model : this.getAll())
        {
            this.blockModels.put(model.getLocation(),model);
        }
        this.module.getLog().debug("{} block-models loaded", this.blockModels.size());
    }

    public Collection<SignMarketBlockModel> getLoadedModels()
    {
        return this.blockModels.values();
    }

    @Override
    public void delete(SignMarketBlockModel model)
    {
        this.blockModels.remove(model.getLocation());
        super.delete(model);
        this.module.getLog().debug("deleted block-model #{}", model.key);
    }

    @Override
    public void store(SignMarketBlockModel blockModel)
    {
        this.blockModels.put(blockModel.getLocation(),blockModel);
        super.store(blockModel);
        this.module.getLog().debug("stored block-model #{}", blockModel.key);
    }
}
