package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.packethook.PacketEventManager;
import net.minecraft.server.v1_5_R1.EntityPlayer;
import net.minecraft.server.v1_5_R1.Packet;
import net.minecraft.server.v1_5_R1.Packet0KeepAlive;
import net.minecraft.server.v1_5_R1.Packet100OpenWindow;
import net.minecraft.server.v1_5_R1.Packet102WindowClick;
import net.minecraft.server.v1_5_R1.Packet103SetSlot;
import net.minecraft.server.v1_5_R1.Packet104WindowItems;
import net.minecraft.server.v1_5_R1.Packet105CraftProgressBar;
import net.minecraft.server.v1_5_R1.Packet106Transaction;
import net.minecraft.server.v1_5_R1.Packet107SetCreativeSlot;
import net.minecraft.server.v1_5_R1.Packet108ButtonClick;
import net.minecraft.server.v1_5_R1.Packet10Flying;
import net.minecraft.server.v1_5_R1.Packet130UpdateSign;
import net.minecraft.server.v1_5_R1.Packet131ItemData;
import net.minecraft.server.v1_5_R1.Packet132TileEntityData;
import net.minecraft.server.v1_5_R1.Packet14BlockDig;
import net.minecraft.server.v1_5_R1.Packet15Place;
import net.minecraft.server.v1_5_R1.Packet16BlockItemSwitch;
import net.minecraft.server.v1_5_R1.Packet17EntityLocationAction;
import net.minecraft.server.v1_5_R1.Packet18ArmAnimation;
import net.minecraft.server.v1_5_R1.Packet19EntityAction;
import net.minecraft.server.v1_5_R1.Packet1Login;
import net.minecraft.server.v1_5_R1.Packet200Statistic;
import net.minecraft.server.v1_5_R1.Packet201PlayerInfo;
import net.minecraft.server.v1_5_R1.Packet202Abilities;
import net.minecraft.server.v1_5_R1.Packet203TabComplete;
import net.minecraft.server.v1_5_R1.Packet204LocaleAndViewDistance;
import net.minecraft.server.v1_5_R1.Packet205ClientCommand;
import net.minecraft.server.v1_5_R1.Packet20NamedEntitySpawn;
import net.minecraft.server.v1_5_R1.Packet22Collect;
import net.minecraft.server.v1_5_R1.Packet23VehicleSpawn;
import net.minecraft.server.v1_5_R1.Packet24MobSpawn;
import net.minecraft.server.v1_5_R1.Packet250CustomPayload;
import net.minecraft.server.v1_5_R1.Packet252KeyResponse;
import net.minecraft.server.v1_5_R1.Packet253KeyRequest;
import net.minecraft.server.v1_5_R1.Packet254GetInfo;
import net.minecraft.server.v1_5_R1.Packet255KickDisconnect;
import net.minecraft.server.v1_5_R1.Packet25EntityPainting;
import net.minecraft.server.v1_5_R1.Packet26AddExpOrb;
import net.minecraft.server.v1_5_R1.Packet28EntityVelocity;
import net.minecraft.server.v1_5_R1.Packet29DestroyEntity;
import net.minecraft.server.v1_5_R1.Packet2Handshake;
import net.minecraft.server.v1_5_R1.Packet30Entity;
import net.minecraft.server.v1_5_R1.Packet34EntityTeleport;
import net.minecraft.server.v1_5_R1.Packet35EntityHeadRotation;
import net.minecraft.server.v1_5_R1.Packet38EntityStatus;
import net.minecraft.server.v1_5_R1.Packet39AttachEntity;
import net.minecraft.server.v1_5_R1.Packet3Chat;
import net.minecraft.server.v1_5_R1.Packet40EntityMetadata;
import net.minecraft.server.v1_5_R1.Packet41MobEffect;
import net.minecraft.server.v1_5_R1.Packet42RemoveMobEffect;
import net.minecraft.server.v1_5_R1.Packet43SetExperience;
import net.minecraft.server.v1_5_R1.Packet4UpdateTime;
import net.minecraft.server.v1_5_R1.Packet51MapChunk;
import net.minecraft.server.v1_5_R1.Packet52MultiBlockChange;
import net.minecraft.server.v1_5_R1.Packet53BlockChange;
import net.minecraft.server.v1_5_R1.Packet54PlayNoteBlock;
import net.minecraft.server.v1_5_R1.Packet55BlockBreakAnimation;
import net.minecraft.server.v1_5_R1.Packet56MapChunkBulk;
import net.minecraft.server.v1_5_R1.Packet5EntityEquipment;
import net.minecraft.server.v1_5_R1.Packet60Explosion;
import net.minecraft.server.v1_5_R1.Packet61WorldEvent;
import net.minecraft.server.v1_5_R1.Packet62NamedSoundEffect;
import net.minecraft.server.v1_5_R1.Packet6SpawnPosition;
import net.minecraft.server.v1_5_R1.Packet70Bed;
import net.minecraft.server.v1_5_R1.Packet71Weather;
import net.minecraft.server.v1_5_R1.Packet7UseEntity;
import net.minecraft.server.v1_5_R1.Packet8UpdateHealth;
import net.minecraft.server.v1_5_R1.Packet9Respawn;
import net.minecraft.server.v1_5_R1.PlayerConnection;
import org.bukkit.entity.Player;

