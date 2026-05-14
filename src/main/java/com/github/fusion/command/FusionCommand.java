package com.github.fusion.command;

import com.github.fusion.FusionMod;
import com.github.fusion.data.ControlMode;
import com.github.fusion.data.FusionInstance;
import com.github.fusion.data.FusionManager;
import com.github.fusion.data.FusionOrientation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Random;

@EventBusSubscriber(modid = FusionMod.MOD_ID)
public class FusionCommand {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("fusion")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("fuse")
                        .then(Commands.argument("playerA", EntityArgument.player())
                                .then(Commands.argument("playerB", EntityArgument.player())

                                        .executes(ctx -> executeFuse(ctx, ControlMode.SHARED, null))

                                        .then(Commands.argument("mode", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String modeStr = StringArgumentType.getString(ctx, "mode");
                                                    ControlMode mode = ControlMode.byName(modeStr);
                                                    return executeFuse(ctx, mode, null);
                                                })

                                                .then(Commands.argument("orientation", StringArgumentType.word())
                                                        .executes(ctx -> {
                                                            String modeStr = StringArgumentType.getString(ctx, "mode");
                                                            String orientStr = StringArgumentType.getString(ctx, "orientation");
                                                            ControlMode mode = ControlMode.byName(modeStr);
                                                            FusionOrientation orient = FusionOrientation.byName(orientStr);
                                                            return executeFuse(ctx, mode, orient);
                                                        })
                                                )
                                        )
                                )
                        )
                )

                .then(Commands.literal("unfuse")
                        .executes(ctx -> executeUnfuseSelf(ctx))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    return executeUnfuse(ctx, target);
                                })
                        )
                )

                .then(Commands.literal("status")
                        .requires(source -> source.hasPermission(0))
                        .executes(ctx -> executeStatusSelf(ctx))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    return executeStatus(ctx, target);
                                })
                        )
                )

                .then(Commands.literal("setmode")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .executes(ctx -> {
                                    String modeStr = StringArgumentType.getString(ctx, "mode");
                                    ControlMode mode = ControlMode.byName(modeStr);
                                    return executeSetMode(ctx, mode);
                                })
                        )
                )
        );

        FusionMod.LOGGER.debug("Registered /fusion commands");
    }

    private static int executeFuse(CommandContext<CommandSourceStack> ctx,
                                    ControlMode mode, FusionOrientation orientation) {
        try {
            ServerPlayer playerA = EntityArgument.getPlayer(ctx, "playerA");
            ServerPlayer playerB = EntityArgument.getPlayer(ctx, "playerB");

            if (playerA == playerB) {
                ctx.getSource().sendFailure(Component.translatable("commands.fusion.fuse.same_player"));
                return 0;
            }

            FusionManager manager = FusionManager.get(playerA.serverLevel());

            if (manager.isPlayerFused(playerA.getUUID())) {
                ctx.getSource().sendFailure(Component.translatable("commands.fusion.fuse.already_fused",
                        playerA.getName()));
                return 0;
            }
            if (manager.isPlayerFused(playerB.getUUID())) {
                ctx.getSource().sendFailure(Component.translatable("commands.fusion.fuse.already_fused",
                        playerB.getName()));
                return 0;
            }

            if (orientation == null) {
                orientation = RANDOM.nextBoolean() ?
                        FusionOrientation.SIDE_BY_SIDE : FusionOrientation.BACK_TO_BACK;
            }

            FusionInstance instance = manager.fuse(playerA, playerB, mode, orientation);
            if (instance != null) {
                ctx.getSource().sendSuccess(() -> Component.translatable(
                        "commands.fusion.fuse.success",
                        playerA.getName(), playerB.getName()), true);
                return 1;
            } else {
                ctx.getSource().sendFailure(Component.literal("Failed to create fusion."));
                return 0;
            }
        } catch (Exception e) {
            FusionMod.LOGGER.error("Error executing fuse command", e);
            ctx.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeUnfuseSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Must be executed by a player."));
            return 0;
        }
        return executeUnfuse(ctx, player);
    }

    private static int executeUnfuse(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        FusionManager manager = FusionManager.get(target.serverLevel());

        if (!manager.isPlayerFused(target.getUUID())) {
            ctx.getSource().sendFailure(Component.translatable("commands.fusion.unfuse.not_fused",
                    target.getName()));
            return 0;
        }

        boolean success = manager.unfusePlayer(target.getUUID(), target.getServer());
        if (success) {
            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "commands.fusion.unfuse.success", target.getName()), true);
            return 1;
        }
        return 0;
    }

    private static int executeStatusSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Must be executed by a player."));
            return 0;
        }
        return executeStatus(ctx, player);
    }

    private static int executeStatus(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        FusionManager manager = FusionManager.get(target.serverLevel());
        FusionInstance instance = manager.getFusionForPlayer(target.getUUID());

        if (instance == null) {
            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "commands.fusion.status.not_fused", target.getName()), false);
        } else {
            var server = target.getServer();
            String partnerName = "?";
            if (server != null) {
                var partnerUUID = instance.getPartner(target.getUUID());
                if (partnerUUID != null) {
                    var partner = server.getPlayerList().getPlayer(partnerUUID);
                    if (partner != null) {
                        partnerName = partner.getName().getString();
                    }
                }
            }
            String finalPartnerName = partnerName;
            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "commands.fusion.status.fused",
                    target.getName(), finalPartnerName,
                    instance.getControlMode().getSerializedName(),
                    instance.getOrientation().getSerializedName()
            ), false);
        }
        return 1;
    }

    private static int executeSetMode(CommandContext<CommandSourceStack> ctx, ControlMode mode) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Must be executed by a player."));
            return 0;
        }

        FusionManager manager = FusionManager.get(player.serverLevel());
        FusionInstance instance = manager.getFusionForPlayer(player.getUUID());

        if (instance == null) {
            ctx.getSource().sendFailure(Component.translatable("commands.fusion.unfuse.not_fused",
                    player.getName()));
            return 0;
        }

        instance.setControlMode(mode);
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.fusion.setmode.success", mode.getSerializedName()), true);
        return 1;
    }
}
