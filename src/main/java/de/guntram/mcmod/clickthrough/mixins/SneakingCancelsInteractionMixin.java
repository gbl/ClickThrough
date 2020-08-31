package de.guntram.mcmod.clickthrough.mixins;

import de.guntram.mcmod.clickthrough.ClickThrough;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)

public class SneakingCancelsInteractionMixin {
    @Inject(method="shouldCancelInteraction", at=@At("HEAD"), cancellable = true)
    private void noCancelWhenDyeing(CallbackInfoReturnable cir) {
        if (((Object) this) instanceof ClientPlayerEntity) {
            // System.out.println("on client");
            if (ClickThrough.isDyeOnSign) {
                // System.out.println("  not cancelling!");
                cir.setReturnValue(false);
                cir.cancel();
                ClickThrough.isDyeOnSign = false;
            }
        }
    }
}
