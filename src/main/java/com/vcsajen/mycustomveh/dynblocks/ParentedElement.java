package com.vcsajen.mycustomveh.dynblocks;

/**
 * Interface for all hierarchy elements who could have parent element.
 * Also have miscellaneous things for elements, like Update.
 * Created by VcSaJen on 19.09.2016 18:54.
 */
public interface ParentedElement {
    ChildedElement getParent();
    void setParent(ChildedElement elem);
    void updateTransformations();
}
