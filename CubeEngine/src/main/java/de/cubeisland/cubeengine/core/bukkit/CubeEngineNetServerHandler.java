package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.bukkit.event.PacketReceivedEvent;
import de.cubeisland.cubeengine.core.bukkit.event.PacketSentEvent;
import de.cubeisland.cubeengine.core.util.worker.TaskQueue;
import net.minecraft.server.v1_4_6.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

/**
 * This class is used to replace the original NetServerHandler and calling an
 * Events when receiving packets.
 */
public class CubeEngineNetServerHandler extends PlayerConnection
{
    private final PluginManager pm;
    private final TaskQueue taskQueue;
    private final Player bukkitPlayer;

    public CubeEngineNetServerHandler(EntityPlayer player, TaskQueue taskQueue)
    {
        super(player.server, player.playerConnection.networkManager, player);
        this.pm = player.getBukkitEntity().getServer().getPluginManager();
        this.taskQueue = taskQueue;
        this.bukkitPlayer = player.getBukkitEntity();
    }

    public void packetReceived(final Packet packet)
    {
        // System.out.println("Received: " + packet.k());
        if (PacketReceivedEvent.getHandlerList().getRegisteredListeners().length == 0)
        {
            return;
        }
        this.taskQueue.addTask(new Runnable()
        {
            @Override
            public void run()
            {
                pm.callEvent(new PacketReceivedEvent(bukkitPlayer, packet));
            }
        });
    }

    @Override
    public void sendPacket(final Packet packet)
    {
        super.sendPacket(packet);
        // System.out.println("Sent: " + packet.k());
        if (PacketSentEvent.getHandlerList().getRegisteredListeners().length == 0)
        {
            return;
        }
        this.taskQueue.addTask(new Runnable()
        {
            @Override
            public void run()
            {
                pm.callEvent(new PacketSentEvent(bukkitPlayer, packet));
            }
        });
    }

    @Override
    public void a(Packet0KeepAlive packet0keepalive)
    {
        super.a(packet0keepalive);
        this.packetReceived(packet0keepalive);
    }

    @Override
    public void a(Packet1Login pl)
    {
        super.a(pl);
        this.packetReceived(pl);
    }

    @Override
    public void a(Packet2Handshake ph)
    {
        super.a(ph);
        this.packetReceived(ph);
    }

    @Override
    public void a(Packet3Chat packet3chat)
    {
        super.a(packet3chat);
        this.packetReceived(packet3chat);
    }

    @Override
    public void a(Packet4UpdateTime put)
    {
        super.a(put);
        this.packetReceived(put);
    }

    @Override
    public void a(Packet5EntityEquipment pee)
    {
        super.a(pee);
        this.packetReceived(pee);
    }

    @Override
    public void a(Packet6SpawnPosition psp)
    {
        super.a(psp);
        this.packetReceived(psp);
    }

    @Override
    public void a(Packet7UseEntity packet7useentity)
    {
        super.a(packet7useentity);
        this.packetReceived(packet7useentity);
    }

    @Override
    public void a(Packet8UpdateHealth puh)
    {
        super.a(puh);
        this.packetReceived(puh);
    }

    @Override
    public void a(Packet9Respawn packet9respawn)
    {
        super.a(packet9respawn);
        this.packetReceived(packet9respawn);
    }

    @Override
    public void a(Packet10Flying packet10flying)
    {
        super.a(packet10flying);
        this.packetReceived(packet10flying);
    }

    @Override
    public void a(Packet14BlockDig packet14blockdig)
    {
        super.a(packet14blockdig);
        this.packetReceived(packet14blockdig);
    }

    @Override
    public void a(Packet15Place packet15place)
    {
        super.a(packet15place);
        this.packetReceived(packet15place);
    }

    @Override
    public void a(Packet16BlockItemSwitch packet16blockitemswitch)
    {
        super.a(packet16blockitemswitch);
        this.packetReceived(packet16blockitemswitch);
    }

