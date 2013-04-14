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
package de.cubeisland.cubeengine.powersigns;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.powersigns.signtype.SignType;
import de.cubeisland.cubeengine.powersigns.signtype.SignTypeInfo;

public class PowerSign<T extends SignType, I extends SignTypeInfo>
{
    private T signType;
    private I signInfo;

    public PowerSign(T signType, Location location, User user, String[] lines)
    {
        this.signType = signType;
        this.signInfo = (I)signType.createInfo(user,location,lines[0],lines[1],lines[2],lines[3]);
        //this.signInfo.saveData();
    }

    public PowerSign(T signType, I signInfo)
    {
        this.signType = signType;
        this.signInfo = signInfo;
    }

    public T getSignType()
    {
        return signType;
    }

    public I getSignTypeInfo()
    {
        return signInfo;
    }

    public Location getLocation()
    {
        return this.signInfo.getLocation();
    }

    public void updateSignText()
    {
        this.signInfo.updateSignText();
    }
}
