package dev.gamblefx;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Config {
    private static class Data {
        boolean enabled = true;
        boolean sounds = true;
        boolean confetti = true;
        float speed = 1f;
        String style = "CRATE"; // CRATE | STRIP | SLOTS | MIXED
        Map<String, Boolean> categories = new LinkedHashMap<>();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Data d = new Data();
    private static Path file;

    private Config() {}

    public static void load() {
        file = FabricLoader.getInstance().getConfigDir().resolve("gamblefx.json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                Data loaded = GSON.fromJson(r, Data.class);
                if (loaded != null) d = loaded;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (d.categories == null) d.categories = new LinkedHashMap<>();
        for (Category c : Category.values()) d.categories.putIfAbsent(c.name(), true);
        save();
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(d, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isEnabled() { return d.enabled; }
    public static void setEnabled(boolean v) { d.enabled = v; }
    public static boolean isSounds() { return d.sounds; }
    public static void setSounds(boolean v) { d.sounds = v; }
    public static boolean isConfetti() { return d.confetti; }
    public static void setConfetti(boolean v) { d.confetti = v; }
    public static float getSpeed() { return d.speed <= 0 ? 1f : d.speed; }
    public static void setSpeed(float v) { d.speed = v; }
    public static String getStyleMode() { return d.style == null ? "CRATE" : d.style; }
    public static void setStyleMode(String s) { d.style = s; }

    public static boolean isCategoryEnabled(Category c) {
        Boolean b = d.categories.get(c.name());
        return b == null || b;
    }

    public static void setCategoryEnabled(Category c, boolean v) { d.categories.put(c.name(), v); }
}
