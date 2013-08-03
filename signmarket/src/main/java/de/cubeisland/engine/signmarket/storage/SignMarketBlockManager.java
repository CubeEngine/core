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

import java.util.Collection;

import org.bukkit.Location;

import com.avaje.ebean.EbeanServer;
import de.cubeisland.engine.signmarket.Signmarket;
import gnu.trove.map.hash.THashMap;

public class SignMarketBlockManager
{
    private static final int REVISION = 1;

    private THashMap<Location,SignMarketBlockModel> blockModels;

    private Signmarket module;

    private EbeanServer ebean;

    public SignMarketBlockManager(Signmarket module)
    {
        this.ebean = module.getCore().getDB().getEbeanServer();
        this.module = module;
    }

    public void load()
    {
        this.blockModels = new THashMap<>();
        for (SignMarketBlockModel model : this.ebean.find(SignMarketBlockModel.class).findList())
        {
            this.blockModels.put(model.getLocation(),model);
        }
        this.module.getLog().debug("{} block-models loaded", this.blockModels.size());
    }

    public Collection<SignMarketBlockModel> getLoadedModels()
    {
        return this.blockModels.values();
    }

    public void delete(SignMarketBlockModel model)
    {
        this.blockModels.remove(model.getLocation());
        if (model.getId() == 0) return; // unsaved model
        this.ebean.delete(model);
        this.module.getLog().debug("deleted block-model #{}", model.getId());
    }

    public void store(SignMarketBlockModel blockModel)
    {
        this.blockModels.put(blockModel.getLocation(),blockModel);
        this.ebean.save(blockModel);
        this.module.getLog().debug("stored block-model #{}", blockModel.getId());
    }

    public void update(SignMarketBlockModel blockItemModel)
    {
        this.ebean.update(blockItemModel);
    }
}
