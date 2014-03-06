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
package de.cubeisland.engine.log.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;


import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.logaction.CraftItem;
import de.cubeisland.engine.log.action.logaction.EnchantItem;
import de.cubeisland.engine.log.action.logaction.ItemDrop;
import de.cubeisland.engine.log.action.logaction.ItemPickup;
import de.cubeisland.engine.log.action.logaction.PlayerChat;
import de.cubeisland.engine.log.action.logaction.PlayerCommand;
import de.cubeisland.engine.log.action.logaction.PlayerJoin;
import de.cubeisland.engine.log.action.logaction.PlayerQuit;
import de.cubeisland.engine.log.action.logaction.PlayerTeleport;
import de.cubeisland.engine.log.action.logaction.XpPickup;
import de.cubeisland.engine.log.action.logaction.block.BlockBurn;
import de.cubeisland.engine.log.action.logaction.block.BlockFade;
import de.cubeisland.engine.log.action.logaction.block.BlockFall;
import de.cubeisland.engine.log.action.logaction.block.BlockForm;
import de.cubeisland.engine.log.action.logaction.block.BlockShift;
import de.cubeisland.engine.log.action.logaction.block.BlockSpread;
import de.cubeisland.engine.log.action.logaction.block.LeafDecay;
import de.cubeisland.engine.log.action.logaction.block.NaturalGrow;
import de.cubeisland.engine.log.action.logaction.block.entity.EndermanPickup;
import de.cubeisland.engine.log.action.logaction.block.entity.EndermanPlace;
import de.cubeisland.engine.log.action.logaction.block.entity.EntityBreak;
import de.cubeisland.engine.log.action.logaction.block.entity.EntityChangeActionType;
import de.cubeisland.engine.log.action.logaction.block.entity.EntityForm;
import de.cubeisland.engine.log.action.logaction.block.entity.SheepEat;
import de.cubeisland.engine.log.action.logaction.block.explosion.CreeperExplode;
import de.cubeisland.engine.log.action.logaction.block.explosion.EnderdragonExplode;
import de.cubeisland.engine.log.action.logaction.block.explosion.EntityExplode;
import de.cubeisland.engine.log.action.logaction.block.explosion.ExplodeActionType;
import de.cubeisland.engine.log.action.logaction.block.explosion.FireballExplode;
import de.cubeisland.engine.log.action.logaction.block.explosion.TntExplode;
import de.cubeisland.engine.log.action.logaction.block.explosion.WitherExplode;
import de.cubeisland.engine.log.action.logaction.block.flow.FlowActionType;
import de.cubeisland.engine.log.action.logaction.block.flow.LavaBreak;
import de.cubeisland.engine.log.action.logaction.block.flow.LavaFlow;
import de.cubeisland.engine.log.action.logaction.block.flow.WaterBreak;
import de.cubeisland.engine.log.action.logaction.block.flow.WaterFlow;
import de.cubeisland.engine.log.action.logaction.block.ignite.FireSpread;
import de.cubeisland.engine.log.action.logaction.block.ignite.FireballIgnite;
import de.cubeisland.engine.log.action.logaction.block.ignite.IgniteActionType;
import de.cubeisland.engine.log.action.logaction.block.ignite.LavaIgnite;
import de.cubeisland.engine.log.action.logaction.block.ignite.Lighter;
import de.cubeisland.engine.log.action.logaction.block.ignite.LightningIgnite;
import de.cubeisland.engine.log.action.logaction.block.ignite.OtherIgnite;
import de.cubeisland.engine.log.action.logaction.block.interaction.BonemealUse;
import de.cubeisland.engine.log.action.logaction.block.interaction.ButtonUse;
import de.cubeisland.engine.log.action.logaction.block.interaction.CakeEat;
import de.cubeisland.engine.log.action.logaction.block.interaction.ComparatorChange;
import de.cubeisland.engine.log.action.logaction.block.interaction.ContainerAccess;
import de.cubeisland.engine.log.action.logaction.block.interaction.CropTrample;
import de.cubeisland.engine.log.action.logaction.block.interaction.DoorUse;
import de.cubeisland.engine.log.action.logaction.block.interaction.LeverUse;
import de.cubeisland.engine.log.action.logaction.block.interaction.NoteBlockChange;
import de.cubeisland.engine.log.action.logaction.block.interaction.PlateStep;
import de.cubeisland.engine.log.action.logaction.block.interaction.RepeaterChange;
import de.cubeisland.engine.log.action.logaction.block.interaction.RightClickActionType;
import de.cubeisland.engine.log.action.logaction.block.interaction.TntPrime;
import de.cubeisland.engine.log.action.logaction.block.player.BlockBreak;
import de.cubeisland.engine.log.action.logaction.block.player.BlockPlace;
import de.cubeisland.engine.log.action.logaction.block.player.BucketEmpty;
import de.cubeisland.engine.log.action.logaction.block.player.BucketFill;
import de.cubeisland.engine.log.action.logaction.block.player.HangingBreak;
import de.cubeisland.engine.log.action.logaction.block.player.HangingPlace;
import de.cubeisland.engine.log.action.logaction.block.player.LavaBucket;
import de.cubeisland.engine.log.action.logaction.block.player.PlayerGrow;
import de.cubeisland.engine.log.action.logaction.block.player.SignChange;
import de.cubeisland.engine.log.action.logaction.block.player.WaterBucket;
import de.cubeisland.engine.log.action.logaction.container.ContainerActionType;
import de.cubeisland.engine.log.action.logaction.container.ItemInsert;
import de.cubeisland.engine.log.action.logaction.container.ItemRemove;
import de.cubeisland.engine.log.action.logaction.container.ItemTransfer;
import de.cubeisland.engine.log.action.logaction.interact.EntityDye;
import de.cubeisland.engine.log.action.logaction.interact.EntityShear;
import de.cubeisland.engine.log.action.logaction.interact.FireworkUse;
import de.cubeisland.engine.log.action.logaction.interact.InteractEntityActionType;
import de.cubeisland.engine.log.action.logaction.interact.ItemInFrameRemove;
import de.cubeisland.engine.log.action.logaction.interact.MilkFill;
import de.cubeisland.engine.log.action.logaction.interact.MonsterEggUse;
import de.cubeisland.engine.log.action.logaction.interact.PotionSplash;
import de.cubeisland.engine.log.action.logaction.interact.SoupFill;
import de.cubeisland.engine.log.action.logaction.interact.VehicleBreak;
import de.cubeisland.engine.log.action.logaction.interact.VehicleEnter;
import de.cubeisland.engine.log.action.logaction.interact.VehicleExit;
import de.cubeisland.engine.log.action.logaction.interact.VehiclePlace;
import de.cubeisland.engine.log.action.logaction.kill.AnimalDeath;
import de.cubeisland.engine.log.action.logaction.kill.BossDeath;
import de.cubeisland.engine.log.action.logaction.kill.KillActionType;
import de.cubeisland.engine.log.action.logaction.kill.MonsterDeath;
import de.cubeisland.engine.log.action.logaction.kill.NpcDeath;
import de.cubeisland.engine.log.action.logaction.kill.OtherDeath;
import de.cubeisland.engine.log.action.logaction.kill.PetDeath;
import de.cubeisland.engine.log.action.logaction.kill.PlayerDeath;
import de.cubeisland.engine.log.action.logaction.spawn.EntitySpawnActionType;
import de.cubeisland.engine.log.action.logaction.spawn.NaturalSpawn;
import de.cubeisland.engine.log.action.logaction.spawn.OtherSpawn;
import de.cubeisland.engine.log.action.logaction.spawn.SpawnerSpawn;
import de.cubeisland.engine.log.action.logaction.worldedit.WorldEditActionType;

