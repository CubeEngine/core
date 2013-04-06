package de.cubeisland.cubeengine.log.action;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.CraftItem;
import de.cubeisland.cubeengine.log.action.logaction.EnchantItem;
import de.cubeisland.cubeengine.log.action.logaction.ItemDrop;
import de.cubeisland.cubeengine.log.action.logaction.ItemPickup;
import de.cubeisland.cubeengine.log.action.logaction.PlayerChat;
import de.cubeisland.cubeengine.log.action.logaction.PlayerCommand;
import de.cubeisland.cubeengine.log.action.logaction.PlayerJoin;
import de.cubeisland.cubeengine.log.action.logaction.PlayerQuit;
import de.cubeisland.cubeengine.log.action.logaction.PlayerTeleport;
import de.cubeisland.cubeengine.log.action.logaction.XpPickup;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockBurn;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockFade;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockFall;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockForm;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockShift;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockSpread;
import de.cubeisland.cubeengine.log.action.logaction.block.LeafDecay;
import de.cubeisland.cubeengine.log.action.logaction.block.NaturalGrow;
import de.cubeisland.cubeengine.log.action.logaction.block.entity.EndermanPickup;
import de.cubeisland.cubeengine.log.action.logaction.block.entity.EndermanPlace;
import de.cubeisland.cubeengine.log.action.logaction.block.entity.EntityBreak;
import de.cubeisland.cubeengine.log.action.logaction.block.entity.EntityChangeActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.entity.EntityForm;
import de.cubeisland.cubeengine.log.action.logaction.block.entity.SheepEat;
import de.cubeisland.cubeengine.log.action.logaction.block.explosion.CreeperExplode;
import de.cubeisland.cubeengine.log.action.logaction.block.explosion.EnderdragonExplode;
import de.cubeisland.cubeengine.log.action.logaction.block.explosion.EntityExplode;
import de.cubeisland.cubeengine.log.action.logaction.block.explosion.ExplodeActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.explosion.FireballExplode;
import de.cubeisland.cubeengine.log.action.logaction.block.explosion.TntExplode;
import de.cubeisland.cubeengine.log.action.logaction.block.explosion.WitherExplode;
import de.cubeisland.cubeengine.log.action.logaction.block.flow.FlowActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.flow.LavaBreak;
import de.cubeisland.cubeengine.log.action.logaction.block.flow.LavaFlow;
import de.cubeisland.cubeengine.log.action.logaction.block.flow.WaterBreak;
import de.cubeisland.cubeengine.log.action.logaction.block.flow.WaterFlow;
import de.cubeisland.cubeengine.log.action.logaction.block.ignite.FireSpread;
import de.cubeisland.cubeengine.log.action.logaction.block.ignite.FireballIgnite;
import de.cubeisland.cubeengine.log.action.logaction.block.ignite.IgniteActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.ignite.LavaIgnite;
import de.cubeisland.cubeengine.log.action.logaction.block.ignite.Lighter;
import de.cubeisland.cubeengine.log.action.logaction.block.ignite.LightningIgnite;
import de.cubeisland.cubeengine.log.action.logaction.block.ignite.OtherIgnite;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.BonemealUse;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.ButtonUse;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.CakeEat;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.ComparatorChange;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.ContainerAccess;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.CropTrample;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.DoorUse;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.LeverUse;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.NoteBlockChange;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.PlateStep;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.RepeaterChange;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.RightClickActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.interaction.TntPrime;
import de.cubeisland.cubeengine.log.action.logaction.block.player.BlockBreak;
import de.cubeisland.cubeengine.log.action.logaction.block.player.BlockPlace;
import de.cubeisland.cubeengine.log.action.logaction.block.player.BucketEmpty;
import de.cubeisland.cubeengine.log.action.logaction.block.player.BucketFill;
import de.cubeisland.cubeengine.log.action.logaction.block.player.HangingBreak;
import de.cubeisland.cubeengine.log.action.logaction.block.player.HangingPlace;
import de.cubeisland.cubeengine.log.action.logaction.block.player.LavaBucket;
import de.cubeisland.cubeengine.log.action.logaction.block.player.PlayerGrow;
import de.cubeisland.cubeengine.log.action.logaction.block.player.SignChange;
import de.cubeisland.cubeengine.log.action.logaction.block.player.WaterBucket;
import de.cubeisland.cubeengine.log.action.logaction.container.ContainerActionType;
import de.cubeisland.cubeengine.log.action.logaction.container.ItemInsert;
import de.cubeisland.cubeengine.log.action.logaction.container.ItemRemove;
import de.cubeisland.cubeengine.log.action.logaction.container.ItemTransfer;
import de.cubeisland.cubeengine.log.action.logaction.interact.EntityDye;
import de.cubeisland.cubeengine.log.action.logaction.interact.EntityShear;
import de.cubeisland.cubeengine.log.action.logaction.interact.FireworkUse;
import de.cubeisland.cubeengine.log.action.logaction.interact.InteractEntityActionType;
import de.cubeisland.cubeengine.log.action.logaction.interact.MilkFill;
import de.cubeisland.cubeengine.log.action.logaction.interact.MonsterEggUse;
import de.cubeisland.cubeengine.log.action.logaction.interact.PotionSplash;
import de.cubeisland.cubeengine.log.action.logaction.interact.SoupFill;
import de.cubeisland.cubeengine.log.action.logaction.interact.VehicleBreak;
import de.cubeisland.cubeengine.log.action.logaction.interact.VehicleEnter;
import de.cubeisland.cubeengine.log.action.logaction.interact.VehicleExit;
import de.cubeisland.cubeengine.log.action.logaction.interact.VehiclePlace;
import de.cubeisland.cubeengine.log.action.logaction.kill.AnimalDeath;
import de.cubeisland.cubeengine.log.action.logaction.kill.BossDeath;
import de.cubeisland.cubeengine.log.action.logaction.kill.KillActionType;
import de.cubeisland.cubeengine.log.action.logaction.kill.MonsterDeath;
import de.cubeisland.cubeengine.log.action.logaction.kill.NpcDeath;
import de.cubeisland.cubeengine.log.action.logaction.kill.OtherDeath;
import de.cubeisland.cubeengine.log.action.logaction.kill.PetDeath;
import de.cubeisland.cubeengine.log.action.logaction.kill.PlayerDeath;
import de.cubeisland.cubeengine.log.action.logaction.spawn.EntitySpawnActionType;
import de.cubeisland.cubeengine.log.action.logaction.spawn.NaturalSpawn;
import de.cubeisland.cubeengine.log.action.logaction.spawn.OtherSpawn;
import de.cubeisland.cubeengine.log.action.logaction.spawn.SpawnerSpawn;
import de.cubeisland.cubeengine.log.action.logaction.worldedit.WorldEditActionType;

