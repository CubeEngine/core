package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.bukkit.event.PacketReceivedEvent;
import de.cubeisland.cubeengine.core.bukkit.event.PacketSentEvent;
import de.cubeisland.cubeengine.core.util.worker.TaskQueue;
import net.minecraft.server.v1_4_R1.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

/**
 * This class is used to replace the original NetServerHandler and calling an
 * Events when receiving packets.
 */
public class CubePlayerConnection extends PlayerConnection
{
    private final PluginManager pm;
    private final Player bukkitPlayer;

    public CubePlayerConnection(EntityPlayer player)
    {
        super(player.server, player.playerConnection.networkManager, player);
        this.pm = player.getBukkitEntity().getServer().getPluginManager();
        this.bukkitPlayer = player.getBukkitEntity();
    }

    public void packetReceived(final Packet packet)
    {
        // System.out.println("Received: " + packet.k());
        if (PacketReceivedEvent.getHandlerList().getRegisteredListeners().length == 0)
        {
            return;
        }
        this.pm.callEvent(new PacketReceivedEvent(bukkitPlayer, packet));
    }

    @Override
    public void sendPacket(final Packet packet)
    {
        // System.out.println("Sent: " + packet.k());
        if (PacketSentEvent.getHandlerList().getRegisteredListeners().length > 0)
        {
            this.pm.callEvent(new PacketSentEvent(bukkitPlayer, packet));
        }
        super.sendPacket(packet);
    }

    @Override
    public void a(Packet0KeepAlive packet0keepalive)
    {
        this.packetReceived(packet0keepalive);
        super.a(packet0keepalive);
    }

    @Override
    public void a(Packet1Login pl)
    {
        this.packetReceived(pl);
        super.a(pl);
    }

    @Override
    public void a(Packet2Handshake ph)
    {
        this.packetReceived(ph);
        super.a(ph);
    }

    @Override
    public void a(Packet3Chat packet3chat)
    {
        this.packetReceived(packet3chat);
        super.a(packet3chat);
    }

    @Override
    public void a(Packet4UpdateTime put)
    {
        this.packetReceived(put);
        super.a(put);
    }

    @Override
    public void a(Packet5EntityEquipment pee)
    {
        this.packetReceived(pee);
        super.a(pee);
    }

    @Override
    public void a(Packet6SpawnPosition psp)
    {
        this.packetReceived(psp);
        super.a(psp);
    }

    @Override
    public void a(Packet7UseEntity packet7useentity)
    {
        this.packetReceived(packet7useentity);
        super.a(packet7useentity);
    }

    @Override
    public void a(Packet8UpdateHealth puh)
    {
        this.packetReceived(puh);
        super.a(puh);
    }

    @Override
    public void a(Packet9Respawn packet9respawn)
    {
        this.packetReceived(packet9respawn);
        super.a(packet9respawn);
    }

    @Override
    public void a(Packet10Flying packet10flying)
    {
        this.packetReceived(packet10flying);
        super.a(packet10flying);
    }

    @Override
    public void a(Packet14BlockDig packet14blockdig)
    {
        this.packetReceived(packet14blockdig);
        super.a(packet14blockdig);
    }

    @Override
    public void a(Packet15Place packet15place)
    {
        this.packetReceived(packet15place);
        super.a(packet15place);
    }

    @Override
    public void a(Packet16BlockItemSwitch packet16blockitemswitch)
    {
        this.packetReceived(packet16blockitemswitch);
        super.a(packet16blockitemswitch);
    }

    @Override
    public void a(Packet17EntityLocationAction action)
    {
        this.packetReceived(action);
        super.a(action);
    }

    @Override
    public void a(Packet18ArmAnimation packet18armanimation)
    {
        this.packetReceived(packet18armanimation);
        super.a(packet18armanimation);
    }

    @Override
    public void a(Packet19EntityAction packet19entityaction)
    {
        this.packetReceived(packet19entityaction);
        super.a(packet19entityaction);
    }

    @Override
    public void a(Packet20NamedEntitySpawn pnes)
    {
        this.packetReceived(pnes);
        super.a(pnes);
    }

    @Override
    public void a(Packet22Collect pc)
    {
        this.packetReceived(pc);
        super.a(pc);
    }

    @Override
    public void a(Packet23VehicleSpawn pvs)
    {
        this.packetReceived(pvs);
        super.a(pvs);
    }

    @Override
    public void a(Packet24MobSpawn pms)
    {
        this.packetReceived(pms);
        super.a(pms);
    }

    @Override
    public void a(Packet25EntityPainting pep)
    {
        this.packetReceived(pep);
        super.a(pep);
    }

    @Override
    public void a(Packet26AddExpOrb paeo)
    {
        this.packetReceived(paeo);
        super.a(paeo);
    }

    @Override
    public void a(Packet28EntityVelocity pev)
    {
        this.packetReceived(pev);
        super.a(pev);
    }

    @Override
    public void a(Packet29DestroyEntity pde)
    {
        this.packetReceived(pde);
        super.a(pde);
    }

    @Override
    public void a(Packet30Entity pe)
    {
        this.packetReceived(pe);
        super.a(pe);
    }

    @Override
    public void a(Packet34EntityTeleport pet)
    {
        this.packetReceived(pet);
        super.a(pet);
    }

    @Override
    public void a(Packet35EntityHeadRotation pehr)
    {
        this.packetReceived(pehr);
        super.a(pehr);
    }

    @Override
    public void a(Packet38EntityStatus pes)
    {
        this.packetReceived(pes);
        super.a(pes);
    }

