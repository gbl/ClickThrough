package de.guntram.mcmod.clickthrough;

import de.guntram.mcmod.crowdintranslate.CrowdinTranslate;
import de.guntram.mcmod.fabrictools.ConfigurationProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F9;

public class ClickThrough implements ClientModInitializer 
{
    static public final String MODID="clickthrough";
    static public final String MODNAME="ClickThrough";

    static public boolean isActive;
    private KeyBinding onoff;

    @Override
    public void onInitializeClient() {
        CrowdinTranslate.downloadTranslations("clickthrough");
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
        ConfigurationProvider.register(MODNAME, confHandler);
        confHandler.load(ConfigurationProvider.getSuggestedFile(MODID));
        setKeyBinding();
        registerCommands();
        isActive = true;
    }
    
    static public boolean isDyeOnSign = false;
    static public boolean needToSneakAgain = false;
    
    public static String getSignRowText(SignBlockEntity sign, int row) {
        StringBuilder builder =  new StringBuilder();
        return sign.getTextOnRow(row, true).getString();
    }

    private void setKeyBinding() {
        final String category = "key.categories.clickthrough";
        KeyBindingHelper.registerKeyBinding(onoff = new KeyBinding("key.clickthrough.toggle", InputUtil.Type.KEYSYM, GLFW_KEY_F9, category));
        ClientTickEvents.END_CLIENT_TICK.register(e -> processKeyBind());
    }

    public void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                literal("clt")
                    .then(
                        literal("on").executes(c -> {
                            setActive();
                            return 1;
                        })
                    )
                    .then(
                        literal("off").executes(c -> {
                            setInActive();
                            return 1;
                        })
                    )
            );
        });
    }

    private void processKeyBind() {
        if (onoff.wasPressed()) {
            if (isActive) {
                setInActive();
            } else {
                setActive();
            }
        }
    }
    public void setActive() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.sendMessage(Text.translatable("clickthrough.msg.active"), false);
        }
        isActive = true;
    }

    public void setInActive() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.sendMessage(Text.translatable("clickthrough.msg.inactive"), false);
        }
        isActive = false;
    }
}
