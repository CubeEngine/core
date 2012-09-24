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
        
        String entityName;
        String entityData = null;
        String ridingEntityName = null;
        String ridingEntityData = null;
        String mobString = context.getString(0);
        if (mobString.contains(","))
        {
            entityName = mobString.substring(0, mobString.indexOf(","));
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
            if (entityType == null)
            {
                return; //TODO msg invalid mob
            }
        }
        if (ridingEntityName != null && ridingEntityName.contains(":"))
        {
            ridingEntityName = ridingEntityName.substring(0, ridingEntityName.indexOf(":"));
            ridingEntityData = ridingEntityName.substring(ridingEntityName.indexOf(":"), ridingEntityName.length());
            ridingEntityType = EntityMatcher.get().matchMob(ridingEntityName);
            if (ridingEntityType == null)
            {
                return; //TODO msg invalid ridingmob
            }
        }
        Location loc;
        if (context.hasIndexed(2))
        {
            User user = context.getUser(2);
            if (user == null)
            {
                return;//TODO msg user not found
            }
            loc = user.getLocation();
        }
        else
        {
            loc = sender.getTargetBlock(null, 200).getLocation(); // TODO do Util method for this in core 
        }
        int amount = 1;
        if (context.hasIndexed(1))
        {
            amount = context.getIndexed(1, int.class, 0);
            if (amount == 0)
            {
                return; //TODO msg invalid amount
            }
        }

        for (int i = 1; i <= amount; ++i)
        { //TODO msg succes spawn
            Entity entity = loc.getWorld().spawnEntity(loc, entityType.getBukkitType());
            this.applyDataToMob(entityType, entity, entityData);
            if (ridingEntityType != null)
            {
                Entity ridingentity = loc.getWorld().spawnEntity(loc, entityType.getBukkitType());
                this.applyDataToMob(ridingEntityType, ridingentity, ridingEntityData);
                entity.setPassenger(ridingentity);
            }
        }
    }
    
    private void applyDataToMob(EntityType entityType, Entity entity, String data)
    {
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
            else if (data.equalsIgnoreCase("powered") || data.equalsIgnoreCase("power"))
            {
                if (entityType.equals(EntityType.CREEPER))
                {
                    ((Creeper) entity).setPowered(true);
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
}