    @Override
    public void a(Packet39AttachEntity pae)
    {
        this.packetReceived(pae);
        super.a(pae);
    }

    @Override
    public void a(Packet40EntityMetadata pem)
    {
        this.packetReceived(pem);
        super.a(pem);
    }

    @Override
    public void a(Packet41MobEffect pme)
    {
        this.packetReceived(pme);
        super.a(pme);
    }

    @Override
    public void a(Packet42RemoveMobEffect prme)
    {
        this.packetReceived(prme);
        super.a(prme);
    }

    @Override
    public void a(Packet43SetExperience pse)
    {
        this.packetReceived(pse);
        super.a(pse);
    }

    @Override
    public void a(Packet51MapChunk pmc)
    {
        this.packetReceived(pmc);
        super.a(pmc);
    }

    @Override
    public void a(Packet52MultiBlockChange pmbc)
    {
        this.packetReceived(pmbc);
        super.a(pmbc);
    }

    @Override
    public void a(Packet53BlockChange pbc)
    {
        this.packetReceived(pbc);
        super.a(pbc);
    }

    @Override
    public void a(Packet54PlayNoteBlock ppnb)
    {
        this.packetReceived(ppnb);
        super.a(ppnb);
    }

    @Override
    public void a(Packet55BlockBreakAnimation pbba)
    {
        this.packetReceived(pbba);
        super.a(pbba);
    }

    @Override
    public void a(Packet56MapChunkBulk pmcb)
    {
        this.packetReceived(pmcb);
        super.a(pmcb);
    }

    @Override
    public void a(Packet60Explosion pe)
    {
        this.packetReceived(pe);
        super.a(pe);
    }

    @Override
    public void a(Packet61WorldEvent pwe)
    {
        this.packetReceived(pwe);
        super.a(pwe);
    }

    @Override
    public void a(Packet62NamedSoundEffect pnse)
    {
        this.packetReceived(pnse);
        super.a(pnse);
    }

    @Override
    public void a(Packet70Bed pb)
    {
        this.packetReceived(pb);
        super.a(pb);
    }

    @Override
    public void a(Packet71Weather pw)
    {
        this.packetReceived(pw);
        super.a(pw);
    }

    @Override
    public void a(Packet100OpenWindow pow)
    {
        this.packetReceived(pow);
        super.a(pow);
    }

    @Override
    public void a(Packet102WindowClick packet102windowclick)
    {
        this.packetReceived(packet102windowclick);
        super.a(packet102windowclick);
    }

    @Override
    public void a(Packet103SetSlot pss)
    {
        this.packetReceived(pss);
        super.a(pss);
    }

    @Override
    public void a(Packet104WindowItems pwi)
    {
        this.packetReceived(pwi);
        super.a(pwi);
    }

    @Override
    public void a(Packet105CraftProgressBar pcpb)
    {
        this.packetReceived(pcpb);
        super.a(pcpb);
    }

    @Override
    public void a(Packet106Transaction packet106transaction)
    {
        this.packetReceived(packet106transaction);
        super.a(packet106transaction);
    }

    @Override
    public void a(Packet107SetCreativeSlot packet107setcreativeslot)
    {
        this.packetReceived(packet107setcreativeslot);
        super.a(packet107setcreativeslot);
    }

    @Override
    public void a(Packet108ButtonClick packet108buttonclick)
    {
        this.packetReceived(packet108buttonclick);
        super.a(packet108buttonclick);
    }

    @Override
    public void a(Packet130UpdateSign packet130updatesign)
    {
        this.packetReceived(packet130updatesign);
        super.a(packet130updatesign);
    }

    @Override
    public void a(Packet131ItemData pid)
    {
        this.packetReceived(pid);
        super.a(pid);
    }

    @Override
    public void a(Packet132TileEntityData pted)
    {
        this.packetReceived(pted);
        super.a(pted);
    }

    @Override
    public void a(Packet200Statistic ps)
    {
        this.packetReceived(ps);
        super.a(ps);
    }

    @Override
    public void a(Packet201PlayerInfo ppi)
    {
        this.packetReceived(ppi);
        super.a(ppi);
    }

    @Override
    public void a(Packet202Abilities packet202abilities)
    {
        this.packetReceived(packet202abilities);
        super.a(packet202abilities);
    }

    @Override
    public void a(Packet203TabComplete packet203tabcomplete)
    {
        this.packetReceived(packet203tabcomplete);
        super.a(packet203tabcomplete);
    }

    @Override
    public void a(Packet204LocaleAndViewDistance packet204localeandviewdistance)
    {
        this.packetReceived(packet204localeandviewdistance);
        super.a(packet204localeandviewdistance);
    }

    @Override
    public void a(Packet205ClientCommand packet205clientcommand)
    {
        this.packetReceived(packet205clientcommand);
        super.a(packet205clientcommand);
    }

    @Override
    public void a(Packet250CustomPayload packet250custompayload)
    {
        this.packetReceived(packet250custompayload);
        super.a(packet250custompayload);
    }

    @Override
    public void a(Packet252KeyResponse pkr)
    {
        this.packetReceived(pkr);
        super.a(pkr);
    }

    @Override
    public void a(Packet253KeyRequest pkr)
    {
        this.packetReceived(pkr);
        super.a(pkr);
    }

    @Override
    public void a(Packet254GetInfo pgi)
    {
        this.packetReceived(pgi);
        super.a(pgi);
    }

    @Override
    public void a(Packet255KickDisconnect packet255kickdisconnect)
    {
        this.packetReceived(packet255kickdisconnect);
        super.a(packet255kickdisconnect);
    }
}