/**
 * This class is used to replace the original NetServerHandler and calling an
 * Events when receiving packets.
 */
public class CubePlayerConnection extends PlayerConnection
{
    private final Player player;
    private final PacketEventManager em;

    public CubePlayerConnection(Player bukkitPlayer, EntityPlayer player)
    {
        super(player.server, player.playerConnection.networkManager, player);
        this.player = bukkitPlayer;
        BukkitCore core = (BukkitCore)CubeEngine.getCore();
        this.em = core.getPacketEventManager();
    }

    /**
     * handles received packets
     *
     * @param packet the packet
     * @return true if the packet should be dropped
     */
    public boolean packetReceived(final Packet packet)
    {
        // System.out.println("Received: " + packet.k());
        return this.em.fireReceivedEvent(this.player, packet);
    }

    @Override
    public void sendPacket(final Packet packet)
    {
        // System.out.println("Sent: " + packet.k());
        if (this.em.fireSentEvent(this.player, packet))
        {
            return;
        }
        super.sendPacket(packet);
    }

    @Override
    public void a(Packet0KeepAlive packet0keepalive)
    {
        if (this.packetReceived(packet0keepalive))
            return;
        super.a(packet0keepalive);
    }

    @Override
    public void a(Packet1Login pl)
    {
        if (this.packetReceived(pl))
            return;
        super.a(pl);
    }

    @Override
    public void a(Packet2Handshake ph)
    {
        if (this.packetReceived(ph))
            return;
        super.a(ph);
    }

    @Override
    public void a(Packet3Chat packet3chat)
    {
        if (this.packetReceived(packet3chat))
            return;
        super.a(packet3chat);
    }

    @Override
    public void a(Packet4UpdateTime put)
    {
        if (this.packetReceived(put))
            return;
        super.a(put);
    }

    @Override
    public void a(Packet5EntityEquipment pee)
    {
        if (this.packetReceived(pee))
            return;
        super.a(pee);
    }

    @Override
    public void a(Packet6SpawnPosition psp)
    {
        if (this.packetReceived(psp))
            return;
        super.a(psp);
    }

    @Override
    public void a(Packet7UseEntity packet7useentity)
    {
        if (this.packetReceived(packet7useentity))
            return;
        super.a(packet7useentity);
    }

    @Override
    public void a(Packet8UpdateHealth puh)
    {
        if (this.packetReceived(puh))
            return;
        super.a(puh);
    }

    @Override
    public void a(Packet9Respawn packet9respawn)
    {
        if (this.packetReceived(packet9respawn))
            return;
        super.a(packet9respawn);
    }

    @Override
    public void a(Packet10Flying packet10flying)
    {
        if (this.packetReceived(packet10flying))
            return;
        super.a(packet10flying);
    }

    @Override
    public void a(Packet14BlockDig packet14blockdig)
    {
        if (this.packetReceived(packet14blockdig))
            return;
        super.a(packet14blockdig);
    }

    @Override
    public void a(Packet15Place packet15place)
    {
        if (this.packetReceived(packet15place))
            return;
        super.a(packet15place);
    }

    @Override
    public void a(Packet16BlockItemSwitch packet16blockitemswitch)
    {
        if (this.packetReceived(packet16blockitemswitch))
            return;
        super.a(packet16blockitemswitch);
    }

