package net.environmentz.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.api.Environment;
import net.environmentz.access.PlayerEnvAccess;
import net.environmentz.util.TemperatureHudRendering;
import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @Shadow
    @Final
    @Mutable
    private final MinecraftClient client;

    private int envTicker;
    private int xEnvPosition;
    private int yEnvPosition;
    private int extraEnvPosition;
    private int envIntensity;

    public InGameHudMixin(MinecraftClient client) {
        this.client = client;
    }

    @Inject(method = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(FLnet/minecraft/client/util/math/MatrixStack;)V"))
    private void renderMixin(MatrixStack matrixStack, float f, CallbackInfo info) {
        // Gets ticked 60 times per second
        PlayerEntity playerEntity = client.player;
        if (!playerEntity.isCreative() && !playerEntity.isSpectator() && !playerEntity.isInvulnerable()) {
            envTicker++;
            if (envTicker >= 60 && !client.isPaused()) {
                int playerTemperature = ((PlayerEnvAccess) playerEntity).getPlayerTemperature();
                if (playerTemperature != 0) {
                    if (playerTemperature < -30) {
                        if (playerTemperature < -120) {
                            xEnvPosition = 0;
                            yEnvPosition = 13;
                            envIntensity = Math.abs(playerTemperature + 120);
                        } else {
                            xEnvPosition = 26;
                            yEnvPosition = 0;
                            envIntensity = Math.abs(playerTemperature);
                        }
                    } else if (playerTemperature > 30) {
                        if (playerTemperature > 120) {
                            xEnvPosition = 0;
                            yEnvPosition = 26;
                            envIntensity = Math.abs(playerTemperature - 120);
                        } else {
                            xEnvPosition = 13;
                            yEnvPosition = 0;
                            envIntensity = Math.abs(playerTemperature);
                        }
                    } else {
                        xEnvPosition = 0;
                        yEnvPosition = 0;
                        envIntensity = 0;
                    }
                } else {
                    xEnvPosition = 0;
                    yEnvPosition = 0;
                    envIntensity = 0;
                }

                int playerWetIntensityValue = ((PlayerEnvAccess) playerEntity).getPlayerWetIntensityValue();
                if (playerWetIntensityValue <= 0)
                    extraEnvPosition = 0;
                else {
                    extraEnvPosition = playerWetIntensityValue;
                }
                envTicker = 0;
            }
            TemperatureHudRendering.renderPlayerEnvironmentIcon(matrixStack, client, playerEntity, xEnvPosition, yEnvPosition, extraEnvPosition, envIntensity);
        }
    }

}