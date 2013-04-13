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
package de.cubeisland.cubeengine.powersigns.signtype;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.powersigns.Powersigns;
import de.cubeisland.cubeengine.powersigns.SignManager;
import de.cubeisland.cubeengine.powersigns.storage.PowerSignModel;

public abstract class SignTypeInfo<T extends SignType>
{
    protected final T signType;
    private final Location location;
    protected Long creator;
    protected Powersigns module;
    protected SignManager manager;
    private PowerSignModel model = null;

    protected SignTypeInfo(Powersigns module, Location location, T signType, User user)
    {
        this.module = module;
        this.manager = module.getManager();
        this.location = location;
        this.creator = user.key;
        this.signType = signType;
    }

    public Location getLocation()
    {
        return location;
    }

    public abstract void updateSignText();

    public Sign getSign()
    {
        Block block = this.location.getBlock();
        if (block.getState() instanceof Sign)
        {
            return (Sign)block.getState();
        }
        return null;
    }

    public Long getCreator()
    {
        return creator;
    }

    public T getType()
    {
        return this.signType;
    }

    public long getWorldID()
    {
        return this.module.getCore().getWorldManager().getWorldId(location.getWorld());
    }

    public void saveData()
    {
        if (this.model == null)
        {
            this.model = new PowerSignModel(this);
        }
        if (this.model.id == -1)
        {
            this.manager.getStorage().store(this.model);
        }
        else
        {
            this.manager.getStorage().update(this.model);
        }
    }

    public abstract String serializeData();
}