import de.cubeisland.engine.log.storage.ActionTypeModel;
import gnu.trove.map.hash.TLongObjectHashMap;

public class ActionTypeManager
{
    private final Map<Class<? extends ActionType>,ActionType> registeredActionTypes = new ConcurrentHashMap<>();
    private final Map<String, ActionType> actionTypesByName = new ConcurrentHashMap<>();
    private final TLongObjectHashMap<ActionType> registeredIds = new TLongObjectHashMap<>();
    private final Map<String, ActionTypeCategory> categories = new HashMap<>();
    private final Log module;

    private final Map<String,ActionTypeModel> actionTypeModels;

    public ActionTypeManager(Log module)
    {
        this.module = module;
        this.actionTypeModels = new HashMap<>();
        for (ActionTypeModel actionTypeModel : this.module.getLogManager().getQueryManager().getActionTypesFromDatabase())
        {
            this.actionTypeModels.put(actionTypeModel.getName(), actionTypeModel);
        }
        ActionTypeCompleter.manager = this;
        this.registerLogActionTypes();
    }

    public void registerLogActionTypes()
    {
        this.registerActionType(new BlockBreak())
            .registerActionType(new BlockBurn())
            .registerActionType(new BlockFade())
            .registerActionType(new LeafDecay())
            .registerActionType(new WaterBreak())
            .registerActionType(new LavaBreak())
            .registerActionType(new EntityBreak())
            .registerActionType(new EndermanPickup())
            .registerActionType(new BucketFill())
            .registerActionType(new CropTrample())
            .registerActionType(new EntityExplode())
            .registerActionType(new CreeperExplode())
            .registerActionType(new TntExplode())
            .registerActionType(new FireballExplode())
            .registerActionType(new EnderdragonExplode())
            .registerActionType(new WitherExplode())
            .registerActionType(new TntPrime())
            .registerActionType(new BlockPlace())
            .registerActionType(new LavaBucket())
            .registerActionType(new WaterBucket())
            .registerActionType(new NaturalGrow())
            .registerActionType(new PlayerGrow())
            .registerActionType(new BlockForm())
            .registerActionType(new EndermanPlace())
            .registerActionType(new EntityForm())
            .registerActionType(new FireSpread())
            .registerActionType(new FireballIgnite())
            .registerActionType(new Lighter())
            .registerActionType(new LavaIgnite())
            .registerActionType(new LightningIgnite())
            .registerActionType(new BlockSpread())
            .registerActionType(new WaterFlow())
            .registerActionType(new LavaFlow())
            .registerActionType(new OtherIgnite())
            .registerActionType(new BlockShift())
            .registerActionType(new BlockFall())
            .registerActionType(new SignChange())
            .registerActionType(new SheepEat())
            .registerActionType(new BonemealUse())
            .registerActionType(new LeverUse())
            .registerActionType(new RepeaterChange())
            .registerActionType(new NoteBlockChange())
            .registerActionType(new DoorUse())
            .registerActionType(new CakeEat())
            .registerActionType(new ComparatorChange())
            .registerActionType(new WorldEditActionType())
            .registerActionType(new ContainerAccess())
            .registerActionType(new ButtonUse())
            .registerActionType(new FireworkUse())
            .registerActionType(new VehicleEnter())
            .registerActionType(new VehicleExit())
            .registerActionType(new PotionSplash())
            .registerActionType(new PlateStep())
            .registerActionType(new MilkFill())
            .registerActionType(new SoupFill())
            .registerActionType(new VehiclePlace())
            .registerActionType(new HangingPlace())
            .registerActionType(new VehicleBreak())
            .registerActionType(new HangingBreak())
            .registerActionType(new PlayerDeath())
            .registerActionType(new MonsterDeath())
            .registerActionType(new AnimalDeath())
            .registerActionType(new PetDeath())
            .registerActionType(new NpcDeath())
            .registerActionType(new BossDeath())
            .registerActionType(new OtherDeath())
            .registerActionType(new MonsterEggUse())
            .registerActionType(new NaturalSpawn())
            .registerActionType(new SpawnerSpawn())
            .registerActionType(new OtherSpawn())
            .registerActionType(new ItemDrop())
            .registerActionType(new ItemPickup())
            .registerActionType(new XpPickup())
            .registerActionType(new EntityShear())
            .registerActionType(new EntityDye())
            .registerActionType(new ItemInsert())
            .registerActionType(new ItemRemove())
            .registerActionType(new ItemTransfer())
            .registerActionType(new PlayerCommand())
            .registerActionType(new PlayerChat())
            .registerActionType(new PlayerJoin())
            .registerActionType(new PlayerQuit())
            .registerActionType(new PlayerTeleport())
            .registerActionType(new EnchantItem())
            .registerActionType(new CraftItem())
            .registerActionType(new ItemInFrameRemove());
        this.registerActionType(new EntityChangeActionType())
            .registerActionType(new ExplodeActionType())
            .registerActionType(new FlowActionType())
            .registerActionType(new IgniteActionType())
            .registerActionType(new RightClickActionType())
            .registerActionType(new BucketEmpty())
            .registerActionType(new ContainerActionType())
            .registerActionType(new InteractEntityActionType())
            .registerActionType(new KillActionType())
            .registerActionType(new EntitySpawnActionType());
    }

