package de.cubeisland.cubeengine.auctions.Util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class Util
{
    /*
     * public static String convertTime(long time) { if
     * (TimeUnit.MILLISECONDS.toMinutes(time)==0) return t("less_time"); return
     * String.format("%dh %dm", TimeUnit.MILLISECONDS.toHours(time),
     * TimeUnit.MILLISECONDS.toMinutes(time) -
     * TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)) ); }
     */
    /**
     * convert ItemStack to String
     */
    public static String convertItem(ItemStack item)
    {
        String out = item.getTypeId() + ":" + item.getDurability();
        if (!item.getEnchantments().isEmpty())
        {
            for (Enchantment ench : item.getEnchantments().keySet())
            {
                out += " " + ench.getId() + ":" + item.getEnchantmentLevel(ench);
            }
        }
        return out;
    }

    /**
     * convert String to ItemStack and set amount
     */
    public static ItemStack convertItem(String in, int amount)
    {
        ItemStack out = convertItem(in);
        out.setAmount(amount);
        return out;
    }

    /**
     * convert String to ItemStack (amount 1)
     */
    public static ItemStack convertItem(String in)
    {
        //id:data
        String mat = in;
        if (in.indexOf(":") != -1)
        {
            mat = in.substring(0, in.indexOf(":"));
        }
        Material material = Material.matchMaterial(mat);
        if (material == null)
        {
            return null;
        }
        short data;
        if (in.indexOf(" ") == -1)
        {
            data = Short.valueOf(in.substring(in.indexOf(":") + 1));
            in = "";
        }
        else
        {
            data = Short.valueOf(in.substring(in.indexOf(":") + 1, in.indexOf(" ")));
            in.replace(in.substring(0, in.indexOf(" ") + 1), "");
        }


        ItemStack out = new ItemStack(material, 1, data);
        //ench1:val1 ench2:val2 ...
        while (in.length() > 1)
        {
            int enchid = Integer.valueOf(in.substring(0, in.indexOf(":")));
            int enchval;
            if (in.indexOf(" ") == -1)
            {
                enchval = Short.valueOf(in.substring(in.indexOf(":") + 1));
                in = "";
            }
            else
            {
                enchval = Integer.valueOf(in.substring(in.indexOf(":") + 1, in.indexOf(" ")));
                in.replace(in.substring(0, in.indexOf(" ") + 1), "");
            }
            if (Enchantment.getById(enchid) != null)
            {
                out.addEnchantment(Enchantment.getById(enchid), enchval);
            }
        }
        return out;
    }
}
