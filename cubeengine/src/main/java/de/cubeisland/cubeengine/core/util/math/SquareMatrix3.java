package de.cubeisland.cubeengine.core.util.math;

public class SquareMatrix3
{
    private final double[][] values;

    public SquareMatrix3(double c1r1, double c2r1, double c3r1,double c1r2, double c2r2, double c3r2, double c1r3, double c2r3, double c3r3 )
    {
        values = new double[][] 
        {
            { c1r1, c2r1, c3r1 },
            { c1r2, c2r2, c3r2 },
            { c1r3, c2r3, c3r3 }
        };
    }

    public double getValue(int column, int row)
    {
        return values[column][row];
    }
    
    public SquareMatrix3 multiply( SquareMatrix3 other )
    {
        return new SquareMatrix3
        ( 
                // col1
                this.values[0][0] * other.values[0][0] + this.values[0][1] * other.values[1][0] + this.values[0][2] * other.values[2][0],
                this.values[0][0] * other.values[0][1] + this.values[0][1] * other.values[1][1] + this.values[0][2] * other.values[2][1],
                this.values[0][0] * other.values[0][2] + this.values[0][1] * other.values[1][2] + this.values[0][2] * other.values[2][2],
                //col2
                this.values[1][0] * other.values[0][0] + this.values[1][1] * other.values[1][0] + this.values[1][2] * other.values[2][0],
                this.values[1][0] * other.values[0][1] + this.values[1][1] * other.values[1][1] + this.values[1][2] * other.values[2][1],
                this.values[1][0] * other.values[0][2] + this.values[1][1] * other.values[1][2] + this.values[1][2] * other.values[2][2],
                //col3
                this.values[2][0] * other.values[0][0] + this.values[2][1] * other.values[1][0] + this.values[2][2] * other.values[2][0],
                this.values[2][0] * other.values[0][1] + this.values[2][1] * other.values[1][1] + this.values[2][2] * other.values[2][1],
                this.values[2][0] * other.values[0][2] + this.values[2][1] * other.values[1][2] + this.values[2][2] * other.values[2][2]
        );
    }
    
    public Vector3 multiply( Vector3 other )
    {
        return this.multiply( other.x, other.y, other.z );
    }
    
    public Vector3 multiply(double x, double y, double z)
    {
        return new Vector3
        (
                this.values[0][0] * x + this.values[0][1] * y + this.values[0][2] * z,
                this.values[1][0] * x + this.values[1][1] * y + this.values[1][2] * z,
                this.values[2][0] * x + this.values[2][1] * y + this.values[2][2] * z 
        );
    }
}