    @Override
    public void a(Packet17EntityLocationAction action)
    {
        if (this.packetReceived(action))
            return;
        super.a(action);
    }

    @Override
    public void a(Packet18ArmAnimation packet18armanimation)
    {
        if (this.packetReceived(packet18armanimation))
            return;
        super.a(packet18armanimation);
    }

    @Override
    public void a(Packet19EntityAction packet19entityaction)
    {
        if (this.packetReceived(packet19entityaction))
            return;
        super.a(packet19entityaction);
    }

    @Override
    public void a(Packet20NamedEntitySpawn pnes)
    {
        if (this.packetReceived(pnes))
            return;
        super.a(pnes);
    }

    @Override
    public void a(Packet22Collect pc)
    {
        if (this.packetReceived(pc))
            return;
        super.a(pc);
    }

    @Override
    public void a(Packet23VehicleSpawn pvs)
    {
        if (this.packetReceived(pvs))
            return;
        super.a(pvs);
    }

    @Override
    public void a(Packet24MobSpawn pms)
    {
        if (this.packetReceived(pms))
            return;
        super.a(pms);
    }

    @Override
    public void a(Packet25EntityPainting pep)
    {
        if (this.packetReceived(pep))
            return;
        super.a(pep);
    }

    @Override
    public void a(Packet26AddExpOrb paeo)
    {
        if (this.packetReceived(paeo))
            return;
        super.a(paeo);
    }

    @Override
    public void a(Packet28EntityVelocity pev)
    {
        if (this.packetReceived(pev))
            return;
        super.a(pev);
    }

    @Override
    public void a(Packet29DestroyEntity pde)
    {
        if (this.packetReceived(pde))
            return;
        super.a(pde);
    }

    @Override
    public void a(Packet30Entity pe)
    {
        if (this.packetReceived(pe))
            return;
        super.a(pe);
    }

    @Override
    public void a(Packet34EntityTeleport pet)
    {
        if (this.packetReceived(pet))
            return;
        super.a(pet);
    }

    @Override
    public void a(Packet35EntityHeadRotation pehr)
    {
        if (this.packetReceived(pehr))
            return;
        super.a(pehr);
    }

    @Override
    public void a(Packet38EntityStatus pes)
    {
        if (this.packetReceived(pes))
            return;
        super.a(pes);
    }

    @Override
    public void a(Packet39AttachEntity pae)
    {
        if (this.packetReceived(pae))
            return;
        super.a(pae);
    }

    @Override
    public void a(Packet40EntityMetadata pem)
    {
        if (this.packetReceived(pem))
            return;
        super.a(pem);
    }

    @Override
    public void a(Packet41MobEffect pme)
    {
        if (this.packetReceived(pme))
            return;
        super.a(pme);
    }

    @Override
    public void a(Packet42RemoveMobEffect prme)
    {
        if (this.packetReceived(prme))
            return;
        super.a(prme);
    }

    @Override
    public void a(Packet43SetExperience pse)
    {
        if (this.packetReceived(pse))
            return;
        super.a(pse);
    }

    @Override
    public void a(Packet51MapChunk pmc)
    {
        if (this.packetReceived(pmc))
            return;
        super.a(pmc);
    }

    @Override
    public void a(Packet52MultiBlockChange pmbc)
    {
        if (this.packetReceived(pmbc))
            return;
        super.a(pmbc);
    }

    @Override
    public void a(Packet53BlockChange pbc)
    {
        if (this.packetReceived(pbc))
            return;
        super.a(pbc);
    }

    @Override
    public void a(Packet54PlayNoteBlock ppnb)
    {
        if (this.packetReceived(ppnb))
            return;
        super.a(ppnb);
    }

    @Override
    public void a(Packet55BlockBreakAnimation pbba)
    {
        if (this.packetReceived(pbba))
            return;
        super.a(pbba);
    }

    @Override
    public void a(Packet56MapChunkBulk pmcb)
    {
        if (this.packetReceived(pmcb))
            return;
        super.a(pmcb);
    }

    @Override
    public void a(Packet60Explosion pe)
    {
        if (this.packetReceived(pe))
            return;
        super.a(pe);
    }

    @Override
    public void a(Packet61WorldEvent pwe)
    {
        if (this.packetReceived(pwe))
            return;
        super.a(pwe);
    }

