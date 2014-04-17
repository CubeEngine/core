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
package de.cubeisland.engine.travel;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;
import de.cubeisland.engine.travel.home.HomeCommand;

public class TravelPerm extends PermissionContainer<Travel>
{
    public TravelPerm(Travel module, HomeCommand cmd)
    {
        super(module);
        HOME_TP_OTHER = cmd.getChild("tp").getPermission().child("other");
        HOME_SET_MORE = cmd.getChild("set").getPermission().child("more");
        HOME_MOVE_OTHER = cmd.getChild("move").getPermission().child("other");
        HOME_REMOVE_OTHER = cmd.getChild("remove").getPermission().child("other");
        HOME_LIST_OTHER = cmd.getChild("list").getPermission().child("other");
        this.registerAllPermissions();
    }

    public final Permission HOME_TP_OTHER;
    public final Permission HOME_SET_MORE;
    public final Permission HOME_MOVE_OTHER;
    public final Permission HOME_REMOVE_OTHER;
    public final Permission HOME_LIST_OTHER;
    public final Permission HOME_CHANGE_OTHER = getBasePerm().child("change-other");
}
