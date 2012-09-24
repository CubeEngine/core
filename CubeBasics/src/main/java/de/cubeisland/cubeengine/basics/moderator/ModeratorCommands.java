package de.cubeisland.cubeengine.basics.moderator;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.EntityMatcher;
import de.cubeisland.cubeengine.core.util.EntityType;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import javax.swing.plaf.metal.OceanTheme;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.PigZombie;
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
    {
        // /spawnmob <mob>[:data][,<mob>[:data]] [amount] [player]
        User user = cuManager.getUser(context.getSender());

        if (user == null)
        {
            // TODO msg no player
            return;
        }
        de.cubeisland.cubeengine.core.util.EntityType entityType;
        try
        {
            entityType = EntityMatcher.get().matchMob(context.getString(1));
            // TODO dataMatcher for entities and substring if conatins :
        }
        catch (ConversionException ex)
        {
            //TODO handle me
            return;
        }
        String data = null;//TODO set data
        Location loc = user.getTargetBlock(null, 200).getLocation(); // TODO check does this work?
        Entity entity = user.getWorld().spawnEntity(loc, entityType.getBukkitType());
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

        }
    }
}