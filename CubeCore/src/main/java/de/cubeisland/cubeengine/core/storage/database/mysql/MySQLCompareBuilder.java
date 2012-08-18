package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.CompareBuilder;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLCompareBuilder<K extends CompareBuilder> extends MySQLBuilderBase implements CompareBuilder<K>
{
    private int subDepth = 0;
    protected MySQLConditionalBuilder parent;

    public MySQLCompareBuilder(MySQLConditionalBuilder parent)
    {
        super();
        this.parent = parent;
        this.query = null;
    }

    public K field(String col)
    {
        this.query.append(this.prepareName(col, false));
        return (K)this;
    }

    public K is(int operation)
    {
        switch (operation)
        {
            case 1:
                this.query.append('=');
                break;
            case 2:
                this.query.append("!=");
                break;
            case 3:
                this.query.append('<');
                break;
            case 4:
                this.query.append("<=");
                break;
            case 5:
                this.query.append('>');
                break;
            case 6:
                this.query.append(">=");
                break;
            default:
                throw new IllegalStateException("Invalid operation");
        }
        return (K)this;
    }

    public K value()
    {
        this.query.append('?');
        return (K)this;
    }

    public K not()
    {
        this.query.append(" NOT");
        return (K)this;
    }

    public K and()
    {
        this.query.append(" AND");
        return (K)this;
    }

    public K or()
    {
        this.query.append(" OR");
        return (K)this;
    }

    public K beginSub()
    {
        this.query.append('(');
        ++this.subDepth;
        return (K)this;
    }

    public K endSub()
    {
        if (this.subDepth > 0)
        {
            this.query.append(')');
            --this.subDepth;
        }
        return (K)this;
    }
}