    @Override
    public void a(Packet62NamedSoundEffect pnse)
    {
        if (this.packetReceived(pnse))
            return;
        super.a(pnse);
    }

    @Override
    public void a(Packet70Bed pb)
    {
        if (this.packetReceived(pb))
            return;
        super.a(pb);
    }

    @Override
    public void a(Packet71Weather pw)
    {
        if (this.packetReceived(pw))
            return;
        super.a(pw);
    }

    @Override
    public void a(Packet100OpenWindow pow)
    {
        if (this.packetReceived(pow))
            return;
        super.a(pow);
    }

    @Override
    public void a(Packet102WindowClick packet102windowclick)
    {
        if (this.packetReceived(packet102windowclick))
            return;
        super.a(packet102windowclick);
    }

    @Override
    public void a(Packet103SetSlot pss)
    {
        if (this.packetReceived(pss))
            return;
        super.a(pss);
    }

    @Override
    public void a(Packet104WindowItems pwi)
    {
        if (this.packetReceived(pwi))
            return;
        super.a(pwi);
    }

    @Override
    public void a(Packet105CraftProgressBar pcpb)
    {
        if (this.packetReceived(pcpb))
            return;
        super.a(pcpb);
    }

    @Override
    public void a(Packet106Transaction packet106transaction)
    {
        if (this.packetReceived(packet106transaction))
            return;
        super.a(packet106transaction);
    }

    @Override
    public void a(Packet107SetCreativeSlot packet107setcreativeslot)
    {
        if (this.packetReceived(packet107setcreativeslot))
            return;
        super.a(packet107setcreativeslot);
    }

    @Override
    public void a(Packet108ButtonClick packet108buttonclick)
    {
        if (this.packetReceived(packet108buttonclick))
            return;
        super.a(packet108buttonclick);
    }

    @Override
    public void a(Packet130UpdateSign packet130updatesign)
    {
        if (this.packetReceived(packet130updatesign))
            return;
        super.a(packet130updatesign);
    }

    @Override
    public void a(Packet131ItemData pid)
    {
        if (this.packetReceived(pid))
            return;
        super.a(pid);
    }

    @Override
    public void a(Packet132TileEntityData pted)
    {
        if (this.packetReceived(pted))
            return;
        super.a(pted);
    }

    @Override
    public void a(Packet200Statistic ps)
    {
        if (this.packetReceived(ps))
            return;
        super.a(ps);
    }

    @Override
    public void a(Packet201PlayerInfo ppi)
    {
        if (this.packetReceived(ppi))
            return;
        super.a(ppi);
    }

    @Override
    public void a(Packet202Abilities packet202abilities)
    {
        if (this.packetReceived(packet202abilities))
            return;
        super.a(packet202abilities);
    }

    @Override
    public void a(Packet203TabComplete packet203tabcomplete)
    {
        if (this.packetReceived(packet203tabcomplete))
            return;
        super.a(packet203tabcomplete);
    }

    @Override
    public void a(Packet204LocaleAndViewDistance packet204localeandviewdistance)
    {
        if (this.packetReceived(packet204localeandviewdistance))
            return;
        super.a(packet204localeandviewdistance);
    }

    @Override
    public void a(Packet205ClientCommand packet205clientcommand)
    {
        if (this.packetReceived(packet205clientcommand))
            return;
        super.a(packet205clientcommand);
    }

    @Override
    public void a(Packet250CustomPayload packet250custompayload)
    {
        if (this.packetReceived(packet250custompayload))
            return;
        super.a(packet250custompayload);
    }

    @Override
    public void a(Packet252KeyResponse pkr)
    {
        if (this.packetReceived(pkr))
            return;
        super.a(pkr);
    }

    @Override
    public void a(Packet253KeyRequest pkr)
    {
        if (this.packetReceived(pkr))
            return;
        super.a(pkr);
    }

    @Override
    public void a(Packet254GetInfo pgi)
    {
        if (this.packetReceived(pgi))
            return;
        super.a(pgi);
    }

    @Override
    public void a(Packet255KickDisconnect packet255kickdisconnect)
    {
        if (this.packetReceived(packet255kickdisconnect))
            return;
        super.a(packet255kickdisconnect);
    }
}
