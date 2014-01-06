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
package de.cubeisland.engine.basics;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.UserAttachment;

public class BasicsAttachment extends UserAttachment
{
    private long lastAction = 0;
    private BasicsUser basicUser = null;
    private boolean afk;
    private Location lastLocation = null;
    private Integer tpRequestCancelTask;
    private String pendingTpToRequest;
    private String pendingTpFromRequest;
    private ItemStack[] stashedArmor;
    private ItemStack[] stashedInventory;
    private String lastWhisper;
    private Location deathLocation;

    public long getLastAction()
    {
        return this.lastAction;
    }

    public long updateLastAction()
    {
        return this.lastAction = System.currentTimeMillis();
    }

    public void setAfk(boolean afk)
    {
        this.afk = afk;
    }

    public boolean isAfk()
    {
        return afk;
    }

    private boolean unlimitedItems = false;

    public boolean hasUnlimitedItems() {
        return unlimitedItems;
    }

    public void setUnlimitedItems(boolean b)
    {
        this.unlimitedItems = b;
    }

    public BasicsUser getBasicsUser() {
        if (basicUser == null)
        {
            this.basicUser = new BasicsUser(this.getModule().getCore().getDB(), this.getHolder());
        }
        return basicUser;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public void setTpRequestCancelTask(Integer tpRequestCancelTask) {
        this.tpRequestCancelTask = tpRequestCancelTask;
    }

    public Integer getTpRequestCancelTask() {
        return tpRequestCancelTask;
    }

    public void removeTpRequestCancelTask() {
        this.tpRequestCancelTask = null;
    }

    public void setPendingTpToRequest(String pendingTpToRequest) {
        this.pendingTpToRequest = pendingTpToRequest;
    }

    public String getPendingTpToRequest() {
        return pendingTpToRequest;
    }

    public void removePendingTpToRequest() {
        pendingTpToRequest = null;
    }

    public void setPendingTpFromRequest(String pendingTpFromRequest) {
        this.pendingTpFromRequest = pendingTpFromRequest;
    }

    public String getPendingTpFromRequest() {
        return pendingTpFromRequest;
    }

    public void removePendingTpFromRequest() {
        pendingTpFromRequest = null;
    }

    public void setStashedArmor(ItemStack[] stashedArmor) {
        this.stashedArmor = stashedArmor;
    }

    public ItemStack[] getStashedArmor() {
        return stashedArmor;
    }

    public void setStashedInventory(ItemStack[] stashedInventory) {
        this.stashedInventory = stashedInventory;
    }

    public ItemStack[] getStashedInventory() {
        return stashedInventory;
    }

    public void setLastWhisper(String lastWhisper) {
        this.lastWhisper = lastWhisper;
    }

    public String getLastWhisper() {
        return lastWhisper;
    }

    public void resetLastAction() {
        this.lastAction = 0;
    }

    public void setDeathLocation(Location deathLocation)
    {
        this.deathLocation = deathLocation;
    }

    /**
     * Also nulls the location
     *
     * @return the location
     */
    public Location getDeathLocation()
    {
        Location loc = deathLocation;
        deathLocation = null;
        return loc;
    }
}
