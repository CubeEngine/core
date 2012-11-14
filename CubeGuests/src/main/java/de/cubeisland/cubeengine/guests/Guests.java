package de.cubeisland.cubeengine.guests;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.guests.prevention.PreventionManager;
import de.cubeisland.cubeengine.guests.prevention.preventions.*;
import de.cubeisland.cubeengine.guests.prevention.punishments.*;
import java.io.File;

public class Guests extends Module
{
    private File dataFolder;
    private File preventionConfigFolder;
    private PreventionManager prevManager;
    private GuestsConfig config;

    @Override
    public void onEnable()
    {
        this.dataFolder = this.getFolder();
        this.preventionConfigFolder = new File(this.dataFolder, "preventions");
        
        this.registerCommand(new Commands(this));


        this.prevManager = new PreventionManager(this)
            .registerPunishment(new BanPunishment())
            .registerPunishment(new BurnPunishment())
            .registerPunishment(new DropitemPunishment())
            .registerPunishment(new ExplosionPunishment())
            .registerPunishment(new KickPunishment())
            .registerPunishment(new KillPunishment())
            .registerPunishment(new LightningPunishment())
            .registerPunishment(new MessagePunishment())
            .registerPunishment(new PotionPunishment())
            .registerPunishment(new RocketPunishment())
            .registerPunishment(new SlapPunishment())
            .registerPunishment(new StarvationPunishment())
            
            .registerPrevention(new AfkPrevention(this))
            .registerPrevention(new BedPrevention(this))
            .registerPrevention(new BowPrevention(this))
            .registerPrevention(new BreakblockPrevention(this))
            .registerPrevention(new BrewPrevention(this))
            .registerPrevention(new ButtonPrevention(this))
            .registerPrevention(new CakePrevention(this))
            .registerPrevention(new CapsPrevention(this))
            .registerPrevention(new ChangesignPrevention(this))
            .registerPrevention(new ChatPrevention(this))
            .registerPrevention(new ChestPrevention(this))
            .registerPrevention(new CommandPrevention(this))
            .registerPrevention(new DamagePrevention(this))
            .registerPrevention(new DispenserPrevention(this))
            .registerPrevention(new DoorPrevention(this))
            .registerPrevention(new DropPrevention(this))
            .registerPrevention(new EnchantPrevention(this))
            .registerPrevention(new FightPrevention(this))
            .registerPrevention(new FishPrevention(this))
            .registerPrevention(new FurnacePrevention(this))
            .registerPrevention(new GuestlimitPrevention(this))
            .registerPrevention(new HungerPrevention(this))
            .registerPrevention(new ItemPrevention(this))
            .registerPrevention(new JukeboxPrevention(this))
            .registerPrevention(new LavabucketPrevention(this))
            .registerPrevention(new LeverPrevention(this))
            .registerPrevention(new MilkingPrevention(this))
            .registerPrevention(new MonsterPrevention(this))
            .registerPrevention(new MovePrevention(this))
            .registerPrevention(new NoteblockPrevention(this))
            .registerPrevention(new PickupPrevention(this))
            .registerPrevention(new PlaceblockPrevention(this))
            .registerPrevention(new PressureplatePrevention(this))
            .registerPrevention(new RepeaterPrevention(this))
            .registerPrevention(new ShearPrevention(this))
            .registerPrevention(new SneakPrevention(this))
            .registerPrevention(new SpamPrevention(this))
            .registerPrevention(new SpawneggPrevention(this))
            .registerPrevention(new SwearPrevention(this))
            .registerPrevention(new TamePrevention(this))
            .registerPrevention(new TradingPrevention(this))
            .registerPrevention(new TramplePrevention(this))
            .registerPrevention(new VehiclePrevention(this))
            .registerPrevention(new WaterbucketPrevention(this))
            .registerPrevention(new WorkbenchPrevention(this))
            .enablePreventions();

        this.getLogger().log(LogLevel.NOTICE, this.prevManager.getPreventions().size() + " Prevention(s) have been registered!");
    }

    @Override
    public void onDisable()
    {
        this.prevManager.disablePreventions();
        this.prevManager = null;
    }
    
    public PreventionManager getPreventionManager()
    {
        return this.prevManager;
    }
    
    
    public boolean allowPunishments()
    {
        return this.config.punishments;
    }

    public File getPreventionsFolder()
    {
        return this.preventionConfigFolder;
    }
}
