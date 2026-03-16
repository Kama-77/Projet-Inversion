package com.kama.inversion.mixin.client;

import com.kama.inversion.InversionClientState;
import com.kama.inversion.render.InvertedRenderLayer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Unique
    private static final ThreadLocal<Boolean> kamas_isInvertedRender = ThreadLocal.withInitial(() -> false);

    @Inject(
        method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void kamas_inversion_mod$onRenderHead(
            LivingEntity entity,
            float yaw, float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci) {

        if (kamas_isInvertedRender.get()) return;

        if (entity instanceof AbstractClientPlayerEntity player
                && InversionClientState.isPlayerInverted(player.getUuid())) {

            ci.cancel();

            VertexConsumerProvider wrappedProvider = renderLayer ->
                    vertexConsumers.getBuffer(
                            InvertedRenderLayer.getInvertedEntityCutout(player.getSkinTexture())
                    );

            kamas_isInvertedRender.set(true);
            try {
                @SuppressWarnings("rawtypes")
                LivingEntityRenderer rawRenderer = (LivingEntityRenderer)(Object)this;
                //noinspection unchecked
                rawRenderer.render(entity, yaw, tickDelta, matrices, wrappedProvider, light);
            } finally {
                kamas_isInvertedRender.set(false);
            }
        }
    }
}
