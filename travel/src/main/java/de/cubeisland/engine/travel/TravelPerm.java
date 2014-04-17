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
import de.cubeisland.engine.travel.warp.WarpCommand;

import static de.cubeisland.engine.core.permission.PermDefault.TRUE;

public class TravelPerm extends PermissionContainer<Travel>
{
    public TravelPerm(Travel module, HomeCommand homeCmd, WarpCommand warpCmd)
    {
        super(module);
        HOME_TP_OTHER = homeCmd.getChild("tp").getPermission().child("other");
        HOME_SET_MORE = homeCmd.getChild("set").getPermission().child("more");
        HOME_MOVE_OTHER = homeCmd.getChild("move").getPermission().child("other");
        HOME_REMOVE_OTHER = homeCmd.getChild("remove").getPermission().child("other");
        HOME_RENAME_OTHER = homeCmd.getChild("rename").getPermission().child("other");
        HOME_LIST_OTHER = homeCmd.getChild("list").getPermission().child("other");
        HOME_PRIVATE_OTHER = homeCmd.getChild("private").getPermission().child("other");
        HOME_PUBLIC_OTHER = homeCmd.getChild("public").getPermission().child("other");

        WARP_TP_OTHER = warpCmd.getChild("tp").getPermission().child("other");
        WARP_MOVE_OTHER = warpCmd.getChild("move").getPermission().child("other");
        WARP_REMOVE_OTHER = warpCmd.getChild("remove").getPermission().child("other");
        WARP_RENAME_OTHER = warpCmd.getChild("rename").getPermission().child("other");
        WARP_LIST_OTHER = warpCmd.getChild("list").getPermission().child("other");
        WARP_PRIVATE_OTHER = warpCmd.getChild("private").getPermission().child("other");
        WARP_PUBLIC_OTHER = warpCmd.getChild("public").getPermission().child("other");

        HOME_USER.attach(homeCmd.getChild("tp").getPermission(),
                         homeCmd.getChild("set").getPermission(),
                         homeCmd.getChild("move").getPermission(),
                         homeCmd.getChild("remove").getPermission(),
                         homeCmd.getChild("rename").getPermission(),
                         homeCmd.getChild("list").getPermission(),
                         homeCmd.getChild("private").getPermission(),
                         homeCmd.getChild("greeting").getPermission(),
                         homeCmd.getChild("ilist").getPermission(),
                         homeCmd.getChild("invite").getPermission(),
                         homeCmd.getChild("uninvite").getPermission());

        this.registerAllPermissions();
    }

    public final Permission HOME_USER = getBasePerm().child("home-user", TRUE);

    public final Permission HOME_TP_OTHER;
    public final Permission HOME_SET_MORE;
    public final Permission HOME_MOVE_OTHER;
    public final Permission HOME_REMOVE_OTHER;
    public final Permission HOME_RENAME_OTHER;
    public final Permission HOME_LIST_OTHER;
    public final Permission HOME_PRIVATE_OTHER;
    public final Permission HOME_PUBLIC_OTHER;

    public final Permission WARP_TP_OTHER;
    public final Permission WARP_MOVE_OTHER;
    public final Permission WARP_REMOVE_OTHER;
    public final Permission WARP_RENAME_OTHER;
    public final Permission WARP_LIST_OTHER;
    public final Permission WARP_PRIVATE_OTHER;
    public final Permission WARP_PUBLIC_OTHER;
}
