package de.guntram.mcmod.clickthrough.mixins;

import de.guntram.mcmod.clickthrough.ClickThrough;
import de.guntram.mcmod.clickthrough.ConfigurationHandler;
import java.util.regex.Pattern;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ItemUseMixin {
    
    @Shadow public HitResult crosshairTarget;
    @Shadow public ClientPlayerEntity player;
    @Shadow public ClientWorld world;
    
    @Inject(method="doItemUse", at=@At(value="INVOKE",
            target="Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    public void switchCrosshairTarget(CallbackInfo ci) {
        ClickThrough.isDyeOnSign = false;
        if (crosshairTarget != null) {
            if (crosshairTarget.getType() == HitResult.Type.ENTITY && ((EntityHitResult)crosshairTarget).getEntity() instanceof ItemFrameEntity) {
                ItemFrameEntity itemFrame = (ItemFrameEntity) ((EntityHitResult)crosshairTarget).getEntity();
                // copied from AbstractDecorationEntity#canStayAttached
                BlockPos blockPos = itemFrame.getDecorationBlockPos().offset(itemFrame.getHorizontalFacing().getOpposite());
                BlockState state = itemFrame.world.getBlockState(blockPos);
                // System.out.println("Item frame attached to "+state.getBlock().getTranslationKey()+" at "+blockPos.toShortString());
                if (!player.isSneaking()) {
                    this.crosshairTarget = new BlockHitResult(crosshairTarget.getPos(), itemFrame.getHorizontalFacing(), blockPos, false);
                }
            }
            else if (crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult)crosshairTarget).getBlockPos();
                BlockState state = world.getBlockState(blockPos);
                Block block = state.getBlock();
                if (block instanceof WallSignBlock) {
                    WallSignBlock sign = (WallSignBlock) block;
                    BlockPos attachedPos = blockPos.offset(state.get(sign.FACING).getOpposite());
                    BlockEntity entity = world.getBlockEntity(blockPos);
                    if (!(entity instanceof SignBlockEntity)) {
                        return;
                    }
                    
                    for (int i=0; i<4; i++) {
                        Pattern pattern;
                        if ((pattern = ConfigurationHandler.getIgnorePattern(i)) != null) {
                            String signText = ClickThrough.getSignRowText((SignBlockEntity) entity, i);
                            if (pattern.matcher(signText).matches()) {
                                System.out.println("not clicking through because "+pattern+" matches "+signText);
                                return;
                            }
                        }
                    }

                    if (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof DyeItem) {
                        if (ConfigurationHandler.getSneakToDyeSigns()) {
                            ClickThrough.isDyeOnSign = true;                // prevent sneaking from cancelling the interaction
                            if (player.isSneaking()) {
                                // System.out.println("releasing shift");
                                ClickThrough.needToSneakAgain = true;
                                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                            } else {
                                this.crosshairTarget = new BlockHitResult(crosshairTarget.getPos(), ((BlockHitResult)crosshairTarget).getSide(), attachedPos, false);
                            }
                        } else {
                            // Don't switch the target; default action of dyeing the sign itself
                            return;
                        }
                    } else {
                        if (!player.isSneaking()) {
                            this.crosshairTarget = new BlockHitResult(crosshairTarget.getPos(), ((BlockHitResult)crosshairTarget).getSide(), attachedPos, false);
                        }
                    }
                }
            }
        }
    }
    
    @Inject(method="doItemUse", at=@At("RETURN"))
    public void reSneakIfNeccesary(CallbackInfo ci) {
        if (ClickThrough.needToSneakAgain) {
            // System.out.println("pressing shift");
            ClickThrough.needToSneakAgain = false;
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
    }
}
