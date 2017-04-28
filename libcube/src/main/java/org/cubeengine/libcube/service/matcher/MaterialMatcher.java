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
package org.cubeengine.libcube.service.matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.inject.Inject;

import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.modularity.core.marker.Enable;
import org.cubeengine.reflect.Reflector;
import org.cubeengine.libcube.service.config.BlockTypeConverter;
import org.cubeengine.libcube.service.config.ItemStackConverter;
import org.cubeengine.libcube.service.config.SimpleItemStackConverter;
import org.cubeengine.libcube.service.config.ItemTypeConverter;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockState.Builder;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataRegistrationNotFoundException;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

import static java.util.stream.Collectors.toList;
import static org.spongepowered.api.item.ItemTypes.*;

/**
 * This Matcher provides methods to match Material or Items.
 */
@ServiceProvider(MaterialMatcher.class)
public class MaterialMatcher
{
    @Inject private PluginContainer plugin;

    private final Map<String, ItemType> names = new HashMap<>();
    private final Map<Integer, ItemType> legacyIds = new HashMap<>(); // TODO fill legacy map
    private final Map<String, ItemType> ids = new HashMap<>();
    private final Map<BlockType, Map<String, BlockState>> variantMap = new HashMap<>();

    private final Map<Locale, Map<String, ItemType>> localizedNames = new HashMap<>();
    private final Map<Locale, Map<String, BlockState>> localizedVariantMap = new HashMap<>();

    private final Map<Locale, Map<String, ItemStack>> localizedStackMap = new HashMap<>();

    //private final Map<String, ItemStack> fromBlockStates = new HashMap<>();

    private final Set<ItemType> repairableMaterials = Collections.synchronizedSet(new HashSet<>(Arrays.asList(
                                                                                      IRON_SHOVEL, IRON_PICKAXE,
                                                                                      IRON_AXE, IRON_SWORD, IRON_HOE,
                                                                                      WOODEN_SHOVEL, WOODEN_PICKAXE,
                                                                                      WOODEN_AXE, WOODEN_SWORD,
                                                                                      WOODEN_HOE, STONE_SHOVEL,
                                                                                      STONE_PICKAXE, STONE_AXE,
                                                                                      STONE_SWORD, STONE_HOE,
                                                                                      DIAMOND_SHOVEL, DIAMOND_PICKAXE,
                                                                                      DIAMOND_AXE, DIAMOND_SWORD,
                                                                                      DIAMOND_HOE, GOLDEN_SHOVEL,
                                                                                      GOLDEN_PICKAXE, GOLDEN_AXE,
                                                                                      GOLDEN_SWORD, GOLDEN_HOE,
                                                                                      LEATHER_HELMET,
                                                                                      LEATHER_CHESTPLATE,
                                                                                      LEATHER_LEGGINGS, LEATHER_BOOTS,
                                                                                      CHAINMAIL_HELMET,
                                                                                      CHAINMAIL_CHESTPLATE,
                                                                                      CHAINMAIL_LEGGINGS,
                                                                                      CHAINMAIL_BOOTS, IRON_HELMET,
                                                                                      IRON_CHESTPLATE, IRON_LEGGINGS,
                                                                                      IRON_BOOTS, DIAMOND_HELMET,
                                                                                      DIAMOND_CHESTPLATE,
                                                                                      DIAMOND_LEGGINGS, DIAMOND_BOOTS,
                                                                                      GOLDEN_HELMET, GOLDEN_CHESTPLATE,
                                                                                      GOLDEN_LEGGINGS, GOLDEN_BOOTS,
                                                                                      FLINT_AND_STEEL, BOW, FISHING_ROD,
                                                                                      SHEARS)));
    @Inject private StringMatcher stringMatcher;
    private Map<String, BlockState> blockStateItems;

    @Inject
    public MaterialMatcher(Reflector reflector)
    {
        reflector.getDefaultConverterManager().registerConverter(new SimpleItemStackConverter(this));
        reflector.getDefaultConverterManager().registerConverter(new ItemStackConverter(), ItemStack.class);
        reflector.getDefaultConverterManager().registerConverter(new ItemTypeConverter(this), ItemType.class);
        reflector.getDefaultConverterManager().registerConverter(new BlockTypeConverter(this), BlockType.class);

        // Read names from GameDictionary
        try
        {
            for (Entry<String, GameDictionary.Entry> entry : Sponge.getGame().getGameDictionary().getAll().entries())
            {
                names.put(entry.getKey(), entry.getValue().getType());
            }
        }
        catch (Exception e) // TODO remove when SpongeVanilla has it
        {
            System.err.println("Could not access GameDictionary! Material matching may not work as expected");
        }

        Map<String, ItemType> defLocalizedName = new HashMap<>();
        Map<String, ItemType> localizedName = new HashMap<>();
        localizedNames.put(Locale.getDefault(), defLocalizedName);
        localizedNames.put(Locale.US, localizedName);
        buildLocalizedNames(defLocalizedName, localizedName);
    }