import gnu.trove.map.hash.TLongObjectHashMap;

public class ActionTypeManager
{
    private Map<Class<? extends ActionType>,ActionType> registeredActionTypes = new ConcurrentHashMap<Class<? extends ActionType>, ActionType>();
    private TLongObjectHashMap<ActionType> registeredIds = new TLongObjectHashMap<ActionType>();
    private final Log module;

    private Map<String,Long> actionIDs;

    public ActionTypeManager(Log module)
    {
        this.module = module;
        this.actionIDs = this.module.getLogManager().getQueryManager().getActionTypesFromDatabase();
    }

    public void registerLogActionTypes()
    {
        this.registerActionType(new BlockBreak(module))
            .registerActionType(new BlockBurn(module))
            .registerActionType(new BlockFade(module))
            .registerActionType(new LeafDecay(module))
            .registerActionType(new WaterBreak(module))
            .registerActionType(new LavaBreak(module))
            .registerActionType(new EntityBreak(module))
            .registerActionType(new EndermanPickup(module))
            .registerActionType(new BucketFill(module))
            .registerActionType(new CropTrample(module))
            .registerActionType(new EntityExplode(module))
            .registerActionType(new CreeperExplode(module))
            .registerActionType(new TntExplode(module))
            .registerActionType(new FireballExplode(module))
            .registerActionType(new EnderdragonExplode(module))
            .registerActionType(new WitherExplode(module))
            .registerActionType(new TntPrime(module))
            .registerActionType(new BlockPlace(module))
            .registerActionType(new LavaBucket(module))
            .registerActionType(new WaterBucket(module))
            .registerActionType(new NaturalGrow(module))
            .registerActionType(new PlayerGrow(module))
            .registerActionType(new BlockForm(module))
            .registerActionType(new EndermanPlace(module))
            .registerActionType(new EntityForm(module))
            .registerActionType(new FireSpread(module))
            .registerActionType(new FireballIgnite(module))
            .registerActionType(new Lighter(module))
            .registerActionType(new LavaIgnite(module))
            .registerActionType(new LightningIgnite(module))
            .registerActionType(new BlockSpread(module))
            .registerActionType(new WaterFlow(module))
            .registerActionType(new LavaFlow(module))
            .registerActionType(new OtherIgnite(module))
            .registerActionType(new BlockShift(module))
            .registerActionType(new BlockFall(module))
            .registerActionType(new SignChange(module))
            .registerActionType(new SheepEat(module))
            .registerActionType(new BonemealUse(module))
            .registerActionType(new LeverUse(module))
            .registerActionType(new RepeaterChange(module))
            .registerActionType(new NoteBlockChange(module))
            .registerActionType(new DoorUse(module))
            .registerActionType(new CakeEat(module))
            .registerActionType(new ComparatorChange(module))
            .registerActionType(new WorldEditActionType(module))
            .registerActionType(new ContainerAccess(module))
            .registerActionType(new ButtonUse(module))
            .registerActionType(new FireworkUse(module))
            .registerActionType(new VehicleEnter(module))
            .registerActionType(new VehicleExit(module))
            .registerActionType(new PotionSplash(module))
            .registerActionType(new PlateStep(module))
            .registerActionType(new MilkFill(module))
            .registerActionType(new SoupFill(module))
            .registerActionType(new VehiclePlace(module))
            .registerActionType(new HangingPlace(module))
            .registerActionType(new VehicleBreak(module))
            .registerActionType(new HangingBreak(module))
            .registerActionType(new PlayerDeath(module))
            .registerActionType(new MonsterDeath(module))
            .registerActionType(new AnimalDeath(module))
            .registerActionType(new PetDeath(module))
            .registerActionType(new NpcDeath(module))
            .registerActionType(new BossDeath(module))
            .registerActionType(new OtherDeath(module))
            .registerActionType(new MonsterEggUse(module))
            .registerActionType(new NaturalSpawn(module))
            .registerActionType(new SpawnerSpawn(module))
            .registerActionType(new OtherSpawn(module))
            .registerActionType(new ItemDrop(module))
            .registerActionType(new ItemPickup(module))
            .registerActionType(new XpPickup(module))
            .registerActionType(new EntityShear(module))
            .registerActionType(new EntityDye(module))
            .registerActionType(new ItemInsert(module))
            .registerActionType(new ItemRemove(module))
            .registerActionType(new ItemTransfer(module))
            .registerActionType(new PlayerCommand(module))
            .registerActionType(new PlayerChat(module))
            .registerActionType(new PlayerJoin(module))
            .registerActionType(new PlayerQuit(module))
            .registerActionType(new PlayerTeleport(module))
            .registerActionType(new EnchantItem(module))
            .registerActionType(new CraftItem(module));
        this.registerActionType(new EntityChangeActionType(module))
            .registerActionType(new ExplodeActionType(module))
            .registerActionType(new FlowActionType(module))
            .registerActionType(new IgniteActionType(module))
            .registerActionType(new RightClickActionType(module))
            .registerActionType(new BucketEmpty(module))
            .registerActionType(new ContainerActionType(module))
            .registerActionType(new InteractEntityActionType(module))
            .registerActionType(new KillActionType(module))
            .registerActionType(new EntitySpawnActionType(module));
    }

    public ActionTypeManager registerActionType(ActionType actionType)
    {
        if (actionType.getID() != -1)
        {
            Long actionTypeId = this.actionIDs.get(actionType.name);
            if (actionTypeId == null)
            {
                actionTypeId = this.module.getLogManager().getQueryManager().registerActionType(actionType.name);
                this.actionIDs.put(actionType.name,actionTypeId);
            }
            actionType.setID(actionTypeId);
        }
        registeredIds.put(actionType.getID(),actionType);
        registeredActionTypes.put(actionType.getClass(),actionType);
        actionType.initialize();
        this.module.getLog().log(LogLevel.DEBUG,"ActionType registered: " + actionType.getID() + " " + actionType.name);
        return this;
    }

    public <AT extends ActionType> AT getActionType(Class<AT> actionTypeClass)
    {
        return (AT)this.registeredActionTypes.get(actionTypeClass);
    }

    public ActionType getActionType(int id)
    {
        return this.registeredIds.get(id);
    }
}
