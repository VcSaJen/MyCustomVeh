package com.vcsajen.mycustomveh.dynblocks;

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.matrix.Matrix4d;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Controls single "block".
 * Needed coord transformations and existence checks are included.
 * Created by VcSaJen on 12.09.2016 18:01.
 */
public class DynBlockSingle implements ParentedElement {

    static final double v = 1;

    private ArmorStand armorStand;

    private ChildedElement parent;

    private final WeakReference<World> world;

    private Vector3d position;
    private Vector3d rotation;

    public BlockState getBlockState() {
        return blockState;
    }

    public void setBlockState(BlockState blockState) {
        this.blockState = blockState;
        ensureExistence();
        armorStand.setHelmet(ItemStack.builder().fromBlockState(blockState).quantity(1).build());
    }

    private BlockState blockState;

    private void ensureExistence()
    {
        if (armorStand==null || armorStand.isRemoved())
        {
            World curWorld = getWorld();
            if (curWorld!=null)
            {
                armorStand = (ArmorStand)curWorld.createEntity(EntityTypes.ARMOR_STAND, convertCoordsBlockToAS(position, rotation));
                armorStand.tryOffer(Keys.ARMOR_STAND_HAS_BASE_PLATE, false);
                armorStand.tryOffer(Keys.ARMOR_STAND_HAS_GRAVITY, false);
                armorStand.tryOffer(Keys.INVISIBLE, true);
                armorStand.setHelmet(ItemStack.builder().fromBlockState(blockState).quantity(1).build());

                boolean success = curWorld.spawnEntity(armorStand,
                        Cause.source(EntitySpawnCause.builder()
                                .entity(armorStand).type(SpawnTypes.PLUGIN).build()).build());
            }
        }
    }

    public DynBlockSingle(Location<World> pos, BlockState blockState) {
        world = new WeakReference<>(pos.getExtent());
        position = pos.getPosition();
        rotation = new Vector3d(0,0,0);
        this.blockState = blockState;
        ensureExistence();
        setPosition(pos.getPosition());
    }

    public Vector3d getPosition()
    {
        return position;
    }

    public World getWorld()
    {
        if (parent==null) return world.get();
        return parent.getWorld();
    }

    public void setPosition(Vector3d val)
    {
        ensureExistence();
        if (!armorStand.isRemoved()) {
            Vector3d coord = convertCoordsBlockToAS(val,rotation);
            //noinspection ConstantConditions
            armorStand.setLocation(new Location<>(getWorld(), coord));
        }
    }

    public Vector3d getRotation()
    {
        return rotation;
    }

    public void setRotation(Vector3d val)
    {
        ensureExistence();
        if (!armorStand.isRemoved()) {
            //noinspection ConstantConditions
            Map<BodyPart, Vector3d> bodyRotations = armorStand.get(Keys.BODY_ROTATIONS).get();
            bodyRotations.replace(BodyParts.HEAD, val);
            armorStand.tryOffer(Keys.BODY_ROTATIONS, bodyRotations);
            this.rotation=val;
            setPosition(position);
        }
    }

    private static Vector3d convertCoordsBlockToAS(Vector3d blockCoord, Vector3d blockRotation)
    {
        Vector3d result = new Vector3d(0,-0.25,0);
        Matrix4d transform = new Matrix4d(Matrix4d.IDENTITY);

        transform = transform.rotate(Quaterniond.fromAxesAnglesDeg(blockRotation.getX(), 0, 0));
        transform = transform.rotate(Quaterniond.fromAxesAnglesDeg(0, blockRotation.getY(), 0));
        transform = transform.rotate(Quaterniond.fromAxesAnglesDeg(0, 0, blockRotation.getZ()));

        transform = transform.translate(0,-1.4375,0);
        result = transform.transform(result.toVector4(1)).toVector3();
        result = result.mul(-1,1,1); // ХЗ почему
        return blockCoord.add(result);
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
