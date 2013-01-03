package de.cubeisland.cubeengine.basics.moderation.kit;

import org.bukkit.Material;

public class KitItem
{
    public Material mat;
    public short dura;
    public int amount;
    public String customName;

    public KitItem(Material mat, short dura, int amount, String customName)
    {
        this.mat = mat;
        this.dura = dura;
        this.amount = amount;
        this.customName = customName;
    }

}
