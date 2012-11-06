package de.cubeisland.cubeengine.log.logger;

import de.cubeisland.cubeengine.log.Log;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;

public abstract class Logger<T extends SubLogConfig> implements Listener
{
    public static final Log module = Log.getInstance();
    protected T config;
    public final LogAction action;

    public Logger(LogAction action)
    {
        this.action = action;
    }

    public T getConfig()
    {
        return this.config;
    }

    public void applyConfig(SubLogConfig config)
    {
        this.config = (T)config;
        this.applyConfig();
    }

    public void applyConfig()
    {
        if (this.config.enabled)
        {
            module.registerListener(this);
        }
        else
        {
            module.unregisterListener(this);
        }
    }
    
    
     public static enum BlockChangeCause
    {
        PLAYER(-1),
        LAVA(-2),
        WATER(-3),
        EXPLOSION(-4),
        FIRE(-5),
        ENDERMAN(-6),
        FADE(-7), FORM(-7),
        DECAY(-8), GROW(-8),
        WITHER(-9);

        private BlockChangeCause(int causeID)
        {
            this.causeID = causeID;
        }
        final private int causeID;

        public int getId()
        {
            return causeID;
        }
    }

    

    
}