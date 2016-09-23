package com.vcsajen.mycustomveh.blocksel;

/**
 * Result of FloodFillSel
 * Created by VcSaJen on 23.09.2016 2:57.
 */
public enum FillResult {
    /**
     * Returned on success
     */
    SUCCESS,

    /**
     * Failure. Returned when fill region have more blocks than maxVoxelCount
     */
    TOO_MANY_BLOCKS,

    /**
     * Failure. Returned when fill region's width or depth is bigger than maxSize.getX() or maxSize.getZ()
     */
    TOO_WIDE,
    
    /**
     * Failure. Returned when fill region's height is bigger than maxSize.getY()
     */
    TOO_TALL
}
