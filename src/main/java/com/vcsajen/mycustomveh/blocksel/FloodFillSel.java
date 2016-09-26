package com.vcsajen.mycustomveh.blocksel;

import com.flowpowered.math.vector.Vector3i;

import java.util.*;

/**
 * Flood fill for 3d <BR/>
 * WARNING: Do not execute while it is being executed <BR/>
 * WARNING: Does not revert image on fail. You must handle it yourself <BR/>
 * Created by VcSaJen on 20.09.2016 18:52.
 */
public class FloodFillSel {

    private static final Vector3i relFaces[] = {new Vector3i(0,1,0), new Vector3i(0,0,1), new Vector3i(0,-1,0), new Vector3i(0,0,-1)};
    private static final Vector3i relEdges[] = {new Vector3i(0,1,1), new Vector3i(0,-1,1), new Vector3i(0,-1,-1), new Vector3i(0,1,-1)};

    private Vector3i lastMin;
    private Vector3i lastMax;

    static {
        //relFaces = {new Vector3i(0,1,0)};

    }

    public Vector3i getLastMin()
    {
        return lastMin;
    }

    public Vector3i getLastMax()
    {
        return lastMax;
    }

    public FillResult floodFill(Vector3i seed, Vector3i maxSize, int maxVoxelCount, Connectivity connectivity, GetVoxel voxelGetter, SetVoxel voxelSetter)
    {
        List<Vector3i> relFacesAndEdges = new ArrayList<>(Arrays.asList(relFaces));
        if (connectivity == Connectivity.CONN18 || connectivity == Connectivity.CONN26)
            relFacesAndEdges.addAll(Arrays.asList(relEdges));

        Vector3i minCorner = new Vector3i(seed);
        Vector3i maxCorner = new Vector3i(seed);
        //boolean abort = false;

        int numFilled = 0;

        if (maxVoxelCount>maxSize.getX()*maxSize.getY()*maxSize.getZ()) maxVoxelCount=maxSize.getX()*maxSize.getY()*maxSize.getZ();

        Deque<Vector3i> stack = new ArrayDeque<>();
        stack.push(seed);
        while (!stack.isEmpty())
        {
            Vector3i pos = stack.pop();
            voxelSetter.setVoxel(pos, VoxelState.FILLED);
            numFilled++;
            //save the x coordinate of the seed
            Vector3i possave = pos.clone();
            // fill the right span of the seed
            pos=pos.add(1,0,0);
            while (voxelGetter.getVoxel(pos)==VoxelState.EMPTY)
            {
                voxelSetter.setVoxel(pos, VoxelState.FILLED);
                numFilled++;
                pos=pos.add(1,0,0);
            }
            maxCorner = pos.sub(1,0,0).max(maxCorner);
            // save the x coordinate of the extreme right voxel
            int xright = pos.getX() - 1;
            // reset the x coordinate to that of the seed
            pos = possave.clone();
            // fill the left span of the seed
            pos=pos.add(-1,0,0);

            while (voxelGetter.getVoxel(pos)==VoxelState.EMPTY)
            {
                voxelSetter.setVoxel(pos, VoxelState.FILLED);
                numFilled++;
                pos=pos.add(-1,0,0);
            }
            minCorner = pos.sub(-1,0,0).min(minCorner);
            // save the x coordinate of the extreme left voxel
            int xleft = pos.getX() + 1;

            pos=pos.sub(-1,0,0);

            //obligatory checks
            if (numFilled>maxVoxelCount) return FillResult.TOO_MANY_BLOCKS;
            Vector3i size = maxCorner.sub(minCorner).add(1,1,1);
            if (size.getX()>maxSize.getX() || size.getZ()>maxSize.getZ()) return FillResult.TOO_WIDE;
            if (size.getY()>maxSize.getY()) return FillResult.TOO_TALL;

            // check that the front scan line with 'y+1,z+1,y-1,z-1' is
            // neither a boundary nor the previously completely
            // filled one; if not, seed the scan line
            possave = pos;
            int i = 0;
            for (Vector3i relFace: relFacesAndEdges)
            {
                pos = possave;
                pos = pos.add(relFace);
                int ex=0; //used for checking more wide X range for CONN18 && CONN26
                if ((connectivity==Connectivity.CONN18 && i<relFaces.length) || connectivity==Connectivity.CONN26)
                    ex=1;
                // start at the left extreme of the scan line
                pos = new Vector3i(xleft-ex, pos.getY(), pos.getZ());
                // store the status of the first voxel
                boolean firstVoxelStatus = voxelGetter.getVoxel(pos)==VoxelState.EMPTY;
                pos = pos.add(1,0,0); //pos.x increased by 1;
                while (pos.getX()<=xright+ex)
                {
                    boolean secondVoxelStatus = voxelGetter.getVoxel(pos)==VoxelState.EMPTY;
                    //find the boundary between inside and outside voxels
                    if ((firstVoxelStatus != secondVoxelStatus) && firstVoxelStatus)
                        stack.push(pos.sub(1,0,0));
                    firstVoxelStatus=secondVoxelStatus;
                    pos = pos.add(1,0,0); //pos.x increased by 1;
                }
                // check the last voxel
                if (firstVoxelStatus) stack.push(pos.sub(1,0,0));
                i++;
            }

        }
        lastMin = minCorner;
        lastMax = maxCorner;
        return FillResult.SUCCESS;
    }

}
























