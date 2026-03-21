package com.toomda.parasitusfix.mixin;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SPacketSpawnPainting.class)
public abstract class MixinSPacketSpawnPainting {

    @Redirect(
            method = "readPacketData",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/PacketBuffer;readString(I)Ljava/lang/String;"
            )
    )
    private String parasitusfix$readLongerPaintingVariant(PacketBuffer buffer, int maxLength) {
        return buffer.readString(32);
    }
}
