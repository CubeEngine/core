package de.cubeisland.cubeengine.core.util.math;

public class SquareMatrix3
{
    public final Vector3[] matrix;

    public SquareMatrix3( Vector3 col1, Vector3 col2, Vector3 col3 )
    {
        this.matrix = new Vector3[]
        {
            col1, col2, col3
        };
    }

    public SquareMatrix3 multiply( SquareMatrix3 other )
    {
        Vector3 col1 = new Vector3
        (
                this.matrix[0].x * other.matrix[0].x + this.matrix[1].x * other.matrix[0].y + this.matrix[2].x * other.matrix[0].z,
                this.matrix[0].y * other.matrix[0].x + this.matrix[1].y * other.matrix[0].y + this.matrix[2].y * other.matrix[0].z,
                this.matrix[0].z * other.matrix[0].x + this.matrix[1].z * other.matrix[0].y + this.matrix[2].z * other.matrix[0].z 
        );
        Vector3 col2 = new Vector3
        (
                this.matrix[0].x * other.matrix[1].x + this.matrix[1].x * other.matrix[1].y + this.matrix[2].x * other.matrix[1].z,
                this.matrix[0].y * other.matrix[1].x + this.matrix[1].y * other.matrix[1].y + this.matrix[2].y * other.matrix[1].z,
                this.matrix[0].z * other.matrix[1].x + this.matrix[1].z * other.matrix[1].y + this.matrix[2].z * other.matrix[1].z 
        );
        Vector3 col3 = new Vector3
        (
                this.matrix[0].x * other.matrix[2].x + this.matrix[1].x * other.matrix[2].y + this.matrix[2].x * other.matrix[2].z,
                this.matrix[0].y * other.matrix[2].x + this.matrix[1].y * other.matrix[2].y + this.matrix[2].y * other.matrix[2].z,
                this.matrix[0].z * other.matrix[2].x + this.matrix[1].z * other.matrix[2].y + this.matrix[2].z * other.matrix[2].z 
        );
        return new SquareMatrix3( col1, col2, col3);
    }
    
    public Vector3 multiply( Vector3 other )
    {
        return new Vector3
        (
                this.matrix[0].x * other.x + this.matrix[1].x * other.y + this.matrix[2].x * other.z,
                this.matrix[0].y * other.x + this.matrix[1].y * other.y + this.matrix[2].y * other.z,
                this.matrix[0].z * other.x + this.matrix[1].z * other.y + this.matrix[2].z * other.z 
        );
    }
}
