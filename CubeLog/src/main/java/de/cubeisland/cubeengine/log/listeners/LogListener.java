package de.cubeisland.cubeengine.log.listeners;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogConfiguration;
import de.cubeisland.cubeengine.log.LogManager;
import de.cubeisland.cubeengine.log.LogSubConfiguration;
import org.bukkit.event.Listener;

public abstract class LogListener implements Listener
{
    protected static LogManager lm;
    protected LogConfiguration config;
    protected LogSubConfiguration subConfig;

    public static void initLogManager(LogManager logManager)
    {
        lm = logManager;
    }
    
    public LogListener(Log module, LogSubConfiguration subConfig)
    {
        this.config = module.getConfiguration();
        this.subConfig = subConfig;
    }

    public <T extends LogSubConfiguration> T getConfiguration()
    {
        return (T)subConfig;
    }

    public static <T extends LogListener> T getInstance(Class<T> clazz, Log module)
    {
        try
        {
            T t= clazz.getConstructor(Log.class).newInstance(module);
            t.getConfiguration().listener = t;
            return t;
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Could not create new instance of LogListener!");
        }
    }
//BlockFormEvent & BlockFadeEvent for SnowCover and Ice
    //
//BlockBurnEvent for destroyed by fire
//LeavesDecayEvent for natural leaves decay
//BlockIgniteEvent for setting fire with fireball flint_and_steel || ?? lava , lightning , spread
//SignChangeEvent for when sign is changed //TODO find when this is fired perhaps after finished editing the sign
//BlockPistonEvent or BlockPistonExtendEvent / BlockPistonRetractEvent piston movements ??   
//BlockFromToEvent ?? water lava dragoneggs
//BlockGrowEvent ?? for wheat sugarcane cactus watermelon pumpkin
//BlockPhysicsEvent ?? for when phyics is checked
//BlockRedstoneEvent ?? when redstone update
//BrewEvent ??
//FurnaceBurnEvent ?? when fuel burned
//FurnaceSmeltEvent ?? when block smelted
//NotePlayEvent ?? noteblock by player or redstone
}
