package name.Nofra.QuickClone;

import name.Nofra.QuickClone.KeyMaps.KeyMapsFactory;
import name.Nofra.QuickClone.events.ButtonClick;
import name.Nofra.QuickClone.events.WorldGarbageDetector;
import name.Nofra.QuickClone.configs.QuickCloneConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickClone implements ClientModInitializer {
    public static final String MOD_ID = "quickclone";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final QuickCloneConfig CONFIG = QuickCloneConfig.createAndLoad();

    @Override
    public void onInitializeClient() {
        KeyMapsFactory.init();

        ButtonClick.register();
        WorldGarbageDetector.register();
    }
}