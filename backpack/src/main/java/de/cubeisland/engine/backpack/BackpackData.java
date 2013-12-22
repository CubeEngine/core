package de.cubeisland.engine.backpack;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.configuration.Configuration;
import de.cubeisland.engine.core.config.codec.NBTCodec;

public class BackpackData extends Configuration<NBTCodec>
{
    public String name;
    public boolean allowItemsIn = true;
    public List<ItemStack> contents = new ArrayList<>(); // TODO register converter for nbt codec
}
