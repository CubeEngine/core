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
import javax.persistence.Transient;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.signmarket.MarketSign;
import gnu.trove.set.hash.THashSet;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static de.cubeisland.engine.signmarket.storage.TableSignItem.TABLE_SIGN_ITEM;

public class SignMarketItemModel extends UpdatableRecordImpl<SignMarketItemModel>
    implements Record8<UInteger, UInteger, String, UShort, String, String, String, Byte> ,InventoryHolder,Cloneable
{
    @Transient
    private ItemStack itemStack;

    public void setItemStack(ItemStack item)
    {
        this.setItem(item.getType().name());
        this.setDamagevalue(UShort.valueOf(item.getDurability()));
        this.setEnchantments(this.getEnchantmentsAsString(item));
        this.setCustomname(null);
        this.setLore(null);
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName())
        {
            this.setCustomname(meta.getDisplayName());
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
        Map<Enchantment, Integer> enchs;
        if (item.getItemMeta() instanceof EnchantmentStorageMeta)
        {
            EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta)item.getItemMeta();
            enchs = itemMeta.getStoredEnchants();
        }
        else
        {
            enchs = item.getEnchantments();
        }
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
        ItemStack itemInSign = this.getItemStack();
        if (itemInSign.hasItemMeta() && itemInHand.hasItemMeta())
        {
            if (itemInSign.getItemMeta() instanceof Repairable && itemInHand.getItemMeta() instanceof Repairable)
            {
                ItemMeta itemMeta = itemInSign.getItemMeta();
                ((Repairable)itemMeta).setRepairCost(((Repairable)itemInHand.getItemMeta()).getRepairCost());
                itemInSign.setItemMeta(itemMeta); // repairCost is not saved
            }
        }
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
            if (this.getItem() == null)
                return null;
            this.itemStack = new ItemStack(Material.valueOf(this.getItem()), 0, this.getDamagevalue().shortValue());
            ItemMeta meta = this.itemStack.getItemMeta();
            if (this.getCustomname() != null)
            {
                meta.setDisplayName(this.getCustomname());
            }
            if (this.getLore() != null)
            {
                meta.setLore(Arrays.asList(StringUtils.explode("\n", this.getLore())));
            }
            if (this.getEnchantments() != null)
            {
                String[] enchStrings = StringUtils.explode(",", this.getEnchantments());
                for (String enchString : enchStrings)
                {
                    String[] split = StringUtils.explode(":", enchString);
                    Enchantment ench = Enchantment.getById(Integer.parseInt(split[0]));
                    int level = Integer.parseInt(split[1]);
                    if (meta instanceof EnchantmentStorageMeta)
                    {
                        ((EnchantmentStorageMeta)meta).addStoredEnchant(ench, level, true);
                    }
                    else
                    {
                        meta.addEnchant(ench, level, true);
                    }
                }
            }
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    @Transient
    private final THashSet<MarketSign> sharedStockSigns = new THashSet<>();

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
        SignMarketItemModel itemInfo = CubeEngine.getCore().getDB().getDSL().newRecord(TABLE_SIGN_ITEM);
        itemInfo.copyValuesFrom(this);
        return itemInfo;
    }

    public void copyValuesFrom(SignMarketItemModel itemInfo)
    {
        this.setStock(itemInfo.getStock());
        this.setItem(itemInfo.getItem());
        this.setDamagevalue(itemInfo.getDamagevalue());
        this.setCustomname(itemInfo.getCustomname());
        this.setLore(itemInfo.getLore());
        this.setEnchantments(itemInfo.getEnchantments());
        this.setSize(itemInfo.getSize());
        // Transient field:
        this.inventory = null;
        this.itemStack = null;
    }

    public SignMarketItemModel()
    {
        super(TABLE_SIGN_ITEM);
        this.setKey(UInteger.valueOf(0));
    }

    public void setKey(UInteger value) {
        setValue(0, value);
    }

    public UInteger getKey() {
        return (UInteger) getValue(0);
    }

    public void setStock(UInteger value) {
        setValue(1, value);
    }

    public UInteger getStock() {
        return (UInteger) getValue(1);
    }

    public void setItem(String value) {
        setValue(2, value);
    }

    public String getItem() {
        return (String) getValue(2);
    }

    public void setDamagevalue(UShort value) {
        setValue(3, value);
    }

    public UShort getDamagevalue() {
        return (UShort) getValue(3);
    }

    public void setCustomname(String value) {
        setValue(4, value);
    }

    public String getCustomname() {
        return (String) getValue(4);
    }

    public void setLore(String value) {
        setValue(5, value);
    }

    public String getLore() {
        return (String) getValue(5);
    }

    public void setEnchantments(String value) {
        setValue(6, value);
    }

    public String getEnchantments() {
        return (String) getValue(6);
    }

    public void setSize(Byte value) {
        setValue(7, value);
    }

    public Byte getSize() {
        return (Byte) getValue(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row8<UInteger, UInteger, String, UShort, String, String, String, Byte> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    @Override
    public Row8<UInteger, UInteger, String, UShort, String, String, String, Byte> valuesRow() {
        return (Row8) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_SIGN_ITEM.KEY;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_SIGN_ITEM.STOCK;
    }

    @Override
    public Field<String> field3() {
        return TABLE_SIGN_ITEM.ITEM;
    }

    @Override
    public Field<UShort> field4() {
        return TABLE_SIGN_ITEM.DAMAGEVALUE;
    }

    @Override
    public Field<String> field5() {
        return TABLE_SIGN_ITEM.CUSTOMNAME;
    }

    @Override
    public Field<String> field6() {
        return TABLE_SIGN_ITEM.LORE;
    }

    @Override
    public Field<String> field7() {
        return TABLE_SIGN_ITEM.ENCHANTMENTS;
    }

    @Override
    public org.jooq.Field<Byte> field8() {
        return TABLE_SIGN_ITEM.SIZE;
    }

    @Override
    public UInteger value1() {
        return getKey();
    }

    @Override
    public UInteger value2() {
        return getStock();
    }

    @Override
    public String value3() {
        return getItem();
    }

    @Override
    public UShort value4() {
        return getDamagevalue();
    }

    @Override
    public String value5() {
        return getCustomname();
    }

    @Override
    public String value6() {
        return getLore();
    }

    @Override
    public String value7() {
        return getEnchantments();
    }

    @Override
    public Byte value8() {
        return getSize();
    }
}
