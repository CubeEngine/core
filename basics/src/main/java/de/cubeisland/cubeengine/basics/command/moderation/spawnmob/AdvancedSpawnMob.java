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
package de.cubeisland.cubeengine.basics.command.moderation.spawnmob;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.converstion.ConversationCommand;
import de.cubeisland.cubeengine.core.command.converstion.ConversationContextFactory;
import de.cubeisland.cubeengine.core.command.parameterized.CommandFlag;
import de.cubeisland.cubeengine.core.command.parameterized.CommandParameter;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.basics.Basics;

import gnu.trove.map.hash.TLongObjectHashMap;

public class AdvancedSpawnMob extends ConversationCommand
{
    public AdvancedSpawnMob(Basics module)
    {
        super(module, new ConversationContextFactory());
        this.getContextFactory()
                .addFlag(new CommandFlag("exit", "exit"))
                .addFlag(new CommandFlag("spawn", "spawnmob"))
                .addParameter(new CommandParameter("mob", EntityType.class))
                .addParameter(new CommandParameter("amount", Integer.class))
                .addFlag(new CommandFlag("charge", "charged"))
                .addFlag(new CommandFlag("baby", "baby"))
                .addFlag(new CommandFlag("saddle", "saddled"))
                .addFlag(new CommandFlag("angry", "angry"))
                .addFlag(new CommandFlag("sitting", "sitting"))
                .addFlag(new CommandFlag("tame", "tame"))
                .addFlag(new CommandFlag("villager", "villagerzombie"))
                .addParameter(new CommandParameter("color", DyeColor.class).addAlias("sheepcolor"))
                .addParameter(new CommandParameter("size", String.class).addAlias("slimesize"))
                .addParameter(new CommandParameter("prof", Villager.Profession.class).addAlias("profession"))
                .addParameter(new CommandParameter("endermanitem", ItemStack.class))
                .addParameter(new CommandParameter("equip-hand", ItemStack.class))
                .addParameter(new CommandParameter("equip-helmet", ItemStack.class))
                .addParameter(new CommandParameter("equip-boots", ItemStack.class))
                .addParameter(new CommandParameter("equip-chestplate", ItemStack.class))
                .addParameter(new CommandParameter("equip-leggings", ItemStack.class))
                .addParameter(new CommandParameter("hp", Integer.class))
                .addFlag(new CommandFlag("here", "here"))
                .addParameter(new CommandParameter("nearby", User.class))
                .addFlag(new CommandFlag("info", "info"))
                .addFlag(new CommandFlag("clear", "clear"))
        //TODO riding mobs
        ;

    }

    private TLongObjectHashMap<SpawningData> spawningDatas = new TLongObjectHashMap<SpawningData>();

    @Override
    public boolean addUser(User user) {
        SpawningData spawningData;
        this.spawningDatas.put(user.key,spawningData= new SpawningData());
        spawningData.location = user.getTargetBlock(null, 200).getLocation().add(new Vector(0, 1, 0));
        return super.addUser(user);
    }

    @Override
    public void removeUser(User user) {
        super.removeUser(user);
        this.spawningDatas.remove(user.key);
    }