    public ActionTypeManager registerActionType(ActionType actionType)
    {
        if (actionType.needsModel())
        {
            ActionTypeModel actionTypeModel = this.actionTypeModels.get(actionType.getName());
            if (actionTypeModel == null)
            {
                actionTypeModel = this.module.getLogManager().getQueryManager().registerActionType(actionType.getName());
                this.actionTypeModels.put(actionType.getName(), actionTypeModel);
            }
            actionType.setModel(actionTypeModel);
            registeredIds.put(actionType.getModel().getId().longValue(), actionType);
        }
        registeredActionTypes.put(actionType.getClass(), actionType);
        actionTypesByName.put(actionType.getName(), actionType);
        actionType.initialize(module, this);
        if (actionType.getModel() != null)
        {
            for (ActionTypeCategory category : actionType.getCategories())
            {
                this.categories.put(category.name, category);
            }
            this.module.getLog().debug("ActionType registered: {} {}", actionType.getModel().getId(), actionType.getName());
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <AT extends ActionType> AT getActionType(Class<AT> actionTypeClass)
    {
        return (AT)this.registeredActionTypes.get(actionTypeClass);
    }

    public ActionType getActionType(int id)
    {
        return this.registeredIds.get(id);
    }

    public String getActionTypesAsString()
    {
        TreeSet<String> actionTypes = new TreeSet<>();
        for (ActionType actionType : this.registeredActionTypes.values())
        {
            actionTypes.add(actionType.getName().replace("-", ChatFormat.WHITE + "-" + ChatFormat.GREY));
        }
        return ChatFormat.GREY.toString() + ChatFormat.ITALIC + StringUtils.implode(ChatFormat.WHITE.toString() + ", " + ChatFormat.GREY + ChatFormat.ITALIC, actionTypes);
    }

    public Set<ActionType> getActionType(String actionString)
    {

        ActionTypeCategory category = this.categories.get(actionString);
        if (category == null)
        {
            String match = Match.string().matchString(actionString, this.actionTypesByName.keySet());
            if (match == null) return null;
            HashSet<ActionType> actionTypes = new HashSet<>();
            actionTypes.add(this.actionTypesByName.get(match));
            return actionTypes;
        }
        else
        {
            return category.getActionTypes();
        }
    }

    public Set<String> getAllActionAndCategoryStrings()
    {
        HashSet<String> strings = new HashSet<>();
        strings.addAll(this.categories.keySet());
        strings.addAll(this.actionTypesByName.keySet());
        return strings;
    }
}
