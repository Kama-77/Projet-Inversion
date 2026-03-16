package com.kama.inversion.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public final class InvertedRenderLayer {

    private static final Function<Identifier, RenderLayer> INVERTED_ENTITY_CUTOUT =
        Util.memoize(texture -> {
            RenderLayer.MultiPhaseParameters params = RenderLayer.MultiPhaseParameters.builder()
                .program(new RenderPhase.ShaderProgram(InvertedShaders::getInvertedEntityCutoutShader))
                .texture(new RenderPhase.Texture(texture, false, false))
                .transparency(RenderPhase.NO_TRANSPARENCY)
                .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                .build(false);

            return RenderLayer.of(
                "kamas_inverted_entity_cutout",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS,
                256,
                true,
                false,
                params
            );
        });

    public static RenderLayer getInvertedEntityCutout(Identifier texture) {
        return INVERTED_ENTITY_CUTOUT.apply(texture);
    }

    private InvertedRenderLayer() {}
}