    @Override
    public CommandResult run(CommandContext runContext) throws Exception
    {
        ParameterizedContext context = (ParameterizedContext)runContext;
        User user = (User) context.getSender();
        SpawningData spawningData = this.spawningDatas.get(user.key);
        if (context.hasFlag("clear"))
        {
            spawningData.clearData();
        }
        if (context.hasParam("mob"))
        {
            EntityType entityType = context.getParam("mob",EntityType.ARROW);
            if (entityType.isAlive())
            {
                spawningData.entityType = entityType;
            }
            else
            {
                context.sendTranslated("&6%s&c is not a living Entity!", Match.entity().getNameFor(entityType));
            }
        }
        if (context.hasFlag("baby"))
        {
            spawningData.add(EntityDataChanger.BABYAGEABLE, true);
            spawningData.add(EntityDataChanger.BABYZOMBIE, true);
        }
        if (context.hasFlag("charged"))
        {
            spawningData.add(EntityDataChanger.POWERED, true);
        }
        if (context.hasFlag("saddle"))
        {
            spawningData.add(EntityDataChanger.PIGSADDLE, true);
        }
        if (context.hasFlag("angry"))
        {
            spawningData.add(EntityDataChanger.ANGRYPIGZOMBIE, true);
            spawningData.add(EntityDataChanger.ANGRYWOLF, true);
        }
        if (context.hasFlag("sitting"))
        {
            spawningData.add(EntityDataChanger.SITTINGOCELOT, true);
            spawningData.add(EntityDataChanger.SITTINGWOLF, true);
        }
        if (context.hasFlag("tame"))
        {
            spawningData.add(EntityDataChanger.TAME,user);
        }
        if (context.hasFlag("villager"))
        {
            spawningData.add(EntityDataChanger.VILLAGER_ZOMBIE, true);
        }
        if (context.hasParam("color"))
        {
            spawningData.add(EntityDataChanger.SHEEP_COLOR,context.getParam("color",DyeColor.WHITE));
        }
        if (context.hasParam("size"))
        {
            String match = Match.string().matchString(context.getString("size"), "tiny", "small", "big");
            try
            {
                int size = "tiny".equals(match) ? 0
                        : "small".equals(match) ? 2
                        : "big".equals(match) ? 4
                        : Integer.parseInt(context.getString("size"));
                spawningData.add(EntityDataChanger.SLIME_SIZE,size);
            }
            catch (NumberFormatException e)
            {
                context.sendTranslated("&eThe slime-size has to be a number or tiny, small or big!");
            }
        }
        if (context.hasParam("prof"))
        {
            spawningData.add(EntityDataChanger.VILLAGER_PROFESSION,context.getParam("prof", Villager.Profession.FARMER));
        }
        if (context.hasParam("endermanitem"))
        {
            spawningData.add(EntityDataChanger.ENDERMAN_ITEM,context.getParam("endermanitem",AIR));
        }
        if (context.hasParam("equip-hand"))
        {
            spawningData.add(EntityDataChanger.EQUIP_ITEMINHAND,context.getParam("equip-hand",AIR));
        }
        if (context.hasParam("equip-helmet"))
        {
            spawningData.add(EntityDataChanger.EQUIP_HELMET,context.getParam("equip-helmet",AIR));
        }
        if (context.hasParam("equip-boots"))
        {
            spawningData.add(EntityDataChanger.EQUIP_BOOTS,context.getParam("equip-boots",AIR));
        }
        if (context.hasParam("equip-chestplate"))
        {
            spawningData.add(EntityDataChanger.EQUIP_CHESTPLATE,context.getParam("equip-chestplate",AIR));
        }
        if (context.hasParam("equip-leggings"))
        {
            spawningData.add(EntityDataChanger.EQUIP_LEGGINGS,context.getParam("equip-leggings",AIR));
        }
        if (context.hasParam("hp"))
        {
            spawningData.add(EntityDataChanger.HP,(Integer)context.getParam("hp",null));
        }
        if (context.hasFlag("here"))
        {
            user.getLocation(spawningData.location);
        }
        if (context.hasParam("nearby"))
        {
            User nearby = context.getParam("nearby",null);
            if (nearby == null || !nearby.isOnline())
            {
                context.sendTranslated("&cUser &2%s &cnot found!",context.getString("nearby"));
            }
            else
            {
                nearby.getLocation(spawningData.location);
            }
        }
        if (context.hasParam("amount"))
        {
            Integer amount = context.getParam("amount",0);
            if (amount == 0)
            {
                context.sendTranslated("&cInvalid amount!");
                amount = 1;
            }
            spawningData.amount = amount;
        }
        if (context.hasFlag("info"))
        {
            spawningData.showInfo(user);
        }
        if (context.hasFlag("spawn"))
        {
            spawningData.doSpawn();
        }
        if (context.hasFlag("exit"))
        {
            this.removeUser(user);
            return null;
        }
        return null;
    }

    private static final ItemStack AIR = new ItemStack(Material.AIR);
}
