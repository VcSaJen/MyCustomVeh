package com.vcsajen.mycustomveh.dynblocks;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.world.World;

/**
 * Interface for all hierarchy elements who could have parent element.
 * Also have miscellaneous things for elements, like Update.
 * Created by VcSaJen on 19.09.2016 18:54.
 */
public interface ParentedElement {
    ChildedElement getParent();
    void setParent(ChildedElement elem);
    void updateTransformations();
    Vector3d getPosition();
    void setPosition(Vector3d position);
    Vector3d getRotation();
    void setRotation(Vector3d rotation);
    World getWorld();
}
