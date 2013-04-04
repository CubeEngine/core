package de.cubeisland.cubeengine.signmarket.storage;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.signmarket.MarketSign;
import gnu.trove.set.hash.THashSet;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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


    public boolean hasStock()
    {
        return this.stock != null;
    }

    /**
     * Returns true if both item-models share the same item and are not infinite item-sources
     * <p>in addition to this the market-signs have to share their owner too!
     *
     * @param model the model to compare to
     * @return
     */
    public boolean canSync(SignMarketItemModel model)
    {
        return this.hasStock() && model.hasStock() // both not infinite stocks
            && this.getItem().isSimilar(model.getItem()); // same item
    }

    //for database:
    @Override
    public Long getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Long key)
    {
        this.key = key;
    }
    public SignMarketItemModel()
    {}

    private THashSet<MarketSign> sharedStockSigns = new THashSet<MarketSign>();

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

    public int getSize() {
        if (this.size == -1)
        {
            return 54;
        }
        return this.size * 9;
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
        this.itemStack = null;
    }
}
