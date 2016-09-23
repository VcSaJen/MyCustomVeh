package com.vcsajen.mycustomveh.blocksel;

/**
 * Determined how voxel's neighborhood work<BR/>
 * Created by VcSaJen on 23.09.2016 18:58.
 */
public enum Connectivity {
    /**
     * Every voxel is connected to 4 others by their faces
     */
    CONN6,
    /**
     * Every voxel is connected to 4 others by their faces,
     * 12 others by their edges
     */
    CONN18,
    /**
     * Every voxel is connected to 4 others by their faces,
     * 12 others by their edges,
     * 8 others by their corners
     */
    CONN26
}
