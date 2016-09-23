package com.vcsajen.mycustomveh.blocksel;

/**
 * Possible state of voxel. <BR/>
 * As abstract as possible; what states actually mean is defined by user in callbacks. <BR/>
 * Created by VcSaJen on 22.09.2016 18:07.
 */
public enum VoxelState {
    /**
     * Yet unfilled voxel
     */
    EMPTY,

    /**
     * Wall voxel
     */
    WALL,

    /**
     * Already filled voxel
     */
    FILLED
}
