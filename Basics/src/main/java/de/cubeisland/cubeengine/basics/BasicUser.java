package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.basics.mail.Mail;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@SingleKeyEntity(tableName = "basicuser", primaryKey = "key", autoIncrement = false, indices = {
    @Index(value = FOREIGN_KEY, fields = "key", f_table = "user", f_field = "key")
})
public class BasicUser implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long key; // User Key
    @Attribute(type = AttrType.TIMESTAMP, notnull = false)
    public Timestamp muted;
    @Attribute(type = AttrType.BOOLEAN)
    public boolean godMode;
    public List<Mail> mailbox = new ArrayList<Mail>();

    public BasicUser()
    {}

    public BasicUser(User user)
    {
        this.key = user.getKey();
    }

    @Override
    public Long getKey()
    {
        return key;
    }

    @Override
    public void setKey(Long key)
    {
        throw new UnsupportedOperationException("Not supported. The BasicUserKey is final!");
    }
}