    @Override
    public void a(Packet17EntityLocationAction action)
    {
        super.a(action);
        this.packetReceived(action);
    }

    @Override
    public void a(Packet18ArmAnimation packet18armanimation)
    {
        super.a(packet18armanimation);
        this.packetReceived(packet18armanimation);
    }

    @Override
    public void a(Packet19EntityAction packet19entityaction)
    {
        super.a(packet19entityaction);
        this.packetReceived(packet19entityaction);
    }

    @Override
    public void a(Packet20NamedEntitySpawn pnes)
    {
        super.a(pnes);
        this.packetReceived(pnes);
    }

    @Override
    public void a(Packet22Collect pc)
    {
        super.a(pc);
        this.packetReceived(pc);
    }

    @Override
    public void a(Packet23VehicleSpawn pvs)
    {
        super.a(pvs);
        this.packetReceived(pvs);
    }

    @Override
    public void a(Packet24MobSpawn pms)
    {
        super.a(pms);
        this.packetReceived(pms);
    }

    @Override
    public void a(Packet25EntityPainting pep)
    {
        super.a(pep);
        this.packetReceived(pep);
    }

    @Override
    public void a(Packet26AddExpOrb paeo)
    {
        super.a(paeo);
        this.packetReceived(paeo);
    }

    @Override
    public void a(Packet28EntityVelocity pev)
    {
        super.a(pev);
        this.packetReceived(pev);
    }

    @Override
    public void a(Packet29DestroyEntity pde)
    {
        super.a(pde);
        this.packetReceived(pde);
    }

    @Override
    public void a(Packet30Entity pe)
    {
        super.a(pe);
        this.packetReceived(pe);
    }

    @Override
    public void a(Packet34EntityTeleport pet)
    {
        super.a(pet);
        this.packetReceived(pet);
    }

    @Override
    public void a(Packet35EntityHeadRotation pehr)
    {
        super.a(pehr);
        this.packetReceived(pehr);
    }

    @Override
    public void a(Packet38EntityStatus pes)
    {
        super.a(pes);
        this.packetReceived(pes);
    }

    @Override
    public void a(Packet39AttachEntity pae)
    {
        super.a(pae);
        this.packetReceived(pae);
    }

    @Override
    public void a(Packet40EntityMetadata pem)
    {
        super.a(pem);
        this.packetReceived(pem);
    }

    @Override
    public void a(Packet41MobEffect pme)
    {
        super.a(pme);
        this.packetReceived(pme);
    }

    @Override
    public void a(Packet42RemoveMobEffect prme)
    {
        super.a(prme);
        this.packetReceived(prme);
    }

    @Override
    public void a(Packet43SetExperience pse)
    {
        super.a(pse);
        this.packetReceived(pse);
    }

    @Override
    public void a(Packet51MapChunk pmc)
    {
        super.a(pmc);
        this.packetReceived(pmc);
    }

    @Override
    public void a(Packet52MultiBlockChange pmbc)
    {
        super.a(pmbc);
        this.packetReceived(pmbc);
    }

    @Override
    public void a(Packet53BlockChange pbc)
    {
        super.a(pbc);
        this.packetReceived(pbc);
    }

    @Override
    public void a(Packet54PlayNoteBlock ppnb)
    {
        super.a(ppnb);
        this.packetReceived(ppnb);
    }

    @Override
    public void a(Packet55BlockBreakAnimation pbba)
    {
        super.a(pbba);
        this.packetReceived(pbba);
    }

    @Override
    public void a(Packet56MapChunkBulk pmcb)
    {
        super.a(pmcb);
        this.packetReceived(pmcb);
    }

    @Override
    public void a(Packet60Explosion pe)
    {
        super.a(pe);
        this.packetReceived(pe);
    }

    @Override
    public void a(Packet61WorldEvent pwe)
    {
        super.a(pwe);
        this.packetReceived(pwe);
    }

