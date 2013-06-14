/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.guests;

import java.io.File;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.guests.prevention.PreventionManager;
import de.cubeisland.cubeengine.guests.prevention.preventions.AfkPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.BedPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.BowPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.BreakblockPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.BrewPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.ButtonPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.CakePrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.CapsPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.ChangesignPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.ChatPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.ChestPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.CommandPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.DamagePrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.DispenserPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.DoorPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.DropPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.EnchantPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.FightPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.FishPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.FurnacePrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.GuestlimitPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.HungerPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.ItemPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.JukeboxPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.LavabucketPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.LeverPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.MilkingPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.MonsterPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.MovePrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.NoteblockPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.PickupPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.PlaceblockPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.PressureplatePrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.RepeaterPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.ShearPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.SneakPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.SpamPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.SpawneggPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.SwearPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.TamePrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.TradingPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.TramplePrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.VehiclePrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.WaterbucketPrevention;
import de.cubeisland.cubeengine.guests.prevention.preventions.WorkbenchPrevention;
import de.cubeisland.cubeengine.guests.prevention.punishments.BanPunishment;
import de.cubeisland.cubeengine.guests.prevention.punishments.BurnPunishment;
import de.cubeisland.cubeengine.guests.prevention.punishments.DropitemPunishment;
import de.cubeisland.cubeengine.guests.prevention.punishments.ExplosionPunishment;
import de.cubeisland.cubeengine.guests.prevention.punishments.KickPunishment;
import de.cubeisland.cubeengine.guests.prevention.punishments.KillPunishment;
import de.cubeisland.cubeengine.guests.prevention.punishments.LightningPunishment;
import de.cubeisland.cubeengine.guests.prevention.punishments.MessagePunishment;
import de.cubeisland.cubeengine.guests.prevention.punishments.PotionPunishment;
import de.cubeisland.cubeengine.guests.prevention.punishments.RocketPunishment;
import de.cubeisland.cubeengine.guests.prevention.punishments.SlapPunishment;
import de.cubeisland.cubeengine.guests.prevention.punishments.StarvationPunishment;

public class Guests extends Module
{
    private File dataFolder;
    private File preventionConfigFolder;
    private PreventionManager prevManager;
    private GuestsConfig config;

    @Override
    public void onEnable()
    {
        this.config = Configuration.load(GuestsConfig.class, this);
        this.dataFolder = this.getFolder();
        this.preventionConfigFolder = new File(this.dataFolder, "preventions");

        this.getCore().getCommandManager().registerCommand(new Commands(this));

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

        this.getLog().warn(this.prevManager.getPreventions().size() + " Prevention(s) have been registered!");
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
