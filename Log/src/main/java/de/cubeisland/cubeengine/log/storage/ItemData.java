package de.cubeisland.cubeengine.log.storage;

import org.bukkit.inventory.ItemStack;

public class ItemData
{
    public int mat;
    public short data;
    public String name = "";

    public ItemData(ItemStack item)
    {
        this.mat = item.getTypeId();
        this.data = item.getDurability();
        if (item.getItemMeta() != null)
        {
            this.name = item.getItemMeta().getDisplayName();
        }
        if (this.name == null)
        {
            this.name = "";
        }
    }

    public ItemData(int mat, Short data)
    {
        this.mat = mat;
        this.data = data;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ItemData)
        {
            ItemData o = ((ItemData)obj);
            if (this.mat == o.mat && this.data == o.data && this.name.equals(o.name))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 97 * hash + this.mat;
        hash = 97 * hash + this.data;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
