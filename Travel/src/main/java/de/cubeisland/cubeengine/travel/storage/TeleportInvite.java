package de.cubeisland.cubeengine.travel.storage;

import de.cubeisland.cubeengine.core.storage.TwoKeyModel;
import de.cubeisland.cubeengine.core.storage.database.*;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

import java.util.List;

@TwoKeyEntity(tableName = "teleportinvites", firstPrimaryKey = "teleportpoint", secondPrimaryKey = "userkey",
    indices = {
            @Index(value = Index.IndexType.FOREIGN_KEY, fields = "teleportpoint", f_table = "teleportpoints", f_field = "key"),
            @Index(value = Index.IndexType.FOREIGN_KEY, fields = "userkey", f_table = "user", f_field = "key")
    })
public class TeleportInvite implements TwoKeyModel<Long, Long>
{
    @Attribute(type = AttrType.INT, unsigned = true, name = "teleportpoint")
    public Long teleportPoint;
    @Attribute(type = AttrType.INT, unsigned = true, name = "userkey")
    public Long userKey;

    @DatabaseConstructor
    public TeleportInvite(List<Object> args) throws ConversionException
    {
        this.teleportPoint = Long.valueOf(args.get(0).toString());
        this.userKey = Long.valueOf(args.get(1).toString());
    }

    public TeleportInvite(Long teleportPoint, Long userKey)
    {
        this.teleportPoint = teleportPoint;
        this.userKey = userKey;
    }

    public boolean semiEquals(TeleportInvite tpI)
    {
        return tpI.teleportPoint == this.teleportPoint && tpI.userKey == this.userKey;
    }

    @Override
    public Pair<Long, Long> getKey()
    {
        return new Pair<Long, Long>(teleportPoint, userKey);
    }

    @Override
    public void setKey(Pair<Long, Long> key)
    {
        this.teleportPoint = key.getLeft();
        this.userKey = key.getRight();
    }
}
