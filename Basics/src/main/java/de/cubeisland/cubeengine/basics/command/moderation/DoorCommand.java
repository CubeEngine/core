package de.cubeisland.cubeengine.basics.command.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.util.Vector;

public class DoorCommand
{
    private Basics basics;
    private final EnumSet<Material> doorMaterials = EnumSet.of( Material.WOODEN_DOOR, Material.IRON_DOOR, Material.IRON_DOOR_BLOCK, Material.TRAP_DOOR, Material.FENCE_GATE );

    public DoorCommand( Basics basics )
    {
        this.basics = basics;
    }

    @Command
    (
            desc = "",
            usage = "<open|close> <radius>",
            min = 2,
            max = 2 
    )
    public void doors( CommandContext context )
    {
        //TODO flags and permissions for all types of doors
        if( !(context.getSender() instanceof User) )
        {
            context.sendMessage( "basics", "&cThis command can only be used by a player!" );
            return;
        }
        User user = ( User ) context.getSender();
        boolean open = context.getArg( 0, String.class).equalsIgnoreCase( "open" );
        int radius = context.getArg( 1, Integer.class, 10 );

        // TODO: find all blocks in the radius. Does not work accurately
        Vector userVector = user.getLocation().toVector();
        Set<BlockVector3> blockVectors = new HashSet<BlockVector3>();

        for( int circle = radius; circle > 0; circle-- )
        {
            double stepSizeLatitudes = 180.0 / (circle * 10);
            double stepSizeLongitudes = 360.0 / (circle * 10);

            for( int width = 0; width < circle * 10; width++ )
            {
                double widthAngle = (90.0 - (width + 1) * 180.0 / stepSizeLatitudes) * Math.PI / 180.0;

                for( int length = 0; length < circle * 10; length++ )
                {
                    double lengthAngle = length * 360 / stepSizeLongitudes * Math.PI / 180.0;

                    blockVectors.add( new BlockVector3(
                            ( int ) (Math.cos( lengthAngle ) * Math.cos( widthAngle ) * circle + userVector.getX()),
                            ( int ) (Math.sin( widthAngle ) * circle + userVector.getY()),
                            ( int ) (Math.sin( lengthAngle ) * Math.cos( widthAngle ) * circle + userVector.getZ()) ) );
                }
            }
        }

        for( BlockVector3 vector : blockVectors )
        {
            Block block = user.getWorld().getBlockAt( vector.x, vector.y, vector.z );
            if( this.doorMaterials.contains( block.getType() ))
            {
                this.setOpen( block, open );
            }
        }
        
        if(open)
        {
            context.sendMessage( "basics", "&aAll doors are open now" );
        }
        else
        {
            context.sendMessage( "basics", "&aAll doors are closed now" );
        }
    }

    public boolean setOpen( Block block, boolean open )
    {
        Material type = block.getType();

        if( !this.doorMaterials.contains( type ) )
        {
            return false;
        }

        BlockState state = block.getState();
        byte rawData = state.getRawData();

        if( (type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK) && (rawData & 8) == 8 )
        {
            return false;
        }

        if( open )
        {
            state.setRawData( ( byte ) (rawData | 4) ); // open door
        }
        else
        {
            state.setRawData( ( byte ) (rawData & 3) );    //close door
        }
        state.update();
        return true;
    }
}
