
package de.cubeisland.cubeengine.signmarket.storage;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.util.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@SingleKeyEntity(autoIncrement = true, primaryKey = "key", tableName = "signmarketinfo",
        indices = {
                @Index(value = Index.IndexType.FOREIGN_KEY, fields = "key", f_field = "key", f_table = "signmarketblocks", onDelete = "CASCADE"),
        })
public class SignMarketInfoModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long key;
    @Attribute(type = AttrType.BOOLEAN)
    public Boolean isBuySign; //else isSellSign / NULL -> Edit illegal value for database!


    @Attribute(type = AttrType.SMALLINT, unsigned = true)
    public int amount;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long price;
    @Attribute(type = AttrType.MEDIUMINT, unsigned = true, notnull = false)
    public Integer stock;
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    public Long owner; //TODO foreign key to user

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

    private ItemStack itemStack = null;



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

    public void setItem(ItemStack item)
    {
        this.item = item.getType().name();
        this.damageValue = (int)item.getDurability();
        this.enchantments = this.getEnchantmentsAsString(item);
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName())
        {
            this.customName = meta.getDisplayName() ;
        }
        if (meta.hasLore())
        {
            this.lore = StringUtils.implode("\n",meta.getLore());
        }
    }

    private String getEnchantmentsAsString(ItemStack item)
    {
        Map<Enchantment,Integer> enchs = item.getEnchantments ();
        if (!enchs.isEmpty())
        {
            List<String> enchStrings = new ArrayList<String>();
            for (Enchantment ench : enchs.keySet())
            {
                enchStrings.add(ench.getId() + ":" + enchs.get(ench));
            }
            return StringUtils.implode(",",enchStrings);
        }
        return null;
    }

    public SignMarketInfoModel() {
    }

    public boolean isItem(ItemStack itemInHand)
    {
        return this.getItem().isSimilar(itemInHand);
    }

    public ItemStack getItem()
    {
        if (this.itemStack == null)
        {
            this.itemStack = new ItemStack(Material.valueOf(this.item),0, this.damageValue.shortValue());
            ItemMeta meta = this.itemStack.getItemMeta();
            if (this.customName != null)
            {
                meta.setDisplayName(this.customName);
            }
            if (this.lore != null)
            {
                meta.setLore(Arrays.asList(StringUtils.explode("\n",this.lore)));
            }
            if (this.enchantments != null)
            {
                String[] enchStrings = StringUtils.explode(",",this.enchantments);
                for (String enchString: enchStrings)
                {
                    String[] split = StringUtils.explode(":",enchString);
                    Enchantment ench = Enchantment.getById(Integer.parseInt(split[0]));
                    int level = Integer.parseInt(split[1]);
                    this.itemStack.addUnsafeEnchantment(ench,level);
                }
            }
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public boolean isAdminSign() {
        return this.owner == null;
    }
}