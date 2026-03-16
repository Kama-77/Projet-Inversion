package com.kama.inversion.render;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceFactory;

import java.io.IOException;

public final class InvertedShaders {

    private static ShaderProgram invertedEntityCutoutShader;

    private InvertedShaders() {}

    public static ShaderProgram getInvertedEntityCutoutShader() {
        return invertedEntityCutoutShader;
    }

    public static void registerShaders(ResourceFactory factory) throws IOException {
        invertedEntityCutoutShader = new ShaderProgram(
            factory,
            "rendertype_entity_cutout_inverted",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        );
    }
}