    @Enable
    public void onEnable()
    {
        Sponge.getScheduler().createTaskBuilder().delayTicks(0).execute(() -> {
            this.blockStateItems = buildBlockStateItems(); // Helper
            this.variantMap.putAll(buildVariantMap());
            this.localizedVariantMap.put(Locale.getDefault(), buildLocalizedVariantMap(Locale.getDefault()));
            this.localizedVariantMap.put(Locale.US, buildLocalizedVariantMap(Locale.US));
            // TODO legacy ID -> ItemType Map

            for (Entry<Locale, Map<String, ItemType>> entry : localizedNames.entrySet())
            {
                Map<String, ItemStack> map = localizedStackMap.get(entry.getKey());
                if (map == null)
                {
                    localizedStackMap.put(entry.getKey(), map = new HashMap<>());
                }
                buildLocalizedStackMapFromType(entry.getValue(), map);
            }
            for (Entry<Locale, Map<String, BlockState>> entry : localizedVariantMap.entrySet())
            {
                Map<String, ItemStack> map = localizedStackMap.get(entry.getKey());
                if (map == null)
                {
                    localizedStackMap.put(entry.getKey(), map = new HashMap<>());
                }
                buildLocalizedStackMapFromState(entry.getValue(), map);
            }
        }).submit(plugin);
    }

    private void buildLocalizedStackMapFromState(Map<String, BlockState> value, Map<String, ItemStack> map)
    {
        for (Entry<String, BlockState> entry : value.entrySet())
        {
            if (!map.containsKey(entry.getKey()))
            {
                map.put(entry.getKey(), ItemStack.builder().fromBlockState(entry.getValue()).quantity(1).build());
            }
        }
    }

    private void buildLocalizedStackMapFromType(Map<String, ItemType> value, Map<String, ItemStack> map) {
        for (Entry<String, ItemType> entry : value.entrySet())
        {
            map.put(entry.getKey(), ItemStack.of(entry.getValue(), 1));
        }
    }

    private void buildLocalizedNames(Map<String, ItemType> defLocalizedName, Map<String, ItemType> localizedName) {
        for (ItemType itemType : Sponge.getRegistry().getAllOf(ItemType.class))
        {
            String id = itemType.getName();
            ids.put(id, itemType);
            if (id.startsWith("minecraft:"))
            {
                ids.put(id.substring("minecraft:".length()), itemType);
            }
            String defName = itemType.getTranslation().get(Locale.getDefault());
            defLocalizedName.put(defName, itemType);
            String[] splitDefName = defName.split(" ");
            if (splitDefName.length > 1) {
                defName = splitDefName[splitDefName.length - 1];
                defName += String.join(" ", splitDefName).replace(defName, "");
                defLocalizedName.put(defName, itemType);
            }
            String sourceName = itemType.getTranslation().get(Locale.US);
            localizedName.put(sourceName, itemType);
            String[] splitSourceName = sourceName.split(" ");
            if (splitSourceName.length > 1) {
                sourceName = splitSourceName[splitSourceName.length - 1];
                sourceName += String.join(" ", splitSourceName).replace(sourceName, "");
                localizedName.put(sourceName, itemType);
            }
        }
    }

    private Map<String, BlockState> buildLocalizedVariantMap(Locale locale)
    {
        HashMap<String, BlockState> map = new HashMap<>();

        for (BlockState blockState : blockStateItems.values())
        {
            map.put(ItemStack.builder().fromBlockState(blockState).build().getTranslation().get(locale), blockState);
        }
        return map;
    }

