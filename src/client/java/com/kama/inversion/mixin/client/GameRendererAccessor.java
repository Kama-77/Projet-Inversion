package com.kama.inversion.mixin.client;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

// Accessor vide - gardé pour la compatibilité mais plus utilisé
// La reflection dans ShaderUtil remplace les @Invoker et @Accessor
@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
}
