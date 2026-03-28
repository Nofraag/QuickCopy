package name.Nofra.QuickClone.events;

import name.Nofra.QuickClone.Creativesmind;
import name.Nofra.QuickClone.KeyMaps.KeyMapsFactory;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class ButtonClick {

    static HashMap<Path, Path> bondedWorld = new HashMap<Path, Path>();
    static Boolean confirmed = false;
    static int confirmTimer = 0;
    static boolean waitingForConfirm = false;
    static File levelData = null;
    static Path pendingNewSave = null;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (waitingForConfirm) {
                confirmTimer++;
                if (confirmTimer >= 100) {
                    waitingForConfirm = false;
                    confirmed = false;
                    confirmTimer = 0;
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("World switch cancelled."), false);
                    }
                }
            }

            while (KeyMapsFactory.SWITCH_TO_CREATIVE_WORLD.wasPressed()) {
                if (client.player == null) return;

                if (Creativesmind.CONFIG.confirmationPrompt() && !confirmed) {
                    waitingForConfirm = true;
                    confirmed = true;
                    confirmTimer = 0;
                    client.player.sendMessage(Text.literal("Press again to confirm switching worlds."), false);
                    return;
                }

                confirmed = false;
                waitingForConfirm = false;
                confirmTimer = 0;

                if (MinecraftClient.getInstance().getServer() == null) return;

                Path worldFolder = MinecraftClient.getInstance()
                        .getServer()
                        .getSavePath(WorldSavePath.ROOT)
                        .normalize();
                String originalName = worldFolder.getFileName().toString();
                Path newSave = worldFolder.getParent().resolve(originalName + "_creative");

                if (getBondedWorld(worldFolder) != null) {
                    client.player.sendMessage(Text.literal("Confirmation canceled, can't create BondedWorld while on a BondedWorld! if u wanna go back to ur old world just exit this world"), false);
                    return;
                }

                try {
                    Files.walk(worldFolder).forEach(source -> {
                        if (source.getFileName().toString().equals("session.lock")) return;

                        if (Creativesmind.CONFIG.brandNewWorld()) {
                            String name = source.getFileName().toString();
                            switch (name) {
                                case "region", "entities", "poi", "data" -> {
                                    return;
                                }
                            }
                            if (name.contains("DIM")) return;
                            if (name.endsWith(".mca")) return;
                        }

                        Path dest = newSave.resolve(worldFolder.relativize(source));
                        try {
                            if (!Files.exists(dest)) {
                                Files.copy(source, dest);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                File newSaveFile = newSave.toFile();
                File[] files = newSaveFile.listFiles();

                if (files == null) return;

                for (File file : files) {
                    if (file.getName().equals("level.dat")) {
                        levelData = file;
                        break;
                    }
                }

                if (levelData == null) return;

                MinecraftClient mClient = MinecraftClient.getInstance();

                bondedWorld.put(newSave, worldFolder);
                pendingNewSave = newSave;

                mClient.setScreen(null);
                mClient.world.disconnect(Text.of("Switching World"));
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (levelData == null || pendingNewSave == null) return;

            MinecraftClient mClient = MinecraftClient.getInstance();

            Path survivalLevelDat = bondedWorld.get(pendingNewSave).resolve("level.dat");

            NbtCompound root = null;
            try {
                root = NbtIo.readCompressed(
                        survivalLevelDat,
                        NbtSizeTracker.forLevel()
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            NbtCompound Data = root.getCompoundOrEmpty("Data");
            NbtCompound playerData = Data.getCompoundOrEmpty("Player");

            Data.putInt("GameType", Creativesmind.CONFIG.gameMode());
            Data.putInt("allowCommands", returnOpNumber());

            if (!playerData.isEmpty()) {
                playerData.putInt("playerGameType", Creativesmind.CONFIG.gameMode());
                Data.put("Player", playerData);
            }

            root.put("Data", Data);

            try {
                NbtIo.writeCompressed(root, levelData.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String worldName = pendingNewSave.getFileName().toString();

            mClient.execute(() -> {
                mClient.createIntegratedServerLoader().start(worldName, () -> {
                });
            });

            levelData = null;
            pendingNewSave = null;
        });
    }

    static int returnOpNumber() {
        return Creativesmind.CONFIG.giveOperator() ? 1 : 0;
    }

    public static Path getBondedWorld(Path cSave) {
        return bondedWorld.get(cSave);
    }

    public static void removeBondedWorld(Path cSave) {
        bondedWorld.remove(cSave);
    }
}