    private Map<BlockType, Map<String, BlockState>> buildVariantMap()
    {
        Map<BlockType, Map<String, BlockState>> blockStateItemsByType = new HashMap<>();
        for (Entry<String, BlockState> entry : blockStateItems.entrySet())
        {
            BlockType itemType = entry.getValue().getType();

            Map<String, BlockState> itemTypes = blockStateItemsByType.get(itemType);
            if (itemTypes == null)
            {
                itemTypes = new HashMap<>();
                blockStateItemsByType.put(itemType, itemTypes);
            }
            itemTypes.put(entry.getKey(), entry.getValue());
        }

        Map<BlockType, Map<String, BlockState>> variants = new HashMap<>();
        blockStateItemsByType.entrySet().stream().filter(e -> e.getValue().size() != 1).forEach(e -> {

            Map<String, Set<String>> variantNames = new HashMap<>();
            Map<List<String>, BlockState> fullVariant = new HashMap<>();
            for (Entry<String, BlockState> entry : e.getValue().entrySet())
            {
                String variant = entry.getKey();
                variant = variant.substring(variant.indexOf("[") + 1, variant.indexOf("]"));
                String[] split = variant.split(","); // multiple variants
                fullVariant.put(Arrays.asList(split), entry.getValue());
                for (String variantEntry : split)
                {
                    String[] variantEntryPart = variantEntry.split("=");
                    Set<String> variantValues = variantNames.get(variantEntryPart[0]);
                    if (variantValues == null)
                    {
                        variantValues = new HashSet<>();
                        variantNames.put(variantEntryPart[0], variantValues);
                    }
                    variantValues.add(variantEntryPart[1]);
                }
            }

            for (Entry<String, Set<String>> entry : variantNames.entrySet())
            {
                if (entry.getKey().equals("axis") || entry.getKey().equals("facing") ||
                    entry.getKey().equals("half") || entry.getKey().equals("shape") ||
                    entry.getKey().equals("open") || entry.getKey().equals("powered") ||
                    entry.getKey().equals("stage") || entry.getKey().equals("decayable"))
                {
                    Map<List<String>, BlockState> filtered = new HashMap<>();
                    for (Entry<List<String>, BlockState> offender : fullVariant.entrySet())
                    {
                        List<String> key = new ArrayList<>(offender.getKey());
                        for (String fv : entry.getValue())
                        {
                            key.remove(entry.getKey() + "=" + fv);
                        }
                        if (!key.isEmpty())
                        {
                            filtered.put(key, offender.getValue());
                        }
                    }
                    fullVariant = filtered;
                }
                if (entry.getValue().size() == 1)
                {
                    String singleVariant = entry.getKey() + "=" + entry.getValue().iterator().next();
                    fullVariant = fullVariant.entrySet().stream().collect(Collectors.toMap(fv -> {
                        List<String> split = new ArrayList<>(fv.getKey());
                        split.remove(singleVariant);
                        return split;
                    }, Entry::getValue));
                }
            }
            for (Entry<List<String>, BlockState> variant : fullVariant.entrySet())
            {
                if (variant.getKey().size() > 1)
                {
                    System.out.print(e.getKey().getName() + " Has multiple Variants:");
                    for (String s : variant.getKey())
                    {
                        System.out.print(" " + s);
                    }
                    System.out.print("\n");
                }
            }
            variants.put(e.getKey(), fullVariant.entrySet().stream().collect(Collectors.toMap(en ->
                String.join(" ", en.getKey().stream().map(s -> s.split("=")[1]).collect(toList())) , Entry::getValue)));
        });

        /*
        for (Entry<ItemType, Map<String, ItemStack>> variant : variants.entrySet())
        {
            System.out.print(variant.getKey().getName() + ":\n");
            for (Entry<String, ItemStack> entry : variant.getValue().entrySet())
            {
                System.out.print("  " + entry.getKey() + ": " + entry.getValue().getTranslation().get() + "\n");
            }
        }
        */
        return variants;
    }

    private Map<String, BlockState> buildBlockStateItems() {
        Map<String, BlockState> blockStateItems = new HashMap<>();
        for (BlockState blockState : Sponge.getRegistry().getAllOf(BlockState.class))
        {
            try
            {
                ItemStack item = ItemStack.builder().fromBlockState(blockState).build();

                Builder state = BlockState.builder().blockType(item.getItem().getBlock().get());

                blockState.getKeys().stream().map(Key.class::cast).forEach(
                    k -> {
                        Optional value = item.get(k);
                        if (value.isPresent())
                        {
                            state.add(k, value.get());
                        }
                    });


                BlockState finalState = state.build();
                /*
                ItemStack.Builder builder = ItemStack.builder().itemType(finalState.getType().getItem().get());
                blockState.getKeys().stream().map(Key.class::cast).forEach(
                    k -> {
                        Optional value = finalState.get(k);
                        if (value.isPresent())
                        {
                            builder.add(k, value.get());
                        }
                    });
                 */
                blockStateItems.put(finalState.getName(), finalState);
            }
            catch (IllegalArgumentException | DataRegistrationNotFoundException ignored)
            {}
        }
        return blockStateItems;
    }

    private ItemType matchWithLevenshteinDistance(String s, Map<String, ItemType> map)
    {

        String t_key = stringMatcher.matchString(s, map.keySet());
        if (t_key != null)
        {
            return map.get(t_key);
        }
        return null;
    }

    private HashMap<ItemStack, Double> allMatchesWithLevenshteinDistance(String s, Map<String, ItemType> map,
                                                                         int maxDistance, int minPercentage)
    {
        HashMap<ItemStack, Double> itemMap = new HashMap<>();
        TreeMap<String, Integer> itemNameList = stringMatcher.getMatches(s, map.keySet(), maxDistance, true);

        for (Entry<String, Integer> entry : itemNameList.entrySet())
        {
            double curPercentage = (entry.getKey().length() - entry.getValue()) * 100 / entry.getKey().length();
            if (curPercentage >= minPercentage)
            {
                itemMap.put(ItemStack.builder().itemType(map.get(entry.getKey())).build(), curPercentage);
            }
        }

        return itemMap;
    }

