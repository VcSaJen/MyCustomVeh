package com.vcsajen.mycustomveh.dynblocks;

import com.flowpowered.math.vector.Vector3d;

/**
 * Controls single "block".
 * Needed coord transformations and existence checks are included.
 * Created by VcSaJen on 12.09.2016 18:01.
 */
public class DynBlockSingle implements ParentedElement {

    static final double v = 1;

    private ChildedElement parent;

    public Vector3d getCoord()
    {
        return null;
    }

    public void setCoord(Vector3d val)
    {

    }

    public Vector3d getRotation()
    {
        return null;
    }

    public void setRotation(Vector3d val)
    {

    }

    protected Vector3d getRotationInner()
    {
        return null;
    }

    protected void setRotationInner(Vector3d val)
    {

    }

    private static Vector3d convertCoordsBlockToAS(Vector3d blockCoord, Vector3d blockRotation)
    {
        Vector3d result = new Vector3d();
        return result;
    }

    @Override
    public ChildedElement getParent() {
        return parent;
    }

    @Override
    public void setParent(ChildedElement elem) {
        parent = elem;
    }

    @Override
    public void updateTransformations() {

    }
}
