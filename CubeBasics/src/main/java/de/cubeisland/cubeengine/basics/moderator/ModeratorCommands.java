package de.cubeisland.cubeengine.basics.moderator;

import de.cubeisland.cubeengine.basics.Basics;
import static de.cubeisland.cubeengine.core.CubeEngine._;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.EntityMatcher;
import de.cubeisland.cubeengine.core.util.EntityType;
import de.cubeisland.cubeengine.core.util.MaterialMatcher;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;

/**
 *
 * @author Anselm Brehme
 */
public class ModeratorCommands
{
    UserManager cuManager;

    public ModeratorCommands(Basics module)
    {
        cuManager = module.getUserManager();
    }

    @Command(
    desc = "Spawns the specified Mob",
    min = 1,
    max = 3)
    public void spawnMob(CommandContext context)
    {//TODO ridingmob
        // /spawnmob <mob>[:data][,<ridingmob>[:data]] [amount] [player]
        User sender = cuManager.getUser(context.getSender());

        if (sender == null)
        {
            context.getSender().sendMessage(_("core","&cThis command can only be used by a player!"));
            return;
        }
        EntityType entityType = null;
        EntityType ridingEntityType = null;
        try
        {
            String entityName = null;
            String entityData = null;
            String ridingEntityName = null;
            String ridingEntityData = null;
            String mobString = context.getString(0);
            if (mobString.contains(","))
            {
                entityName = mobString.substring(0,mobString.indexOf(","));
                ridingEntityName = mobString.substring(mobString.indexOf(","), mobString.length());
            }
            else
            {
                entityName = mobString;
            }
            if (entityName.contains(":"))
            {
                entityName = entityName.substring(0, entityName.indexOf(":"));
                entityData = entityName.substring(entityName.indexOf(":"), entityName.length());
                entityType = EntityMatcher.get().matchMob(entityName);
            }
            if (ridingEntityName != null && ridingEntityName.contains(":"))
            {
                ridingEntityName = ridingEntityName.substring(0, ridingEntityName.indexOf(":"));
                ridingEntityData = ridingEntityName.substring(ridingEntityName.indexOf(":"), ridingEntityName.length());
                ridingEntityType = EntityMatcher.get().matchMob(ridingEntityName);
            }
        }
        catch (ConversionException ex)
        {
            //TODO handle me
            return;
        }
        String data = null;//TODO set data
        Location loc = sender.getTargetBlock(null, 200).getLocation(); // TODO check does this work?
        Entity entity = sender.getWorld().spawnEntity(loc, entityType.getBukkitType());
        //TODO apply data to entity
        
        if (data != null)
        {
            if (data.equalsIgnoreCase("baby"))
            {
                if (entityType.isAnimal())
                {
                    ((Animals) entity).setBaby();
                }
                else
                {
                    //could not apply data
                }
            }
            else if (data.equalsIgnoreCase("angry"))
            {
                if (entityType.equals(EntityType.WOLF))
                {
                    ((Wolf) entity).setAngry(true);
                }
                else if (entityType.equals(EntityType.PIG_ZOMBIE))
                {
                    ((PigZombie) entity).setAngry(true);
                }
            }
            else if (data.equalsIgnoreCase("tamed"))
            {
                if (entityType.equals(EntityType.WOLF))
                {
                    ((Wolf) entity).setTamed(true);
                }
                else if (entityType.equals(EntityType.OCELOT))
                {
                    ((Ocelot) entity).setTamed(true);
                }
            }
            else if (data.equalsIgnoreCase("powered")||data.equalsIgnoreCase("power"))
            {
                if (entityType.equals(EntityType.CREEPER))
                {
                    ((Creeper)entity).setPowered(true);
                }
            }
            else
            {
                if (entityType.equals(EntityType.SHEEP))
                {
                    DyeColor color = MaterialMatcher.get().matchColorData(data);
                    if (color == null)
                    {
                        //TODO msg color not found
                        return;
                    }
                    ((Sheep) entity).setColor(color);
                }
                else if (entityType.equals(EntityType.SLIME) || entityType.equals(EntityType.MAGMA_CUBE))
                {
                    int size;
                    try
                    {
                        size = Integer.parseInt(data);
                    }
                    catch (NumberFormatException e)
                    {
                        //TODO msg invalid size
                        return;
                    }
                    if (size > 0 && size <= 250)
                    {
                        ((Slime) entity).setSize(size);
                    }
                }
            }
        }
    }
    
    private void applyDataToMob(EntityType type, Entity entity, String data)
    {
        
    }
}