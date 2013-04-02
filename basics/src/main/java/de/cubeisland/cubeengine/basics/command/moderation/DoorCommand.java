package de.cubeisland.cubeengine.basics.command.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.math.Vector3;
import de.cubeisland.cubeengine.core.util.math.shape.Cylinder;
import de.cubeisland.cubeengine.core.util.math.shape.Shape;
import de.cubeisland.cubeengine.core.util.math.shape.Sphere;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.Openable;

public class DoorCommand
{
    private Basics basics;
    private Set<Block> block = new HashSet<Block>();

    public DoorCommand(Basics basics)
    {
        this.basics = basics;
    }

    @Command(
            desc = "",
            max = 0)
    public void shaperollback(CommandContext context)
    {
        Iterator<Block> iter = this.block.iterator();
        while(iter.hasNext())
        {
            Block block = iter.next();
            block.setType(Material.AIR);
            iter.remove();
        }
    }

    @Command(
            desc = "",
            min = 3,
            max = 6)
    public void shape(CommandContext context)
    {
        User user = (User) context.getSender();
        int size = 0;
        int width = context.getArg(0, Integer.class);
        int height = context.getArg(1, Integer.class);
        int depth = context.getArg(2, Integer.class);
        int rotx = context.getArg(3, Integer.class, 0);
        int roty = context.getArg(4, Integer.class, 0);
        int rotz = context.getArg(5, Integer.class, 0);

        Block block = user.getTargetBlock(null, 20);

        Shape shape = new Cylinder(new Vector3(block.getX(), block.getY(), block.getZ()), width, height, depth);
        shape = shape.rotate(new Vector3(rotx, roty, rotz));
        for(Vector3 p : shape)
        {
            Block block2 = user.getWorld().getBlockAt((int) p.x, (int) p.y, (int) p.z);
            block2.setType(Material.DIRT);
            this.block.add(block2);
            size++;
        }
        context.sendMessage("finished " + size);
    }

    @Command(
        desc = "",
        usage = "<open|close> <radius> <world> <x> <y> <z>",
        min = 2,
        max = 6
    )
    public void doors(CommandContext context)   //TODO flags and permissions for all types of doors
    {   
        // flags % permission -> iron door, trap door, fence gate
        boolean open = false;
        int radius = context.getArg(1, Integer.class, 0);
        Vector3 vector;
        World world;
        
        String task = context.getString(0);
        if(task.equalsIgnoreCase("open"))
        {
            open = true;
        }
        else if(task.equalsIgnoreCase("close"))
        {
            open = false;
        }
        else
        {
            context.sendTranslated("&cDo not know whether I should close or open the doors");
            return;
        }
        
        if(radius > this.basics.getConfiguration().maxDoorRadius)
        {
            context.sendTranslated("&cYou can't execute this with a radius over %d", this.basics.getConfiguration().maxDoorRadius);
            return;
        }
        
        if(!context.hasArg(5) && !(context.getSender() instanceof User))
        {
            context.sendTranslated("&cYou has to specify a location!");
            return;
        }
        else if(!context.hasArg(5))
        {
            Location location = ((User)context.getSender()).getLocation();
            world = location.getWorld();
            vector = new Vector3(location.getX(), location.getY(), location.getZ());
        }
        else if(!BasicsPerm.COMMAND_DOOR_LOCATION.isAuthorized(context.getSender()))
        {
            context.sendTranslated("&cYou are not allowed to open/close doors at other locations");
            return;
        }
        else
        {
            world = context.getArg(2, World.class, null);
            if(world == null)
            {
                context.sendTranslated("&cWorld &6%s &cnot found!", context.getString(2));
                return;
            }
            Double x = context.getArg(3, Double.class, null);
            if(x == null)
            {
                context.sendTranslated("&cx-value &6%s &cis not supported!", context.getString(3));
                return;
            }
            Double y = context.getArg(4, Double.class, null);
            if(y == null)
            {
                context.sendTranslated("&cy-value &6%s &cis not supported!", context.getString(4));
                return;
            }
            Double z = context.getArg(5, Double.class, null);
            if(z == null)
            {
                context.sendTranslated("&cz-value &6%s &cis not supported!", context.getString(5));
                return;
            }
            vector = new Vector3(x, y, z);
        }
        
        Sphere sphere = new Sphere(vector, radius);
        for(Vector3 point : sphere)
        {
            Block block = world.getBlockAt((int)point.x, (int)point.y, (int)point.z);
            if(block.getType() == Material.WOODEN_DOOR)
            {
                this.setOpen(block, open);
            }
        }
    }

    /**
     * sets the block either open or closed.
     *
     * @param block - the block
     * @param open  - true to set open, false to set closed
     * @return returns whether the block could set or not
     */
    public boolean setOpen(Block block, boolean open)
    {
        Material type = block.getType();
        BlockState state = block.getState();

        if(!this.isOpenable(state))
        {
            return false;
        }

        byte rawData = state.getRawData();

        if((type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK) && (rawData & 0x8) == 0x8)
        {
            return false;
        }

        if(open)
        {
            state.setRawData((byte) (rawData | 0x4));   // open door
        }
        else
        {
            state.setRawData((byte) (rawData & 0xB));    //close door
        }
        state.update();
        return true;
    }

    /**
     * This method checks whether a Block is openable.
     *
     * @param state - The state of the block.
     * @return whether the meterialdate of the block implements openable or not.
     */
    public boolean isOpenable(BlockState state)
    {
        Class<?>[] interfaces = state.getData().getClass().getInterfaces();
        for(int i = 0; i < interfaces.length; i++)
        {
            if(interfaces[i] == Openable.class)
            {
                return true;
            }
        }
        return false;
    }
}
