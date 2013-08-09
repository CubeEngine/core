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
package de.cubeisland.engine.powersigns.signtype;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import de.cubeisland.engine.powersigns.Powersigns;
import de.cubeisland.engine.powersigns.SignManager;
import de.cubeisland.engine.powersigns.storage.PowerSignModel;

import static de.cubeisland.engine.powersigns.storage.TablePowerSign.TABLE_POWER_SIGN;

public abstract class SignTypeInfo<T extends SignType>
{
    protected final T signType;
    private final Location location;
    protected Long creator;
    protected Powersigns module;
    protected SignManager manager;
    private PowerSignModel model = null;

    protected SignTypeInfo(Powersigns module, Location location, T signType, long creator)
    {
        this.module = module;
        this.manager = module.getManager();
        this.location = location;
        this.creator = creator;
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
            this.model = this.manager.dsl.newRecord(TABLE_POWER_SIGN).newPSign(this);
            this.model.insert();
        }
        else
        {
            model.update();
        }
    }

    public abstract String serializeData();
}
