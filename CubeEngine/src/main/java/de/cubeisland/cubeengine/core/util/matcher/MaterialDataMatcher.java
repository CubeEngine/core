package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.util.StringUtils;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class MaterialDataMatcher {

    private TIntObjectHashMap<THashMap<String, Short>> datavalues;

    MaterialDataMatcher() {
        this.readDataValues();
    }

    /**
     * Loads in the file with the saved item-datavalues
     */
    private void readDataValues()
    {
        this.datavalues = new TIntObjectHashMap<THashMap<String, Short>>();
        try
        {
            File file = new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.DATAVALUES.getTarget());
            List<String> input = FileUtil.readStringList(file);
            THashMap<String, Short> data = new THashMap<String, Short>();
            for (String line : input)
            {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                {
                    continue;
                }
                if (line.endsWith(":"))
                {
                    for (String key : StringUtils.explode(",", line.substring(0, line.length() - 1)))
                    {
                        data = new THashMap<String, Short>();
                        this.datavalues.put(Integer.parseInt(key), data);
                    }
                }
                else
                {
                    if (line.contains(":"))
                    {
                        for (String key : StringUtils.explode(",", line.substring(0, line.indexOf(":"))))
                        {
                            data.put(key, Short.parseShort(line.substring(line.indexOf(":") + 1).trim()));
                        }
                    }
                }
            }
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalStateException("datavalues.txt is corrupted!", ex);
        }
    }


    /**
     * Matches a DyeColor
     *
     * @param data the data
     * @return the dye color
     */
    public DyeColor colorData(String data)
    {
        Short dataVal = this.datavalues.get(35).get(Match.string().matchString(data, this.datavalues.get(35).keySet()));
        if (dataVal == null)
        {
            return null;
        }
        return DyeColor.getByData(dataVal.byteValue());
    }

    /**
     * Sets the data for an ItemStack
     *
     * @param item the item
     * @param rawData the data
     * @return the modified clone of the item
     */
    public ItemStack setData(ItemStack item, String rawData)
    {
        String data = rawData.toLowerCase(Locale.ENGLISH);
        if (item == null)
        {
            return null;
        }
        item = item.clone();
        try
        { // try dataValue as Number
            item.setDurability(Short.parseShort(data));
            return item;
        }
        catch (NumberFormatException e)
        { // check for special cases
            switch (item.getType())
            {
                case WOOD:
                case LOG:
                case LEAVES:
                case WOOD_STEP:
                case WOOD_DOUBLE_STEP:
                case WOOL:
                case INK_SACK:
                case DOUBLE_STEP:
                case STEP:
                case SANDSTONE:
                case BRICK:
                case HUGE_MUSHROOM_1:
                case HUGE_MUSHROOM_2:
                case POTION:
                    String foundData = Match.string().matchString(data, this.datavalues.get(item.getTypeId()).keySet());
                    if (foundData != null)
                    {
                        item.setDurability(this.datavalues.get(item.getType().getId()).get(foundData));
                    }
                    return item;
                case MONSTER_EGG:
                    EntityType foundEggData = Match.entity().spawnEggMob(data);
                    if (foundEggData != null)
                    {
                        item.setDurability(foundEggData.getBukkitType().getTypeId());
                    }
                    return item;
                case SKULL_ITEM:
                    item.setDurability((short)3);
                    SkullMeta meta = ((SkullMeta)item.getItemMeta());
                    meta.setOwner(rawData);
                    item.setItemMeta(meta);
                    return item;
                default:
                    return null;
            }
        }
    }
}
