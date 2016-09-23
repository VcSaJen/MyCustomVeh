package com.vcsajen.mycustomveh.dynblocks;

import com.flowpowered.math.matrix.Matrix4d;

import java.util.List;

/**
 * Interface for all hierarchy elements who could have child elements.
 * Created by VcSaJen on 19.09.2016 19:10.
 */
public interface ChildedElement extends ParentedElement {
    Matrix4d getTransformation();
    void setTransformation(Matrix4d transformation);
    List<ParentedElement> getChildren();
}
