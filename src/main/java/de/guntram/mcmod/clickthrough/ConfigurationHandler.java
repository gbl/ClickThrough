package de.guntram.mcmod.clickthrough;

import de.guntram.mcmod.fabrictools.ConfigChangedEvent;
import de.guntram.mcmod.fabrictools.Configuration;
import de.guntram.mcmod.fabrictools.ModConfigurationHandler;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigurationHandler implements ModConfigurationHandler {

    private static ConfigurationHandler instance;

    private Configuration config;
    private String configFileName;
    
    private boolean sneakToDyeSigns;
    
    private Pattern compiledPatterns[];
    private String patterns[];
    private static final String[] defaultPatterns = {
        "\\[\\D+\\]",
        "",
        "b\\s*\\d+|b\\s*\\d+\\s*:\\s*\\d+\\s*s|\\d+\\s*s",
        ""
    };
    private boolean onlyToContainers;
    
    public static ConfigurationHandler getInstance() {
        if (instance==null)
            instance=new ConfigurationHandler();
        return instance;
    }
    
    private ConfigurationHandler() {
        compiledPatterns = new Pattern[4];
        patterns = new String[4];
    }

    public void load(final File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            configFileName=configFile.getPath();
            loadConfig();
        }
    }
    
    @Override
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equalsIgnoreCase(ClickThrough.MODID)) {
            loadConfig();
        }
    }
    
    private void loadConfig() {
        
        sneakToDyeSigns=config.getBoolean("clickthrough.config.sneaktodye", Configuration.CATEGORY_CLIENT, true, "clickthrough.config.tt.sneaktodye");
        for (int i=0; i<4; i++) {
            patterns[i]=config.getString("clickthrough.config.ignore."+(i+1), Configuration.CATEGORY_CLIENT, defaultPatterns[i], "clickthrough.config.tt.ignore."+(i+1));
            try {
                if (patterns[i].isEmpty()) {
                    compiledPatterns[i] = null;
                } else {
                    compiledPatterns[i] = Pattern.compile(patterns[i], Pattern.CASE_INSENSITIVE);
                }
            } catch (PatternSyntaxException ex) {
                System.out.println("Pattern syntax exception with Pattern '"+patterns[i]+"' "+ex.getMessage());
                compiledPatterns[i] = null;
            }
        }
        onlyToContainers=config.getBoolean("clickthrough.config.onlycontainers", Configuration.CATEGORY_CLIENT, false, "clickthrough.config.tt.onlycontainers");
        
        if (config.hasChanged())
            config.save();
    }
    
    @Override
    public Configuration getConfig() {
        return config;
    }

    public static String getConfigFileName() {
        return getInstance().configFileName;
    }
    
    public static boolean getSneakToDyeSigns() {
        return getInstance().sneakToDyeSigns;
    }
    
    public static Pattern getIgnorePattern(int row) {
        return getInstance().compiledPatterns[row];
    }
    
    public static boolean onlyToContainers() {
        return getInstance().onlyToContainers;
    }
}
