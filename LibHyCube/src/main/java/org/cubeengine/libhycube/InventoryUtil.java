package org.cubeengine.libhycube;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Identifiable;

import static org.spongepowered.api.block.BlockTypes.*;

public class InventoryUtil
{
    public static Map<String, CustomInventory> inv = new HashMap<>();

    public static Object buildInventory()
    {
        return buildChestInventory(3);
    }

    public static Object buildInventory(BlockType type)
    {
        return buildInventory(type, -1);
    }

    public static Object buildChestInventory(int rows)
    {
        return buildInventory(CHEST, rows * 9);
    }

    private static Object buildInventory(BlockType type, int size)
    {
        if (type == CHEST)
        {
            size = size == -1 ? 9 * 3 : size;
        }
        else if (type == CRAFTING_TABLE)
        {
            size = 0;
        }
        else if (type == FURNACE)
        {
            size = 3;
        }
        else if (type == DISPENSER)
        {
            size = 9;
        }
        else if (type == ENCHANTING_TABLE)
        {
            size = 0;
        }
        else if (type == BREWING_STAND)
        {
            size = 4;
        }
        else if (type == BEACON)
        {
            size = 1;
        }
        else if (type == ANVIL)
        {
            size = 0;
        }
        else if (type == HOPPER)
        {
            size = 5;
        }
        else if (type == DROPPER)
        {
            size = 9;
        }
        else
        {
            size = 9 * 3;
        }
        CustomInventory inventory = inv.get(type.getId() + "#" + size);
        if (inventory != null)
        {
            return inventory;
        }
        inventory = new CustomInventory(type, "Portable Enchantingtable", true, size);
        inv.put(type.getId() + "#" + size, inventory);
        return inventory;
    }

    public static void openInventory(Object inventory, Player player)
    {
        CustomInventory i = (CustomInventory)inventory;
        EntityPlayerMP p = (EntityPlayerMP)player;

        i.open(p);

    }

    public static class CustomInventory implements IInteractionObject, IInventory
    {
        private final InventoryBasic inventory;
        private BlockType type;

        public CustomInventory(BlockType type, String title, boolean customName, int slotCount)
        {
            this.inventory = new InventoryBasic(title, customName, slotCount);
            this.type = type;
        }

        public void openInventory(EntityPlayerMP p)
        {
            inventory.openInventory(p);
        }

        public IChatComponent getDisplayName()
        {
            return inventory.getDisplayName();
        }

        public int getSize()
        {
            return inventory.getSizeInventory();
        }

        @Override
        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player)
        {
            if (type == CHEST)
            {
                return new ContainerChest(player.inventory, inventory, player);
            }
            else if (type == CRAFTING_TABLE)
            {
                return new PortableContainerWorkbench(player);
            }
            else if (type == FURNACE)
            {
                return new ContainerFurnace(playerInventory, inventory);
            }
            else if (type == DISPENSER)
            {
                return new ContainerDispenser(playerInventory, inventory);
            }
            else if (type == ENCHANTING_TABLE)
            {
                return new PortableContainerEnchantment(player);
            }
            else if (type == BREWING_STAND)
            {
                return new ContainerBrewingStand(playerInventory, inventory);
            }
            else if (type == BEACON)
            {
                return new ContainerBeacon(playerInventory, inventory);
            }
            else if (type == ANVIL)
            {
                return new PortableContainerAnvil(player);
            }
            else if (type == HOPPER)
            {
                return new ContainerHopper(playerInventory, inventory, player);
            }
            else if (type == DROPPER)
            {
                return new ContainerDispenser(playerInventory, inventory);
            }
            else
            {
                return new ContainerChest(playerInventory, inventory, player);
            }
            // TODO HorseInventories
        }

        /*
            minecraft:chest 	Chest, large chest, or minecart with chest
            minecraft:crafting_table 	Crafting table
            minecraft:furnace 	Furnace
            minecraft:dispenser 	Dispenser
            minecraft:enchanting_table 	Enchantment table
            minecraft:brewing_stand 	Brewing stand
            minecraft:villager 	Villager
            minecraft:beacon 	Beacon
            minecraft:anvil 	Anvil
            minecraft:hopper 	Hopper or minecart with hopper
            minecraft:dropper 	Dropper
            EntityHorse 	Horse, donkey, or mule
         */
        @Override
        public String getGuiID()
        {
            return type.getName();
            //return "unknown";
        }

        @Override
        public String getName()
        {
            return inventory.getName();
        }

        @Override
        public boolean hasCustomName()
        {
            return inventory.hasCustomName();
        }

        public void open(EntityPlayerMP p)
        {
            if (type == CRAFTING_TABLE)
            {
                p.displayGui(this);
            }
            else
            {
                p.displayGUIChest(this);
            }
        }

        // Inventory proxy

        @Override
        public int getSizeInventory()
        {
            return inventory.getSizeInventory();
        }

        @Override
        public ItemStack getStackInSlot(int index)
        {
            return inventory.getStackInSlot(index);
        }

        @Override
        public ItemStack decrStackSize(int index, int count)
        {
            return inventory.decrStackSize(index, count);
        }

        @Override
        public ItemStack removeStackFromSlot(int index)
        {
            return inventory.removeStackFromSlot(index);
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack)
        {
            inventory.setInventorySlotContents(index, stack);
        }

        @Override
        public int getInventoryStackLimit()
        {
            return inventory.getInventoryStackLimit();
        }

        @Override
        public void markDirty()
        {
            inventory.markDirty();
        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player)
        {
            return inventory.isUseableByPlayer(player);
        }

        @Override
        public void openInventory(EntityPlayer player)
        {
            inventory.openInventory(player);
        }

        @Override
        public void closeInventory(EntityPlayer player)
        {
            inventory.closeInventory(player);
        }

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack)
        {
            return inventory.isItemValidForSlot(index, stack);
        }

        @Override
        public int getField(int id)
        {
            return inventory.getField(id);
        }

        @Override
        public void setField(int id, int value)
        {
            inventory.setField(id, value);
        }

        @Override
        public int getFieldCount()
        {
            return inventory.getFieldCount();
        }

        @Override
        public void clear()
        {
            inventory.clear();
        }
    }



    // Portable Containers

    private static class PortableContainerWorkbench extends ContainerWorkbench
    {
        public PortableContainerWorkbench(EntityPlayer p)
        {
            super(p.inventory, p.worldObj, p.getPosition());
        }

        @Override
        public boolean canInteractWith(EntityPlayer playerIn)
        {
            return true;
        }
    }

    private static class PortableContainerAnvil extends ContainerRepair
    {
        public PortableContainerAnvil(EntityPlayer p)
        {
            super(p.inventory, p.worldObj, p.getPosition(), p);
        }

        @Override
        public boolean canInteractWith(EntityPlayer playerIn)
        {
            return true;
        }
    }

    private static class PortableContainerEnchantment extends ContainerEnchantment
    {
        public PortableContainerEnchantment(EntityPlayer p)
        {
            super(p.inventory, p.worldObj, p.getPosition());
        }

        @Override
        public boolean canInteractWith(EntityPlayer playerIn)
        {
            return true;
        }
    }




}
