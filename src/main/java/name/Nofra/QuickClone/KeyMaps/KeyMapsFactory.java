package name.Nofra.QuickClone.KeyMaps;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyMapsFactory {

    public static final KeyBinding.Category CATEGORY = new KeyBinding.Category(
            Identifier.of("creativesmind", "custom_category")
    );

    public static KeyBinding SWITCH_TO_CREATIVE_WORLD;

    public static void init() {
        SWITCH_TO_CREATIVE_WORLD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.creativesmind.switch",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY
        ));
    }

}