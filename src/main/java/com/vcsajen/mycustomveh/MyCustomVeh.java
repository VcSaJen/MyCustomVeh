package com.vcsajen.mycustomveh;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.vcsajen.mycustomveh.blocksel.Connectivity;
import com.vcsajen.mycustomveh.blocksel.FillResult;
import com.vcsajen.mycustomveh.blocksel.FloodFillSel;
import com.vcsajen.mycustomveh.blocksel.VoxelState;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.BodyPartRotationalData;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.property.BooleanProperty;
import org.spongepowered.api.data.property.DoubleProperty;
import org.spongepowered.api.data.property.block.BlastResistanceProperty;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.data.property.block.UnbreakableProperty;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.schematic.BlockPaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import static java.lang.Math.abs;

/**
 * Main plugin class
 * Created by VcSaJen on 02.09.2016 18:44.
 */
@Plugin(id = "mycustomveh", name = "MyCustomVeh", version = "1.0", authors = {"VcSaJen"},
        description = "Sponge plugin for making custom dynamic vehicles", dependencies = {})
public class MyCustomVeh {
    @Inject
    private Logger logger;

    @Inject
    private Game game;

    @Inject
    private PluginContainer myPlugin;

    private int floodFillTest = 0;
    private int floorLevel;

    private Schematic schematic;

    private boolean isWall(BlockState blockState) {
        if (blockState.getType().equals(BlockTypes.AIR)) return true;

        BlastResistanceProperty blastResist = blockState.getProperty(BlastResistanceProperty.class).orElse(null);
        if (blastResist!=null)
        {
            if (DoubleProperty.greaterThanOrEqual(10000000).matches(blastResist)) return true;
        }
        UnbreakableProperty unbreak = blockState.getProperty(UnbreakableProperty.class).orElse(null);
        if (unbreak!=null)
        {
            if (BooleanProperty.of(true).matches(unbreak)) return true;
        }
        MatterProperty matter = blockState.getProperty(MatterProperty.class).orElse(null);
        if (matter!=null)
        {
            if (matter.getValue() == MatterProperty.Matter.LIQUID || matter.getValue() == MatterProperty.Matter.GAS) return true;
        }

        return false;
    }

    private boolean inBounds(Vector3i pos, Vector3i bounds)
    {
        return abs(pos.getX()) < bounds.getX() &&
                abs(pos.getY()) < bounds.getY() &&
                abs(pos.getZ()) < bounds.getZ();
    }