    @Override
    public void a(Packet62NamedSoundEffect pnse)
    {
        super.a(pnse);
        this.packetReceived(pnse);
    }

    @Override
    public void a(Packet70Bed pb)
    {
        super.a(pb);
        this.packetReceived(pb);
    }

    @Override
    public void a(Packet71Weather pw)
    {
        super.a(pw);
        this.packetReceived(pw);
    }

    @Override
    public void a(Packet100OpenWindow pow)
    {
        super.a(pow);
        this.packetReceived(pow);
    }

    @Override
    public void a(Packet102WindowClick packet102windowclick)
    {
        super.a(packet102windowclick);
        this.packetReceived(packet102windowclick);
    }

    @Override
    public void a(Packet103SetSlot pss)
    {
        super.a(pss);
        this.packetReceived(pss);
    }

    @Override
    public void a(Packet104WindowItems pwi)
    {
        super.a(pwi);
        this.packetReceived(pwi);
    }

    @Override
    public void a(Packet105CraftProgressBar pcpb)
    {
        super.a(pcpb);
        this.packetReceived(pcpb);
    }

    @Override
    public void a(Packet106Transaction packet106transaction)
    {
        super.a(packet106transaction);
        this.packetReceived(packet106transaction);
    }

    @Override
    public void a(Packet107SetCreativeSlot packet107setcreativeslot)
    {
        super.a(packet107setcreativeslot);
        this.packetReceived(packet107setcreativeslot);
    }

    @Override
    public void a(Packet108ButtonClick packet108buttonclick)
    {
        super.a(packet108buttonclick);
        this.packetReceived(packet108buttonclick);
    }

    @Override
    public void a(Packet130UpdateSign packet130updatesign)
    {
        super.a(packet130updatesign);
        this.packetReceived(packet130updatesign);
    }

    @Override
    public void a(Packet131ItemData pid)
    {
        super.a(pid);
        this.packetReceived(pid);
    }

    @Override
    public void a(Packet132TileEntityData pted)
    {
        super.a(pted);
        this.packetReceived(pted);
    }

    @Override
    public void a(Packet200Statistic ps)
    {
        super.a(ps);
        this.packetReceived(ps);
    }

    @Override
    public void a(Packet201PlayerInfo ppi)
    {
        super.a(ppi);
        this.packetReceived(ppi);
    }

    @Override
    public void a(Packet202Abilities packet202abilities)
    {
        super.a(packet202abilities);
        this.packetReceived(packet202abilities);
    }

    @Override
    public void a(Packet203TabComplete packet203tabcomplete)
    {
        super.a(packet203tabcomplete);
        this.packetReceived(packet203tabcomplete);
    }

    @Override
    public void a(Packet204LocaleAndViewDistance packet204localeandviewdistance)
    {
        super.a(packet204localeandviewdistance);
        this.packetReceived(packet204localeandviewdistance);
    }

    @Override
    public void a(Packet205ClientCommand packet205clientcommand)
    {
        super.a(packet205clientcommand);
        this.packetReceived(packet205clientcommand);
    }

    @Override
    public void a(Packet250CustomPayload packet250custompayload)
    {
        super.a(packet250custompayload);
        this.packetReceived(packet250custompayload);
    }

    @Override
    public void a(Packet252KeyResponse pkr)
    {
        super.a(pkr);
        this.packetReceived(pkr);
    }

    @Override
    public void a(Packet253KeyRequest pkr)
    {
        super.a(pkr);
        this.packetReceived(pkr);
    }

    @Override
    public void a(Packet254GetInfo pgi)
    {
        super.a(pgi);
        this.packetReceived(pgi);
    }

    @Override
    public void a(Packet255KickDisconnect packet255kickdisconnect)
    {
        super.a(packet255kickdisconnect);
        this.packetReceived(packet255kickdisconnect);
    }
}
