package name.Nofra.QuickClone.events;

import name.Nofra.QuickClone.QuickClone;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorldGarbageDetector {

    public static void register() {
        final Path[] pendingDelete = {null};
        final Path[] pendingSwitch = {null};

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (!client.isInSingleplayer()) return;
            if (client.getServer() == null) return;

            Path worldPath = client.getServer()
                    .getSavePath(WorldSavePath.ROOT)
                    .normalize();

            if (ButtonClick.getBondedWorld(worldPath) == null) return;

            pendingDelete[0] = worldPath;
            pendingSwitch[0] = ButtonClick.getBondedWorld(worldPath);
            ButtonClick.removeBondedWorld(worldPath);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (pendingDelete[0] == null) return;

            if (QuickClone.CONFIG.deleteSaveWhenLeaving()) {
                try {
                    Files.walk(pendingDelete[0])
                            .sorted(java.util.Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                pendingDelete[0] = null;
            }

            if (QuickClone.CONFIG.redirectToOldSave()) {
                MinecraftClient client = MinecraftClient.getInstance();
                String worldName = pendingSwitch[0].getFileName().toString();
                client.execute(() -> {
                    client.createIntegratedServerLoader().start(worldName, () -> {
                    });
                });

                pendingSwitch[0] = null;
            }
        });
    }
}
