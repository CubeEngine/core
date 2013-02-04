package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import gnu.trove.map.hash.*;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MaterialDataMatcher {

    private THashMap<Material,TShortObjectHashMap<Set<String>>> reverseItemData;
    private THashMap<Material,TByteObjectHashMap<Set<String>>> reverseBlockData;
    private THashMap<Material,TObjectShortHashMap<String>> itemData;
    private THashMap<Material,TObjectByteHashMap<String>> blockData;

    MaterialDataMatcher() {
        this.readDataValues(false);
        this.readDataValues(true);
    }

    /**
     * Loads in the file with the saved item-datavalues
     */
    private void readDataValues(boolean update)
    {
        if (!update)
        {
            this.reverseItemData = new THashMap<Material, TShortObjectHashMap<Set<String>>>();
            this.reverseBlockData = new THashMap<Material, TByteObjectHashMap<Set<String>>>();
            this.itemData = new THashMap<Material, TObjectShortHashMap<String>>();
            this.blockData = new THashMap<Material, TObjectByteHashMap<String>>();
        }
        boolean updated = false;
        File file = new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.DATAVALUES.getTarget());
        List<String> input;
        if (update)
        {
            input = FileUtil.readStringList(CubeEngine.getFileManager().getSourceOf(file));
        }
        else
        {
            input = FileUtil.readStringList(file);
        }
        TShortObjectHashMap<Set<String>> reverseCurrentItemData = null;
        TByteObjectHashMap<Set<String>> reverseCurrentBlockData = null;
        TObjectShortHashMap<String> currentItemData = null;
        TObjectByteHashMap<String> currentBlockData = null;
        for (String line : input)
        {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            if (line.endsWith(":"))
            {
                // clean up empty maps in last material
                Collection<Material> mats = new HashSet<Material>(this.itemData.keySet());
                for (Material mat : mats)
                {
                    if (itemData.get(mat) != null && itemData.get(mat).isEmpty())
                    {
                        itemData.remove(mat);
                    }
                    if (reverseItemData.get(mat) != null && reverseItemData.get(mat).isEmpty())
                    {
                        reverseItemData.remove(mat);
                    }
                    if (blockData.get(mat) != null && blockData.get(mat).isEmpty())
                    {
                        blockData.remove(mat);
                    }
                    if (reverseBlockData.get(mat) != null && reverseBlockData.get(mat).isEmpty())
                    {
                        reverseBlockData.remove(mat);
                    }
                }

                boolean first = true;
                for (String key : StringUtils.explode(",", line.substring(0, line.length() - 1)))
                {
                    try
                    {
                        Material material = Material.valueOf(key);
                        if (first)
                        {
                            reverseCurrentItemData = reverseItemData.get(material);
                            reverseCurrentBlockData =reverseBlockData.get(material);
                            currentItemData = itemData.get(material);
                            currentBlockData = blockData.get(material);
                            if (reverseCurrentItemData == null)
                            {
                                reverseCurrentItemData = new TShortObjectHashMap<Set<String>>();
                            }
                            if (reverseCurrentBlockData == null)
                            {
                                reverseCurrentBlockData = new TByteObjectHashMap<Set<String>>();
                            }
                            if (currentItemData == null)
                            {
                                currentItemData = new TObjectShortHashMap<String>();
                            }
                            if (currentBlockData == null)
                            {
                                currentBlockData = new TObjectByteHashMap<String>();
                            }
                            first = false;
                        }
                        this.reverseItemData.put(material, reverseCurrentItemData);
                        this.reverseBlockData.put(material, reverseCurrentBlockData);
                        this.itemData.put(material,currentItemData);
                        this.blockData.put(material,currentBlockData);
                    }
                    catch (IllegalArgumentException ex)
                    {
                        CubeEngine.getLogger().warning("Unknown Material for Data: "+key);
                        reverseCurrentItemData = new TShortObjectHashMap<Set<String>>();
                        reverseCurrentBlockData = new TByteObjectHashMap<Set<String>>();
                        currentItemData = new TObjectShortHashMap<String>();
                        currentBlockData = new TObjectByteHashMap<String>();
                        continue;
                    }
                }
            }
            else
            {
                if (line.contains(":"))
                {
                    String value = line.substring(line.indexOf(":") + 1).trim();
                    Set<String> names;
                    Byte blockDataVal = null;
                    Short itemDataVal = null;
                    try
                    {
                        if (value.contains("x"))
                        {
                            blockDataVal = Byte.decode(value);
                            names = reverseCurrentBlockData.get(blockDataVal);
                            if (names == null)
                            {
                                names = new HashSet<String>();
                                reverseCurrentBlockData.put( blockDataVal, names);
                                updated = true;
                            }
                        }
                        else
                        {
                            itemDataVal = Short.parseShort(value);
                            names = reverseCurrentItemData.get(itemDataVal);
                            if (names == null)
                            {
                                names = new HashSet<String>();
                                reverseCurrentItemData.put(itemDataVal,names);
                                updated = true;
                            }
                        }
                    }
                    catch (NumberFormatException ex)
                    {
                        CubeEngine.getLogger().warning("Could not parse data for Material: "+ value);
                        continue;
                    }
                    for (String key : StringUtils.explode(",", line.substring(0, line.indexOf(":"))))
                    {
                        if (names.add(key))
                        {
                            if (itemDataVal == null)
                            {
                                currentBlockData.put(key,blockDataVal);
                            }
                            else
                            {
                                currentItemData.put(key,itemDataVal);
                            }
                        }
                    }
                }
            }
        }
        if (update && updated)
        {
            CubeEngine.getLogger().log(LogLevel.NOTICE, "Updated datavalues.txt");
            StringBuilder sb = new StringBuilder();
            HashMap<TShortObjectHashMap<Set<String>>,String> itemMap = new HashMap<TShortObjectHashMap<Set<String>>, String>();
            for (Material material : this.reverseItemData.keySet()) // make serializable...
            {
                TShortObjectHashMap<Set<String>> map =  this.reverseItemData.get(material);
                if (map.isEmpty())
                    continue;
                String mats = itemMap.get(map);
                if (mats == null)
                {
                    mats = material.name();
                }
                else
                {
                    mats += ","+material.name();
                }
                itemMap.put(map,mats);
            }
            for (Map.Entry<TShortObjectHashMap<Set<String>>,String> entry : itemMap.entrySet()) // serialize...
            {
                if (entry.getKey().isEmpty())
                    continue;
                sb.append(entry.getValue()).append(":\n");
                TShortObjectHashMap<Set<String>> map = entry.getKey();
                for (Short value : map.keys())
                {
                    sb.append("    ").append(StringUtils.implode(",",map.get(value))).append(": ").append(value).append("\n");
                }
            }
            HashMap<TByteObjectHashMap<Set<String>>,String> blockMap = new HashMap<TByteObjectHashMap<Set<String>>, String>();
            for (Material material : this.reverseBlockData.keySet()) // make serializable...
            {
                TByteObjectHashMap<Set<String>> map = this.reverseBlockData.get(material);
                if (map.isEmpty())
                    continue;
                String mats = blockMap.put(map,material.name());
                if (mats != null)
                {
                    mats += ","+material.name();
                    blockMap.put(map,mats);
                }
            }
            for (Map.Entry<TByteObjectHashMap<Set<String>>,String> entry : blockMap.entrySet()) // serialize...
            {
                if (entry.getKey().isEmpty())
                    continue;
                sb.append(entry.getValue()).append(":\n");
                TByteObjectHashMap<Set<String>> map = entry.getKey();
                for (Byte value : map.keys())
                {
                    String val;
                    if (value <0)
                    {
                        val = "-0x"+Integer.toString(-value,16);
                    }
                    else
                    {
                        val = "0x"+Integer.toString(value,16);
                    }
                    sb.append("    ").append(StringUtils.implode(",",map.get(value))).append(": ").append(val).append("\n");
                }
            }
            try
            {
                FileUtil.saveFile(sb.toString(), file);
            } catch (IOException e)
            {
                CubeEngine.getLogger().warning("Could not save changed datavalues.txt");
            }
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
        try
        {
            byte byteData = Byte.parseByte(data);
            DyeColor color = DyeColor.getByWoolData(byteData);
            if (color != null)
            {
                return color;
            }
        }
        catch (NumberFormatException e)
        {}
        TObjectShortHashMap<String> woolData = this.itemData.get(Material.WOOL);
        if (woolData == null)
        {
            CubeEngine.getLogger().warning("No data found for Wool-color");
            return null;
        }
        Short dataVal = woolData.get(Match.string().matchString(data, woolData.keySet()));
        if (dataVal == null)
        {
            return null;
        }
        return DyeColor.getByWoolData(dataVal.byteValue());
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
                case MONSTER_EGG:
                    EntityType foundEggData = Match.entity().spawnEggMob(data);
                    if (foundEggData != null)
                    {
                        item.setDurability(foundEggData.getTypeId());
                    }
                    return item;
                case SKULL_ITEM:
                    item.setDurability((short)3);
                    SkullMeta meta = ((SkullMeta)item.getItemMeta());
                    meta.setOwner(rawData);
                    item.setItemMeta(meta);
                    return item;
                default:
                    TObjectShortHashMap<String> dataMap = this.itemData.get(item.getType());
                    if (dataMap != null)
                    {
                        String foundData = Match.string().matchString(data, dataMap.keySet());
                        if (foundData != null)
                        {
                            item.setDurability(dataMap.get(foundData));
                        }
                    }
                    return item;
            }
        }
    }

    public String getDataNameFor(Material mat, Byte blockData)
    {
        TByteObjectHashMap<Set<String>> map = this.reverseBlockData.get(mat);
        String dataNames = "";
        if (map != null)
        {
            String zeroData = null;
            byte mask = blockData;
            for (Byte flag : map.keys())
            {
                if (flag < 0)
                {
                    if (!((mask & -flag) == flag))
                    {
                        dataNames += ":"+map.get(flag).iterator().next();
                    }
                }
                else if (flag == 0)
                {
                    zeroData = map.get(flag).iterator().next();
                }
                else
                {
                    if ((mask & flag) == flag)
                    {
                        dataNames += ":"+map.get(flag).iterator().next();
                        mask &= ~flag;
                    }
                }
            }
            if (dataNames.isEmpty() && zeroData != null)
            {
                dataNames += ":" + zeroData;
            }
        }
        TShortObjectHashMap<Set<String>> itemMap = this.reverseItemData.get(mat);
        if (itemMap == null)
        {
            if (dataNames.isEmpty())
            {
                CubeEngine.getLogger().warning("Unknown Block-Type: "+ mat);
                return null;
            }
        }
        else
        {
            Set<String> names = itemMap.get(blockData.shortValue());
            if (names == null)
            {
                if (dataNames.isEmpty())
                {
                    CubeEngine.getLogger().warning("Unknown Block-Data: "+ mat + "DATA: "+blockData);
                    return null;
                }
            }
            else
                dataNames += ":"+names.iterator().next();
        }
        if (dataNames.isEmpty())
        {
            CubeEngine.getLogger().warning("Unknown Block-Data: "+ mat + "DATA: "+blockData);
            return null;
        }
        return dataNames;
    }
}
