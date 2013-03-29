package de.cubeisland.cubeengine.log.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonArray;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonElement;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParseException;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonPrimitive;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.cubeengine.core.util.StringUtils;

public class ItemData
{
    public Material material;
    public short dura;
    public int amount;
    public String displayName;
    public List<String> lore;
    public Map<Enchantment,Integer> enchantments;

    public ItemData(Material material, short dura, int amount, String displayName, List<String> lore, Map<Enchantment, Integer> enchantments)
    {
        this.material = material;
        this.dura = dura;
        this.amount = amount;
        this.displayName = displayName;
        this.lore = lore;
        this.enchantments = enchantments;
    }

    public ItemData(ItemStack itemStack)
    {
        this.material = itemStack.getType();
        this.dura = itemStack.getDurability();
        this.amount = itemStack.getAmount();
        if (itemStack.hasItemMeta())
        {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasDisplayName())
            {
                displayName = meta.getDisplayName();
            }
            if (meta.hasLore())
            {
                lore = meta.getLore();
            }
            if (meta.hasEnchants())
            {
                enchantments = meta.getEnchants();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemData itemData = (ItemData) o;
        if (dura != itemData.dura) return false;
        if (displayName != null ? !displayName.equals(itemData.displayName) : itemData.displayName != null)
            return false;
        if (enchantments != null ? !enchantments.equals(itemData.enchantments) : itemData.enchantments != null)
            return false;
        if (lore != null ? !lore.equals(itemData.lore) : itemData.lore != null) return false;
        if (material != itemData.material) return false;
        // ignore amount
        return true;
    }

    @Override
    public int hashCode() {
        int result = material != null ? material.hashCode() : 0;
        result = 31 * result + (int) dura;
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (lore != null ? lore.hashCode() : 0);
        result = 31 * result + (enchantments != null ? enchantments.hashCode() : 0);
        return result;
    }

    public JsonElement serialize() {
        JsonObject result = new JsonObject();
        result.add("mats", new JsonPrimitive(this.material.name()));
        result.add("dura", new JsonPrimitive(this.dura));
        result.add("amount", new JsonPrimitive(this.amount));
        if (this.displayName != null)
        {
            result.add("name", new JsonPrimitive(this.displayName));
        }
        if (this.lore != null)
        {
            JsonArray lore = new JsonArray();
            for (String loreLine : this.lore)
            {
                lore.add(new JsonPrimitive(loreLine));
            }
            result.add("lore",lore);
        }
        if (this.enchantments != null)
        {
            JsonArray enchs = new JsonArray();
            for (Entry<Enchantment,Integer> ench : this.enchantments.entrySet())
            {
                enchs.add(new JsonPrimitive(ench.getKey().getId()+":"+ench.getValue()));
            }
            result.add("enchs",enchs);
        }
        return result;
    }

    public static ItemData deserialize(JsonElement jsonElement) throws JsonParseException
    {
        JsonObject obj = jsonElement.getAsJsonObject();
        Material mat = Material.getMaterial(obj.get("mats").getAsString());
        Short dura = obj.get("data").getAsShort();
        int amount = obj.get("amount").getAsInt();
        String name = null;
        List<String> lore = null;
        Map<Enchantment,Integer> enchantments = null;
        if (obj.get("name") != null)
        {
            name = obj.get("name").getAsString();
        }
        if (obj.get("lore") != null)
        {
            lore = new ArrayList<String>();
            JsonArray jsonLore = obj.get("lore").getAsJsonArray();
            for (JsonElement elem : jsonLore)
            {
                lore.add(elem.getAsString());
            }
        }
        if (obj.get("enchs") != null)
        {
            enchantments = new HashMap<Enchantment, Integer>();
            JsonArray jsonLore = obj.get("enchs").getAsJsonArray();
            for (JsonElement elem : jsonLore)
            {
                String[] ench = StringUtils.explode(":",elem.getAsString());
                enchantments.put(Enchantment.getByName(ench[0]),Integer.parseInt(ench[1]));
            }
        }
        return new ItemData(mat,dura,amount,name,lore,enchantments);
    }

    public ItemStack toItemStack()
    {
        ItemStack itemStack = new ItemStack(material,amount,dura);
        ItemMeta meta = itemStack.getItemMeta();
        if (displayName != null)
        {
            meta.setDisplayName(displayName);
        }
        if (lore != null)
        {
            meta.setLore(lore);
        }
        if (enchantments != null)
        {
            meta.getEnchants().clear();
            meta.getEnchants().putAll(enchantments);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}