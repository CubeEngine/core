package de.cubeisland.cubeengine.log.action.logaction.container;

import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.cubeengine.core.util.matcher.Match;

public class ContainerType
{
    private static final String STORAGE_MINECART = "storage-minecart";
    private static final String CHEST = "chest";
    private static final String FURNACE = "furnace";
    private static final String DISPENSER = "dispenser";
    private static final String DROPPER = "dropper";
    private static final String HOPPER = "hopper";
    private static final String BREWING_STAND = "brewing-stand";

    public final String name;

    private ContainerType(String name)
    {
        this.name = name;
    }

    public static ContainerType ofName(String name)
    {
        String match = Match.string().matchString(name,STORAGE_MINECART,CHEST,FURNACE,DISPENSER,DROPPER,HOPPER,BREWING_STAND);
        if (match == null)
        {
            return null;
        }
        return new ContainerType(match);
    }

    public ContainerType (InventoryHolder holder)
    {
        if (holder instanceof StorageMinecart)
        {
            this.name = STORAGE_MINECART;
        }
        else if (holder instanceof DoubleChest)
        {
            this.name = CHEST;
        }
        else if (holder instanceof Chest)
        {
            this.name = CHEST;
        }
        else if (holder instanceof Furnace)
        {
            this.name = FURNACE;
        }
        else if (holder instanceof Dispenser)
        {
            this.name = DISPENSER;
        }
        else if (holder instanceof BrewingStand)
        {
            this.name = BREWING_STAND;
        }
        else if (holder instanceof Dropper)
        {
            this.name = DROPPER;
        }
        else if (holder instanceof Hopper)
        {
            this.name = HOPPER;
        }
        else
        {
            this.name = null;
        }
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ContainerType that = (ContainerType)o;

        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : 0;
    }
}

