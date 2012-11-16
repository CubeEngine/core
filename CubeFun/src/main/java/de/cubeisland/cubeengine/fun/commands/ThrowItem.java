package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;

public class ThrowItem implements Runnable
{
    Class material;
    String name;
    UserManager userManager;

    public ThrowItem(UserManager userManager, String name, Class materialClass)
    {
        this.userManager = userManager;
        this.name = name;
        this.material = materialClass;

        Validate.notNull(name, "The name must not be null");
        Validate.notNull(material, "The material must not be null");
    }

    @Override
    public void run()
    {
        User user = userManager.getUser(name, true);
        if (material == Fireball.class)
        {
            Fireball fireball = (Fireball)user.getWorld().spawnEntity(user.getLocation().add(user.getLocation().getDirection()), EntityType.FIREBALL);
            fireball.setShooter(user);
            fireball.setVelocity(user.getLocation().getDirection());
        }
        else
        {
            user.launchProjectile(material);
        }
    }
}
