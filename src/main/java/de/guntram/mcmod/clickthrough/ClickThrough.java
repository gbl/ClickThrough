package de.guntram.mcmod.clickthrough;

import de.guntram.mcmod.fabrictools.ConfigurationProvider;
import java.util.Optional;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.StringRenderable;

public class ClickThrough implements ClientModInitializer 
{
    static public final String MODID="clickthrough";
    static public final String MODNAME="ClickThrough";

    @Override
    public void onInitializeClient() {
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
        ConfigurationProvider.register(MODNAME, confHandler);
        confHandler.load(ConfigurationProvider.getSuggestedFile(MODID));
    }
    
    static public boolean isDyeOnSign = false;
    static public boolean needToSneakAgain = false;
    
    public static String getSignRowText(SignBlockEntity sign, int row) {
        StringBuilder builder =  new StringBuilder();
        StringRenderable result = sign.getTextBeingEditedOnRow(row, (t) -> {
            return t;
        });

        result.visit((part) -> {
            builder.append(part);
            return Optional.empty();
        } );
        
        return builder.toString();
    }    
}
