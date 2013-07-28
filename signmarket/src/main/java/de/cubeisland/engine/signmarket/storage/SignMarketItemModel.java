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
package de.cubeisland.engine.signmarket.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.engine.core.storage.Model;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.SingleKeyEntity;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.signmarket.MarketSign;

import gnu.trove.set.hash.THashSet;

@SingleKeyEntity(autoIncrement = true, primaryKey = "key", tableName = "signmarketitem", indices = {

})
public class SignMarketItemModel implements Model<Long>,InventoryHolder,Cloneable
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long key = -1;

    @Attribute(type = AttrType.MEDIUMINT, unsigned = true, notnull = false)
    public Integer stock; // can be null if infinite stock

    //ITEM-data:
    @Attribute(type = AttrType.VARCHAR, length = 32)
    public String item;
    @Attribute(type = AttrType.SMALLINT, unsigned = true)
    public Integer damageValue;
    @Attribute(type = AttrType.VARCHAR, length = 100, notnull = false)
    public String customName;
    @Attribute(type = AttrType.VARCHAR, length = 1000, notnull = false)
    public String lore;
    @Attribute(type = AttrType.VARCHAR, length = 255, notnull = false)
    public String enchantments;
    @Attribute(type = AttrType.TINYINT)
    public int size = 6;

    private ItemStack itemStack;

    public void setItem(ItemStack item)
    {
        this.item = item.getType().name();
        this.damageValue = (int)item.getDurability();
        this.enchantments = this.getEnchantmentsAsString(item);
        this.customName = null;
        this.lore = null;
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName())
        {
            this.customName = meta.getDisplayName();
        }
        if (meta.hasLore())
        {
            this.lore = StringUtils.implode("\n", meta.getLore());
        }
        this.itemStack = null;
        this.inventory = null;
    }

    private String getEnchantmentsAsString(ItemStack item)
    {
        Map<Enchantment, Integer> enchs = item.getEnchantments();
        if (!enchs.isEmpty())
        {
            List<String> enchStrings = new ArrayList<>();
            for (Enchantment ench : enchs.keySet())
            {
                enchStrings.add(ench.getId() + ":" + enchs.get(ench));
            }
            return StringUtils.implode(",", enchStrings);
        }
        return null;
    }

    public boolean matchesItem(ItemStack itemInHand)
    {
        return this.getItem().isSimilar(itemInHand);
    }

    /**
     * Returns the ItemStack of the item saved in this sign with amount 0.
     *
     * @return
     */
    public ItemStack getItem()
    {
        if (this.itemStack == null)
        {
            if (this.item == null)
                return null;
            this.itemStack = new ItemStack(Material.valueOf(this.item), 0, this.damageValue.shortValue());
            ItemMeta meta = this.itemStack.getItemMeta();
            if (this.customName != null)
            {
                meta.setDisplayName(this.customName);
            }
            if (this.lore != null)
            {
                meta.setLore(Arrays.asList(StringUtils.explode("\n", this.lore)));
            }
            itemStack.setItemMeta(meta);
            if (this.enchantments != null)
            {
                String[] enchStrings = StringUtils.explode(",", this.enchantments);
                for (String enchString : enchStrings)
                {
                    String[] split = StringUtils.explode(":", enchString);
                    Enchantment ench = Enchantment.getById(Integer.parseInt(split[0]));
                    int level = Integer.parseInt(split[1]);
                    this.itemStack.addUnsafeEnchantment(ench, level);
                }
            }
        }
        return itemStack;
    }

    private THashSet<MarketSign> sharedStockSigns = new THashSet<>();

    public void removeSign(MarketSign marketSign)
    {
        this.sharedStockSigns.remove(marketSign);
    }

    public void addSign(MarketSign marketSign)
    {
        this.sharedStockSigns.add(marketSign);
    }

    public boolean isNotReferenced()
    {
        return this.sharedStockSigns.isEmpty();
    }

    public boolean sharesStock()
    {
        return this.sharedStockSigns.size() > 1;
    }

    public void updateSignTexts()
    {
        for (MarketSign sign : this.sharedStockSigns)
        {
            sign.updateSignText();
        }
    }

    public Inventory inventory;
    @Override
    public Inventory getInventory()
    {
        return this.inventory;
    }

    public void initInventory(Inventory inventory)
    {
        this.inventory = inventory;
    }

    public THashSet<MarketSign> getReferenced()
    {
        return this.sharedStockSigns;
    }

    public SignMarketItemModel clone()
    {
        SignMarketItemModel itemInfo = new SignMarketItemModel();
        itemInfo.copyValuesFrom(this);
        return itemInfo;
    }

    public void copyValuesFrom(SignMarketItemModel itemInfo)
    {
        this.stock = itemInfo.stock;
        this.item = itemInfo.item;
        this.damageValue = itemInfo.damageValue;
        this.customName = itemInfo.customName;
        this.lore = itemInfo.lore;
        this.enchantments = itemInfo.enchantments;
        this.size = itemInfo.size;
        this.itemStack = null;
    }

    //for database:
    @Override
    public Long getId()
    {
        return this.key;
    }
    @Override
    public void setId(Long id)
    {
        this.key = id;
    }
    public SignMarketItemModel()
    {}
}
