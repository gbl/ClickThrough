package de.guntram.mcmod.clickthrough;

import de.guntram.mcmod.crowdintranslate.CrowdinTranslate;
import de.guntram.mcmod.fabrictools.ConfigurationProvider;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.entity.SignBlockEntity;

public class ClickThrough implements ClientModInitializer 
{
    static public final String MODID="clickthrough";
    static public final String MODNAME="ClickThrough";

    @Override
    public void onInitializeClient() {
        CrowdinTranslate.downloadTranslations("clickthrough");
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
        ConfigurationProvider.register(MODNAME, confHandler);
        confHandler.load(ConfigurationProvider.getSuggestedFile(MODID));
    }
    
    static public boolean isDyeOnSign = false;
    static public boolean needToSneakAgain = false;
    
    public static String getSignRowText(SignBlockEntity sign, int row) {
        return sign.text[row].asString();
    }    
}
