package org.cubeengine.libcube.util.math.shape;

import com.flowpowered.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CompositeShape implements Shape {

    // TODO implement me!!!

    private List<Shape> positiveShapes;
    private List<Shape> negativeShapes;

    public CompositeShape(Shape... positiveShapes)
    {
        this(Arrays.asList(positiveShapes), new ArrayList<>());
    }

    public CompositeShape(List<Shape> positiveShapes, List<Shape> negativeShapes)
    {
        this.positiveShapes = positiveShapes;
        this.negativeShapes = negativeShapes;
    }

    @Override
    public Shape setPoint(Vector3d point)
    {
        return this;
    }

    @Override
    public Vector3d getPoint()
    {
        return Vector3d.ZERO;
    }

    @Override
    public Shape rotate(Vector3d angle)
    {
        return null;
    }

    @Override
    public Shape setCenterOfRotation(Vector3d center)
    {
        return null;
    }

    @Override
    public Vector3d getRotationAngle()
    {
        return null;
    }

    @Override
    public Vector3d getCenterOfRotation()
    {
        return null;
    }

    @Override
    public Shape scale(Vector3d vector)
    {
        return null;
    }

    @Override
    public boolean contains(Vector3d point)
    {
        return false;
    }

    @Override
    public boolean contains(double x, double y, double z)
    {

        return false;
    }

    @Override
    public boolean intersects(Shape other)
    {
        return false;
    }

    @Override
    public boolean contains(Shape other)
    {
        return false;
    }

    @Override
    public Cuboid getBoundingCuboid()
    {
        return null;
    }

    @Override
    public Iterator<Vector3d> iterator()
    {
        return null;
    }
}
