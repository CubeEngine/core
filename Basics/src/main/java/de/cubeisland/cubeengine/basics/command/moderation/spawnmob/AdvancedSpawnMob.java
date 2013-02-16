package de.cubeisland.cubeengine.basics.command.moderation.spawnmob;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.chatcommand.ChatCommand;
import de.cubeisland.cubeengine.core.command.chatcommand.ChatCommandContext;
import de.cubeisland.cubeengine.core.command.chatcommand.ChatCommandContextFactory;
import de.cubeisland.cubeengine.core.command.parameterized.CommandFlag;
import de.cubeisland.cubeengine.core.command.parameterized.CommandParameter;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class AdvancedSpawnMob extends ChatCommand<Basics>
{
    public AdvancedSpawnMob(Basics module)
    {
        super(module, new ChatCommandContextFactory());
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
                .addFlag(new CommandFlag("tame", "tame")) //TODO
                .addFlag(new CommandFlag("villager", "villagerzombie")) //TODO
                .addParameter(new CommandParameter("color", String.class).addAlias("sheepcolor"))//TODO
                .addParameter(new CommandParameter("size", Integer.class).addAlias("slimesize"))//TODO
                .addParameter(new CommandParameter("prof", Villager.Profession.class).addAlias("profession"))//TODO
                .addParameter(new CommandParameter("endermanitem", ItemStack.class))//TODO
                .addParameter(new CommandParameter("equip-hand", ItemStack.class))//TODO
                .addParameter(new CommandParameter("equip-helmet", ItemStack.class))//TODO
                .addParameter(new CommandParameter("equip-boots", ItemStack.class))//TODO
                .addParameter(new CommandParameter("equip-chestplate", ItemStack.class))//TODO
                .addParameter(new CommandParameter("equip-leggings", ItemStack.class))//TODO
                .addParameter(new CommandParameter("hp", Integer.class))//TODO
        ;

    }

    private TLongObjectHashMap<SpawningData> spawningDatas = new TLongObjectHashMap<SpawningData>();

    @Override
    public void addUser(User user) {
        super.addUser(user);
        SpawningData spawningData;
        this.spawningDatas.put(user.key,spawningData= new SpawningData());
        spawningData.location = user.getTargetBlock(null, 200).getLocation().add(new Vector(0, 1, 0));
    }

    @Override
    public void removeUser(User user) {
        super.removeUser(user);
        this.spawningDatas.remove(user.key);
    }

    @Override
    public CommandResult run(CommandContext runContext) throws Exception
    {
        ChatCommandContext context = (ChatCommandContext)runContext;
        User user = (User) context.getSender();
        SpawningData spawningData = this.spawningDatas.get(user.key);
        if (context.hasParam("mob"))
        {
            EntityType entityType = context.getParam("mob",EntityType.ARROW);
            if (entityType.isAlive())
            {
                spawningData.entityType = entityType;
            }
            else
            {
                context.sendMessage("basics","&6%s &cis not a living Entity!", Match.entity().getNameFor(entityType));
            }
        }
        if (context.hasFlag("baby"))
        {
            spawningData.add(EntityDataChanger.BABY, true);
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
            spawningData.add(EntityDataChanger.ANGRY, true);
        }
        if (context.hasFlag("sitting"))
        {
            spawningData.add(EntityDataChanger.SITTING, true);
        }
        if (context.hasParam("amount"))
        {
            Integer amount = context.getParam("amount",0);
            if (amount == 0)
            {
                context.sendMessage("basics","&cInvalid amount!");
                amount = 1;
            }
            spawningData.amount = amount;
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
}
