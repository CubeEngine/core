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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.DBUpdater;
import de.cubeisland.engine.core.storage.database.DatabaseUpdater;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.signmarket.MarketSign;
import de.cubeisland.engine.signmarket.storage.SignMarketItemModel.SignMarketItemUpdater;
import gnu.trove.set.hash.THashSet;

@Entity
@Table(name = "signmarketitem")
@DBUpdater(SignMarketItemUpdater.class)
public class SignMarketItemModel implements InventoryHolder,Cloneable
{
    @javax.persistence.Version
    static final Version version = new Version(2);

    @Id
    @Attribute(type = AttrType.INT, unsigned = true)
    private long id = 0;

    @Column
    @Attribute(type = AttrType.MEDIUMINT, unsigned = true)
    private Integer stock; // can be null if infinite stock

    //ITEM-data:
    @Column(name = "item", length = 32)
    @Attribute(type = AttrType.VARCHAR)
    private String itemString;
    @Column(nullable = false)
    @Attribute(type = AttrType.SMALLINT, unsigned = true)
    private Integer damageValue;
    @Column(length = 100)
    @Attribute(type = AttrType.VARCHAR)
    private String customName;
    @Column(length = 1000)
    @Attribute(type = AttrType.VARCHAR)
    private String lore;
    @Column(length = 225)
    @Attribute(type = AttrType.VARCHAR)
    private String enchantments;
    @Column
    @Attribute(type = AttrType.TINYINT)
    private int size = 6;

    @Transient
    private ItemStack itemStack;

    public void setItemStack(ItemStack item)
    {
        this.setItemString(item.getType().name());
        this.setDamageValue((int)item.getDurability());
        this.setEnchantments(this.getEnchantmentsAsString(item));
        this.setCustomName(null);
        this.setLore(null);
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName())
        {
            this.setCustomName(meta.getDisplayName());
        }
        if (meta.hasLore())
        {
            this.setLore(StringUtils.implode("\n", meta.getLore()));
        }
        // Transient Fields:
        this.itemStack = null;
        this.inventory = null;
    }

    private String getEnchantmentsAsString(ItemStack item)
    {
        Map<Enchantment, Integer> enchs = item.getEnchantments();
        if (!enchs.isEmpty())
        {
            List<String> enchStrings = new ArrayList<String>();
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
        return this.getItemStack().isSimilar(itemInHand);
    }

    /**
     * Returns the ItemStack of the item saved in this sign with amount 0.
     *
     * @return
     */
    public ItemStack getItemStack()
    {
        if (this.itemStack == null)
        {
            if (this.itemString == null)
                return null;
            this.itemStack = new ItemStack(Material.valueOf(this.itemString), 0, this.damageValue.shortValue());
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

    @Transient
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

    @Transient
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
        this.setStock(itemInfo.stock);
        this.setItemString(itemInfo.itemString);
        this.setDamageValue(itemInfo.damageValue);
        this.setCustomName(itemInfo.customName);
        this.setLore(itemInfo.lore);
        this.setEnchantments(itemInfo.enchantments);
        this.setSize(itemInfo.size);
        // Transient field:
        this.inventory = null;
        this.itemStack = null;
    }

    public SignMarketItemModel()
    {}

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Integer getStock()
    {
        return stock;
    }

    public void setStock(Integer stock)
    {
        this.stock = stock;
    }

    public void setItemString(String itemString)
    {
        this.itemString = itemString;
    }

    public String getItemString()
    {
        return itemString;
    }

    public Integer getDamageValue()
    {
        return damageValue;
    }

    public void setDamageValue(Integer damageValue)
    {
        this.damageValue = damageValue;
    }

    public String getCustomName()
    {
        return customName;
    }

    public void setCustomName(String customName)
    {
        this.customName = customName;
    }

    public String getLore()
    {
        return lore;
    }

    public void setLore(String lore)
    {
        this.lore = lore;
    }

    public String getEnchantments()
    {
        return enchantments;
    }

    public void setEnchantments(String enchantments)
    {
        this.enchantments = enchantments;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public static class SignMarketItemUpdater implements DatabaseUpdater
    {
        @Override
        public void update(Connection connection, Class<?> entityClass, Version dbVersion, Version codeVersion) throws SQLException
        {
            if (codeVersion.getMajor() == 2)
            {
                // prepare related table
                connection.prepareStatement("RENAME TABLE cube_signmarketitem TO old_signmarketitem").execute();
                connection.prepareStatement("CREATE TABLE `cube_signmarketitem` (  " +
                                                "`id` int(10) unsigned NOT NULL AUTO_INCREMENT, " +
                                                "`stock` mediumint(8) unsigned DEFAULT NULL,  " +
                                                "`item` varchar(32) NOT NULL,  " +
                                                "`damageValue` smallint(5) unsigned NOT NULL,  " +
                                                "`customName` varchar(100) DEFAULT NULL,  " +
                                                "`lore` varchar(1000) DEFAULT NULL,  " +
                                                "`enchantments` varchar(255) DEFAULT NULL,  " +
                                                "`size` tinyint(4) NOT NULL,  PRIMARY KEY (`id`)) " +
                                                "DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT = '2.0.0'").execute();
                connection.prepareStatement("INSERT INTO cube_signmarketitem (`id`, `stock`, `item`, `damageValue`, `customName`, `lore`, `enchantments`, `size`) " +
                                                "SELECT `key`, `stock`, `item`, `damageValue`, `customName`, `lore`, `enchantments`, `size` FROM old_signmarketitem").execute();

            }
        }
    }
}