    /**
     * Tries to match a ItemStack for given name
     *
     * @param name the name
     * @return the found ItemStack
     */
    public ItemStack itemStack(String name)
    {
        return this.itemStack(name, Locale.getDefault());

    }

    public ItemStack itemStack(String name, Locale locale)
    {
        if (name == null)
        {
            return null;
        }

        Map<String, ItemStack> map = localizedStackMap.get(locale);
        if (map != null)
        {
            String match = stringMatcher.matchString(name, map.keySet());
            ItemStack itemStack = map.get(match);
            if (itemStack != null)
            {
                return itemStack.copy();
            }
        }

        String[] parts = name.toLowerCase(Locale.ENGLISH).split("=");
        String[] typeName = parts[0].split(":");
        ItemType type = material(typeName[0], locale);
        if (type == null)
        {
            return null;
        }

        if (parts.length > 1)
        {
            String variant = parts[1];
            if (type.getBlock().isPresent())
            {
                Map<String, BlockState> variants = variantMap.get(type.getBlock().get());
                if (variants != null)
                {
                    String match2 = stringMatcher.matchString(variant, variants.keySet());
                    if (match2 != null)
                    {
                        return ItemStack.builder().fromBlockState(variants.get(match2)).quantity(1).build();
                    }
                }
            }
        }
        ItemStack.Builder builder = ItemStack.builder().itemType(type).quantity(1);
        if (typeName.length == 2)
        {
            try
            {
                builder.add(Keys.ITEM_DURABILITY, Integer.valueOf(typeName[1]));
            }
            catch (IllegalArgumentException e)
            {
                return null;
            }
        }
        return builder.build();
    }


    /**
     * Tries to match a ItemStack-list for given name
     *
     * @param name the name
     * @return the found ItemStack-list
     */
    public List<ItemStack> itemStackList(String name)
    {
        if (name == null)
        {
            return null;
        }

        name = name.toLowerCase(Locale.ENGLISH);

        ItemStack best = itemStack(name);

        HashMap<ItemStack, Double> itemMap = this.allMatchesWithLevenshteinDistance(name, names, 5, 50);
        itemMap.put(best, 0d);
        TreeSet<Entry<ItemStack, Double>> itemSet = new TreeSet<>(new ItemStackComparator());
        itemSet.addAll(itemMap.entrySet());

        return itemSet.stream().map(Entry::getKey).collect(toList());
    }

    /**
     * Tries to match a Material for given name
     *
     * @param name the name
     * @return the material or null if not found
     */
    public ItemType material(String name)
    {
        return material(name, Locale.getDefault());
    }

    public ItemType material(String name, Locale locale)
    {
        ItemType type = null;
        Map<String, ItemType> map = localizedNames.get(locale);
        if (map != null)
        {
            type = map.get(name);
            if (type == null)
            {
                String match = stringMatcher.matchString(name, map.keySet());
                type = map.get(match);
            }
        }
        else
        {
            System.out.print("Localized Name Map not generated for: " + locale.getDisplayName());
        }

        if (type != null) return type;
        type = this.names.get(name); //direct match
        if (type != null) return type;
        try
        {
            type = legacyIds.get(Integer.valueOf(name));
        }
        catch (NumberFormatException e)
        {
            String match = stringMatcher.matchString(name, names.keySet());
            type = names.get(match);

            if (type == null)
            {
                match = stringMatcher.matchString(name, ids.keySet());
                type = ids.get(match);
            }
        }
        return type;
    }


    public BlockType block(String name)
    {
        return Sponge.getRegistry().getType(BlockType.class, name).orElse(null);
    }

    /**
     * Returns whether the given ItemStack is repairable
     */
    public boolean repairable(ItemStack item)
    {
        return item != null && this.repairableMaterials.contains(item.getItem());
    }

    /**
     * Returns the name for given ItemStack
     *
     * @param item the item
     * @return the name or null if none was found
     */
    public String getNameFor(ItemStack item)
    {
        return item.getItem().getTranslation().get();
    }

    private static class ItemStackComparator implements Comparator<Entry<ItemStack, Double>>
    {
        @Override
        public int compare(Entry<ItemStack, Double> item1, Entry<ItemStack, Double> item2)
        {
            if (item1.getValue() > item2.getValue())
            {
                return -1;
            }
            else if (item1.getValue() < item2.getValue())
            {
                return 1;
            }
            return 0;
        }
    }
}
