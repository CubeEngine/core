package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.basics.storage.BasicUser;
import de.cubeisland.cubeengine.core.user.UserAttachment;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BasicsAttachment extends UserAttachment
{
    private long lastAction = 0;
    private BasicUser basicUser = null;
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

    private Map<String,Long> kitUsages = new HashMap<String, Long>();

    public void setKitUsage(String name)
    {
        this.kitUsages.put(name,System.currentTimeMillis());
    }

    public Long getKitUsage(String name) {
        return this.kitUsages.get(name);
    }

    private boolean unlimitedItems = false;

    public boolean hasUnlimitedItems() {
        return unlimitedItems;
    }

    public void setUnlimitedItems(boolean b)
    {
        this.unlimitedItems = b;
    }

    public BasicUser getBasicUser() {
        return basicUser;
    }

    public void setBasicUser(BasicUser basicUser) {
        this.basicUser = basicUser;
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
     * @return
     */
    public Location getDeathLocation()
    {
        Location loc = deathLocation;
        deathLocation = null;
        return deathLocation;
    }
}
