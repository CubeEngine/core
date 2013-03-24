package de.cubeisland.cubeengine.vaultcompat;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;

import net.milkbowl.vault.chat.Chat;

public class VaultChatService extends net.milkbowl.vault.chat.Chat
{
    private final Vaultcompat compat;
    private final Chat chat;
    private final Roles roles;

    public VaultChatService(Vaultcompat compat, Chat chat, VaultRolesService rolesService)
    {
        super(rolesService);
        this.compat = compat;
        this.chat = chat;
        this.roles = rolesService.getRoles();
    }

    @Override
    public String getName()
    {
        return "CubeEngine:Chat";
    }

    @Override
    public boolean isEnabled()
    {
        return this.roles.isEnabled() && this.chat.isEnabled();
    }

    @Override
    public String getPlayerPrefix(String worldName, String playerName)
    {
        User user = this.compat.getUserManager().getUser(playerName);
        World world = this.compat.getCore().getServer().getWorld(worldName);

        return this.roles.getApi().getMetaData(user, world, "prefix");
    }

    @Override
    public void setPlayerPrefix(String worldName, String playerName, String newPrefix)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getPlayerSuffix(String worldName, String playerName)
    {
        User user = this.compat.getUserManager().getUser(playerName);
        World world = this.compat.getCore().getServer().getWorld(worldName);

        return this.roles.getApi().getMetaData(user, world, "suffix");
    }

    @Override
    public void setPlayerSuffix(String s, String s2, String s3)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getGroupPrefix(String s, String s2)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setGroupPrefix(String s, String s2, String s3)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getGroupSuffix(String s, String s2)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setGroupSuffix(String s, String s2, String s3)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getPlayerInfoInteger(String s, String s2, String s3, int i)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setPlayerInfoInteger(String s, String s2, String s3, int i)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getGroupInfoInteger(String s, String s2, String s3, int i)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setGroupInfoInteger(String s, String s2, String s3, int i)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double getPlayerInfoDouble(String s, String s2, String s3, double v)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setPlayerInfoDouble(String s, String s2, String s3, double v)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double getGroupInfoDouble(String s, String s2, String s3, double v)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setGroupInfoDouble(String s, String s2, String s3, double v)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean getPlayerInfoBoolean(String s, String s2, String s3, boolean b)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setPlayerInfoBoolean(String s, String s2, String s3, boolean b)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean getGroupInfoBoolean(String s, String s2, String s3, boolean b)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setGroupInfoBoolean(String s, String s2, String s3, boolean b)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getPlayerInfoString(String s, String s2, String s3, String s4)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setPlayerInfoString(String s, String s2, String s3, String s4)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getGroupInfoString(String s, String s2, String s3, String s4)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setGroupInfoString(String s, String s2, String s3, String s4)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
