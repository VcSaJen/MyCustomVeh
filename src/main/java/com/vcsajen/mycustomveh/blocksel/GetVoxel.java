package com.vcsajen.mycustomveh.blocksel;

import com.flowpowered.math.vector.Vector3i;

/**
 * SAM interface for checking voxel state ("callback") <BR/>
 * User MUST handle any coordinates! Return VoxelState.WALL for both actual walls and incorrect coordinates. <BR/>
 * Created by VcSaJen on 22.09.2016 17:48.
 */
public interface GetVoxel {
    VoxelState getVoxel(Vector3i coord);
}
