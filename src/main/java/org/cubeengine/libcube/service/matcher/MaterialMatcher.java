/*
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

import static java.util.stream.Collectors.toList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.TranslatableComponent;
import org.cubeengine.libcube.LibCube;
import org.cubeengine.libcube.ModuleManager;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockState.Builder;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataRegistrationNotFoundException;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This Matcher provides methods to match Material or Items.
 */
@Singleton
public class MaterialMatcher
{
    private PluginContainer plugin;

    private final Map<String, ItemType> names = new HashMap<>();
    private final Map<String, ItemType> ids = new HashMap<>();
    private final Map<BlockType, Map<String, BlockState>> variantMap = new HashMap<>();

    private final Map<Locale, Map<String, ItemType>> localizedNames = new HashMap<>();
    private final Map<Locale, Map<String, BlockState>> localizedVariantMap = new HashMap<>();

    private final Map<Locale, Map<String, ItemStack>> localizedStackMap = new HashMap<>();

    @Inject private StringMatcher stringMatcher;
    private Map<String, BlockState> blockStateItems;

    @Inject
    public <T extends CatalogType> MaterialMatcher(ModuleManager mm)
    {
        this.plugin = mm.getPlugin(LibCube.class).get();

        for (ItemType type : Sponge.getRegistry().getCatalogRegistry().getAllOf(ItemType.class)) {
            final String translationKey = ((TranslatableComponent) type.asComponent()).key();
            // TODO get translation on server? GameDictionary?

            // TODO Keys.IS_REPAIRABLE
        }

        Map<String, ItemType> defLocalizedName = new HashMap<>();
        Map<String, ItemType> localizedName = new HashMap<>();
        localizedNames.put(Locale.getDefault(), defLocalizedName);
        localizedNames.put(Locale.US, localizedName);
        buildLocalizedNames(defLocalizedName, localizedName);
// TODO Taskbuilder        onEnable();
    }

    public void onEnable()
    {
        Sponge.getAsyncScheduler().submit(Task.builder().execute(() -> {
            this.blockStateItems = buildBlockStateItems(); // Helper
            this.variantMap.putAll(buildVariantMap());
            this.localizedVariantMap.put(Locale.getDefault(), buildLocalizedVariantMap(Locale.getDefault()));
            this.localizedVariantMap.put(Locale.US, buildLocalizedVariantMap(Locale.US));
            // TODO legacy ID -> ItemType Map

            for (Entry<Locale, Map<String, ItemType>> entry : localizedNames.entrySet())
            {
                Map<String, ItemStack> map = localizedStackMap.computeIfAbsent(entry.getKey(), k -> new HashMap<>());
                buildLocalizedStackMapFromType(entry.getValue(), map);
            }
            for (Entry<Locale, Map<String, BlockState>> entry : localizedVariantMap.entrySet())
            {
                Map<String, ItemStack> map = localizedStackMap.computeIfAbsent(entry.getKey(), k -> new HashMap<>());
                buildLocalizedStackMapFromState(entry.getValue(), map);
            }
        }).build());
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
        for (ItemType itemType : Sponge.getRegistry().getCatalogRegistry().getAllOf(ItemType.class))
        {
            final ResourceKey id = itemType.getKey();
            ids.put(id.asString(), itemType);
            if ("minecraft:".equals(id.getNamespace()))
            {
                ids.put(id.getValue(), itemType);
            }
// TODO get other language translations for items
//            String defName = itemType.getTranslation().get(Locale.getDefault());
//            defLocalizedName.put(defName, itemType);
//            String[] splitDefName = defName.split(" ");
//            if (splitDefName.length > 1) {
//                defName = splitDefName[splitDefName.length - 1];
//                defName += String.join(" ", splitDefName).replace(defName, "");
//                defLocalizedName.put(defName, itemType);
//            }

// TODO get default language translatiosn for items
            String sourceName = ((TranslatableComponent)itemType.asComponent()).key();// TODO get(Locale.US);
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
            final String transKey = ((TranslatableComponent) blockState.getType().asComponent()).key();
            // TODO locale
            map.put(transKey, blockState);
        }
        return map;
    }

    private Map<BlockType, Map<String, BlockState>> buildVariantMap()
    {
        Map<BlockType, Map<String, BlockState>> blockStateItemsByType = new HashMap<>();
        for (Entry<String, BlockState> entry : blockStateItems.entrySet())
        {
            BlockType itemType = entry.getValue().getType();

            Map<String, BlockState> itemTypes = blockStateItemsByType.computeIfAbsent(itemType, k -> new HashMap<>());
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
                    Set<String> variantValues = variantNames.computeIfAbsent(variantEntryPart[0], k -> new HashSet<>());
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
                    System.out.print(e.getKey().getKey() + " has multiple Variants:");
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
        Collection<BlockState> blocks = Sponge.getRegistry().getCatalogRegistry().getAllOf(BlockState.class);
        //System.out.println("Loading Names for " + blocks.size() + " Blockstates");
        for (BlockState blockState : blocks)
        {
            try
            {
                if (!blockState.getType().getItem().isPresent())
                {
                    continue;
                }
                ItemStack item = ItemStack.builder().fromBlockState(blockState).build();

                Builder state = BlockState.builder().blockType(item.getType().getBlock().get());

                blockState.getKeys().stream().map(Key.class::cast).forEach(
                    k -> {
                        Optional<?> value = item.get(k);
                        value.ifPresent(o -> state.add(k, o));
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
                blockStateItems.put(finalState.getKey().asString(), finalState);
            }
            catch (IllegalArgumentException | DataRegistrationNotFoundException ignored)
            {}
        }
        return blockStateItems;
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
        String[] typeName = parts[0].split("=");
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
        String match = stringMatcher.matchString(name, names.keySet());
        type = names.get(match);

        if (type == null)
        {
            match = stringMatcher.matchString(name, ids.keySet());
            type = ids.get(match);
        }
        return type;
    }

}