    @Listener
    public void onInteractBlockSecondary(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        if (event.getInteractionPoint().isPresent())
        {
            if (floodFillTest==1)
            {
                floorLevel = event.getTargetBlock().getPosition().getY();
                floodFillTest = 2;
                player.sendMessage(Text.of("Right click to fill (copy)"));
                return;
            }
            if (floodFillTest==2)
            {
                floodFillTest=3;
                player.sendMessage(Text.of("Fill (cut) attempt..."));

                Vector3i maxSize = new Vector3i(20,20,20);
                int maxBlockCount = 512;

                Set<Vector3i> selectedBlocks = new HashSet<>(maxBlockCount);

                Vector3i seedBlockPos = event.getTargetBlock().getPosition();

                FloodFillSel ffs = new FloodFillSel();

                Connectivity conn = Connectivity.CONN6;

                FillResult fr = ffs.floodFill(seedBlockPos, maxSize, maxBlockCount, conn,
                        coord -> {
                            if (coord.getY()<=floorLevel || coord.getY()>254) return VoxelState.WALL;
                            if (selectedBlocks.contains(coord)) return VoxelState.FILLED;
                            if (!isWall(player.getWorld().getBlock(coord))) return VoxelState.EMPTY;
                            return VoxelState.WALL;
                        }, (coord, state) -> {
                            if (state==VoxelState.FILLED) {
                                selectedBlocks.add(coord);
                            }
                        });

                switch (fr) {
                    case SUCCESS:
                        ffs.getLastMin();
                        Vector3i center = ffs.getLastMax().sub(ffs.getLastMin()).div(2);
                        Vector3i actualSize = ffs.getLastMax().sub(ffs.getLastMin()).add(1,1,1);
                        Vector3i centerGlobal = center.add(ffs.getLastMin());
                        ArchetypeVolume archetypeVolume = game.getRegistry().getExtentBufferFactory().createArchetypeVolume(actualSize, center);
                        for (Vector3i coord: selectedBlocks) {
                            Vector3i relcoord = coord.sub(centerGlobal);
                            archetypeVolume.setBlock(relcoord, player.getWorld().getBlock(coord), Cause.source(myPlugin).build());
                            player.getWorld().getTileEntity(coord).ifPresent(tileEntity ->
                                    archetypeVolume.getTileEntityArchetypes().put(relcoord, tileEntity.createArchetype()));
                        }

                        schematic = Schematic.builder().volume(archetypeVolume).metaValue(Schematic.METADATA_AUTHOR, player.getName()).metaValue(Schematic.METADATA_NAME, "test").paletteType(BlockPaletteTypes.LOCAL).build();

                        try {
                            DataFormats.NBT.writeTo(new GZIPOutputStream(new FileOutputStream("test.schematic")), DataTranslators.SCHEMATIC.translate(schematic));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        player.sendMessage(Text.of(TextColors.GREEN, "Success!"));
                        player.sendMessage(Text.of("Right click to paste"));
                        break;
                    case TOO_MANY_BLOCKS:  player.sendMessage(Text.of(TextColors.RED, "Fail! Too many blocks!")); break;
                    case TOO_TALL:  player.sendMessage(Text.of(TextColors.RED, "Fail! Too tall!")); break;
                    case TOO_WIDE:  player.sendMessage(Text.of(TextColors.RED, "Fail! Too wide!")); break;
                }
                return;
            }
            if (floodFillTest==3)
            {
                floodFillTest=0;
                schematic.apply(player.getLocation().add(0,1,0), BlockChangeFlag.NONE, Cause.source(myPlugin).build());
                player.sendMessage(Text.of(TextColors.GREEN, "Pasted!"));
            }

            //noinspection OptionalGetWithoutIsPresent
            //player.sendMessage(Text.of(event.getInteractionPoint().get().mul(16).toString()));


            /*player.sendMessage(Text.of("Spawn attempt."));
            //noinspection OptionalGetWithoutIsPresent
            Location<World> spawnLocation = new Location<World>(player.getWorld(), event.getTargetBlock().getPosition().add(0.5,0.0,0.5));
            Extent extent = spawnLocation.getExtent();
            Collection<Entity> ec = extent.getEntities(entity -> (entity.getType()==EntityTypes.SHULKER)&&(entity.getLocation().getPosition().round().toInt().equals(spawnLocation.getPosition().round().toInt()) ));
            if (!ec.isEmpty())
            {
                Shulker sh = (Shulker)ec.toArray()[0];
                sh.remove();
            } else {
                Shulker hiblock = (Shulker)extent
                        .createEntity(EntityTypes.SHULKER, spawnLocation.getPosition());
                hiblock.tryOffer(Keys.VANISH, true);
                hiblock.tryOffer(Keys.AI_ENABLED, false);
                hiblock.tryOffer(Keys.GLOWING, true);

                player.sendMessage(Text.of(hiblock.supports(Keys.INVISIBLE) ? "Yes" : "No!"));

                //hiblock.tryOffer(Keys.GLOWING, true);
                boolean success = extent.spawnEntity(hiblock,
                        Cause.source(EntitySpawnCause.builder()
                                .entity(hiblock).type(SpawnTypes.PLUGIN).build()).build());

                if (!success) player.sendMessage(Text.of("fail!"));
            }*/
        }
    }

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        CommandSpec myCommandSpec = CommandSpec.builder()
                .description(Text.of("test"))
                .executor(this::cmdDbgArmorStandTest)
                .build();
        CommandSpec myCommandSpec2 = CommandSpec.builder()
                .description(Text.of("test"))
                .executor(this::cmdDbgFloodFillTest)
                .build();
        game.getCommandManager().register(this, myCommandSpec, "armorstandtest", "astest");
        game.getCommandManager().register(this, myCommandSpec2, "floodfilltest", "fftest");
    }

    private CommandResult cmdDbgFloodFillTest(CommandSource src, CommandContext commandContext) {
        if (src instanceof Player) {
            if (floodFillTest == 0)
            {
                floodFillTest = 1;
                src.sendMessage(Text.of("Right click on floor"));
                return CommandResult.success();
            }
            if (floodFillTest == 1)
            {

                return CommandResult.success();
            }
        }
        return CommandResult.success();
    }

    private CommandResult cmdDbgArmorStandTest(CommandSource src, CommandContext commandContext) {
        if(src instanceof Player) {
            Location<World> spawnLocation = ((Player) src).getLocation();
            Extent extent = spawnLocation.getExtent();
            Entity armst = extent
                    .createEntity(EntityTypes.ARMOR_STAND, spawnLocation.getPosition());

            /*Set<Key<?>> keys = armst.getKeys();
            for (Key<?> key: keys) {
                logger.debug(key.toString() + ": " + key.getValueClass().getSimpleName());
            }*/

            armst.tryOffer(Keys.ARMOR_STAND_HAS_BASE_PLATE, false);
            armst.tryOffer(Keys.ARMOR_STAND_HAS_GRAVITY, false);
            //armst.tryOffer(Keys.INVISIBLE, true);
            ((ArmorStand) armst).setHelmet(ItemStack.of(ItemTypes.BOOKSHELF,1));

            //noinspection OptionalGetWithoutIsPresent
            Map<BodyPart, Vector3d> d = armst.get(Keys.BODY_ROTATIONS).get();
            d.replace(BodyParts.HEAD, new Vector3d(0,0,45));
            armst.tryOffer(Keys.BODY_ROTATIONS, d);

            boolean success = extent.spawnEntity(armst,
                        Cause.source(EntitySpawnCause.builder()
                                .entity(armst).type(SpawnTypes.PLUGIN).build()).build());

            if (!success) src.sendMessage(Text.of("fail!"));
        }

        return CommandResult.success();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.debug("*************************");
        logger.debug("HI! MY PLUGIN IS WORKING!");
        logger.debug("*************************");
        logger.debug("Minecraft Version: " + game.getPlatform().getMinecraftVersion().getName());



    }

}
