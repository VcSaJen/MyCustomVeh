package com.vcsajen.mycustomveh.blocksel;

import com.flowpowered.math.vector.Vector3i;

/**
 * SAM interface for filling voxel ("callback")
 * Created by VcSaJen on 22.09.2016 17:48.
 */
public interface SetVoxel {
    void setVoxel(Vector3i coord, VoxelState state);
}
