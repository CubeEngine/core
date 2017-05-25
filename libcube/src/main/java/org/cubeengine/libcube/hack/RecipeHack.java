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
package org.cubeengine.libcube.hack;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// omg pls don't look it might not even work in production
public class RecipeHack
{
    private static Method addRecipe;
    private static Object manager;

    static
    {
        try
        {
            Class<?> c = Class.forName("net.minecraft.item.crafting.CraftingManager");
            manager = c.getDeclaredMethod("getInstance").invoke(null);
            addRecipe = c.getMethod("addRecipe", ItemStack.of(ItemTypes.AIR, 1).getClass(), Object[].class);
        }
        catch (ReflectiveOperationException e)
        {

        }
    }

    public static Object addRecipe(ItemStack stack, String[] grid, Map<Character, ItemStack> items)
    {
        List<Object> list = new ArrayList<>();
        list.add(grid);
        for (Map.Entry<Character, ItemStack> entry : items.entrySet())
        {
            list.add(entry.getKey());
            list.add(entry.getValue());
        }
        try
        {
            return addRecipe.invoke(manager, stack, list.toArray());
        }
        catch (ReflectiveOperationException e)
        {
            return null;
        }
    }

    public static Object addRecipe2(ItemStack stack, String[] grid, Map<Character, ItemType> items)
    {
        List<Object> list = new ArrayList<>();
        list.add(grid);
        for (Map.Entry<Character, ItemType> entry : items.entrySet())
        {
            list.add(entry.getKey());
            list.add(entry.getValue());
        }
        try
        {
            return addRecipe.invoke(manager, stack, list.toArray());
        }
        catch (ReflectiveOperationException e)
        {
            return null;
        }
    }

    public static Object addRecipe3(ItemStack stack, String[] grid, Map<Character, BlockType> items)
    {
        List<Object> list = new ArrayList<>();
        list.add(grid);
        for (Map.Entry<Character, BlockType> entry : items.entrySet())
        {
            list.add(entry.getKey());
            list.add(entry.getValue());
        }
        try
        {
            return addRecipe.invoke(manager, stack, list.toArray());
        }
        catch (ReflectiveOperationException e)
        {
            return null;
        }
    }


}


//this.addRecipe(new ItemStack(Blocks.DISPENSER, 1),