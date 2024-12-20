package org.nrnr.neverdies.impl.module.world;

import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.nrnr.neverdies.api.config.Config;
import org.nrnr.neverdies.api.config.setting.EnumConfig;
import org.nrnr.neverdies.api.config.setting.NumberConfig;
import org.nrnr.neverdies.api.event.listener.EventListener;
import org.nrnr.neverdies.api.module.ModuleCategory;
import org.nrnr.neverdies.api.module.ToggleModule;
import org.nrnr.neverdies.impl.event.network.PlayerTickEvent;
import org.nrnr.neverdies.impl.event.network.SetCurrentHandEvent;
import org.nrnr.neverdies.init.Managers;
import org.nrnr.neverdies.util.player.MovementUtil;
import org.nrnr.neverdies.util.string.EnumFormatter;

/**
 * @author xgraza/linus
 * @since 1.0
 */
public final class FastEatModule extends ToggleModule {
    Config<Mode> modeConfig = new EnumConfig<>("Mode", "The bypass mode", Mode.VANILLA, Mode.values());
    Config<Integer> ticksConfig = new NumberConfig<>("Ticks", "The amount of ticks to have 'consumed' an item before fast eating", 0, 10, 30);
    private int packets;

    public FastEatModule() {
        super("FastEat", "Allows you to consume items faster", ModuleCategory.WORLD);
    }

    @Override
    public String getModuleData() {
        return EnumFormatter.formatEnum(modeConfig.getValue());
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event) {
        if (MovementUtil.isMoving() || !mc.player.isOnGround()) {
            packets--;
            if (packets <= 0) {
                packets = 0;
            }
        } else {
            packets++;
            if (packets > 100) {
                packets = 100;
            }
        }

        if (!mc.player.isUsingItem()) {
            return;
        }
        final ItemStack stack = mc.player.getStackInHand(mc.player.getActiveHand());
        if (stack.isEmpty() || !stack.getItem().isFood()) {
            return;
        }

        final int timeUsed = mc.player.getItemUseTime();
        if (timeUsed >= ticksConfig.getValue()) {
            final int usePackets = 32 - timeUsed;
            for (int i = 0; i < usePackets; ++i) {
                switch (modeConfig.getValue()) {

                }
            }
        }
    }

    @EventListener
    public void onSetCurrentHand(SetCurrentHandEvent event) {
        if (modeConfig.getValue() == Mode.SHIFT) {
            ItemStack stack = event.getStackInHand();
            if (!stack.getItem().isFood() && !(stack.getItem() instanceof PotionItem)) {
                return;
            }
            int maxUseTime = stack.getMaxUseTime();
            if (packets < maxUseTime) {
                return;
            }
            for (int i = 0; i < maxUseTime; i++) {
                Managers.NETWORK.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(),
                        mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
                packets -= maxUseTime;
            }
            event.cancel();
            stack.getItem().finishUsing(stack, mc.world, mc.player);
        }
    }

    private enum Mode {
        VANILLA,
        SHIFT,
        GRIM
    }
}
