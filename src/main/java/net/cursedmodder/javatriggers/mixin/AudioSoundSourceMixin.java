package net.cursedmodder.javatriggers.mixin;

import net.cursedmodder.javatriggers.triggers.FoundationTriggerHandler;
import net.minecraft.client.sounds.MusicManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(MusicManager.class)
public class AudioSoundSourceMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cancelMusicControllerTick(CallbackInfo info) {
        if(!FoundationTriggerHandler.list.isEmpty()) info.cancel();
    }
}
