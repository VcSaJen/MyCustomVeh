package com.vcsajen.mycustomveh;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
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
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.BodyPartRotationalData;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.golem.Shulker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

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

    private boolean floodFillTest;

    @Listener
    public void onInteractBlockSecondary(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        if (event.getInteractionPoint().isPresent())
        {
            if (floodFillTest)
            {
                floodFillTest=false;
                player.sendMessage(Text.of("Fill attempt..."));
                final BlockState fillbs = BlockState.builder().blockType(BlockTypes.GOLD_BLOCK).build();
                final BlockState pickedbs = event.getTargetBlock().getState();

                if (fillbs.equals(pickedbs)) return;

                FloodFillSel ffs = new FloodFillSel();
                FillResult fr = ffs.floodFill(event.getTargetBlock().getPosition(), new Vector3i(5,10,5), 512,
                        coord -> {
                            if (coord.getY()<0 || coord.getY()>254) return VoxelState.WALL;
                            if (player.getWorld().getBlock(coord).equals(pickedbs)) return VoxelState.EMPTY;
                            if (player.getWorld().getBlock(coord).equals(fillbs)) return VoxelState.FILLED;
                            return VoxelState.WALL;
                        }, (coord, state) -> {
                            if (state==VoxelState.FILLED) player.getWorld().setBlock(coord, fillbs, Cause.source(myPlugin).build());
                        });

                switch (fr) {
                    case SUCCESS:  player.sendMessage(Text.of(TextColors.GREEN, "Success!")); break;
                    case TOO_MANY_BLOCKS:  player.sendMessage(Text.of(TextColors.RED, "Fail! Too many blocks!")); break;
                    case TOO_TALL:  player.sendMessage(Text.of(TextColors.RED, "Fail! Too tall!")); break;
                    case TOO_WIDE:  player.sendMessage(Text.of(TextColors.RED, "Fail! Too wide!")); break;
                }

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
        Sponge.getCommandManager().register(this, this.createArmourStandCommand, "createarmourstand");
    }

    private CommandResult cmdDbgFloodFillTest(CommandSource src, CommandContext commandContext) {
        if (src instanceof Player) {
            floodFillTest=true;
            src.sendMessage(Text.of("Right click to fill"));
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
            //((ArmorStand) armst).getBodyPartRotationalData().partRotation().put(BodyParts.HEAD, new Vector3d(0,0,90));
            BodyPartRotationalData data = Sponge.getDataManager().getManipulatorBuilder(BodyPartRotationalData.class).get().create();

            data.partRotation().putAll(Maps.asMap(Sets.newHashSet(Sponge.getRegistry().getAllOf(BodyPart.class)), k -> new Vector3d(45, 45, 45)));
            //data.partRotation().put(BodyParts.HEAD, new Vector3d(0,0,90));
            logger.debug(armst.tryOffer(data).toString());

            //((ArmorStand) armst).setHeadRotation(new Vector3d(1,1,1));
            //((ArmorStand) armst).getInventory().
            //if (optional.isPresent()) {
            boolean success = extent.spawnEntity(armst,
                        Cause.source(EntitySpawnCause.builder()
                                .entity(armst).type(SpawnTypes.PLUGIN).build()).build());
            //}

            if (!success) ((Player) src).sendMessage(Text.of("fail!"));
        }

        return CommandResult.success();
    }

    public final CommandCallable createArmourStandCommand = CommandSpec.builder()
            .description(Text.of("Creates a new armour stand in a random position"))
            .executor((src, args) -> {
                if (src instanceof Player) {
                    Player player = (Player) src;
                    World world = player.getWorld();

                    Entity entity = world.createEntity(EntityTypes.ARMOR_STAND, player.getLocation().getPosition());
                    BodyPartRotationalData data = Sponge.getDataManager().getManipulatorBuilder(BodyPartRotationalData.class).get().create();
                    Random random = new Random();
                    data.partRotation().putAll(Maps.asMap(Sets.newHashSet(Sponge.getRegistry().getAllOf(BodyPart.class)), k -> new Vector3d(random.nextDouble() * 360, random.nextDouble() * 360, random.nextDouble() * 360)));
                    System.out.println(entity.offer(data));

                    world.spawnEntity(entity, Cause.source(EntitySpawnCause.builder()
                            .entity(entity).type(SpawnTypes.PLUGIN).build()).build());
                }
                return CommandResult.success();
            })
            .build();

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.debug("*************************");
        logger.debug("HI! MY PLUGIN IS WORKING!");
        logger.debug("*************************");
        logger.debug("Minecraft Version: " + game.getPlatform().getMinecraftVersion().getName());
    }

}
