package model.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import model.vo.PlayerSaveData;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class SaveManager {
    private static SaveManager instance;
    private static final String SAVE_VERSION = "1.0";
    public static final String DEFAULT_SAVE_DIR = "save/";
    private static final String DEFAULT_SAVE_FILE = "save_data.json";

    private Gson gson;
    private File currentSaveFile;

    private SaveManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        currentSaveFile = new File(DEFAULT_SAVE_DIR + DEFAULT_SAVE_FILE);
    }

    public static SaveManager getInstance() {
        if (instance == null) {
            synchronized (SaveManager.class) {
                if (instance == null) instance = new SaveManager();
            }
        }
        return instance;
    }

    public File getCurrentSaveFile() { return currentSaveFile; }
    public void setCurrentSaveFile(File f) { this.currentSaveFile = f; }

    public boolean save(PlayerSaveData data) {
        return saveAs(data, currentSaveFile);
    }

    public boolean saveAs(PlayerSaveData data, File file) {
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();

        File tempFile = new File(file.getParent(), file.getName() + ".tmp");
        File backupFile = new File(file.getParent(), file.getName() + ".bak");

        try {
            data.getMeta().setSaveTime(System.currentTimeMillis());
            String json = gson.toJson(data);
            Files.write(tempFile.toPath(), json.getBytes(StandardCharsets.UTF_8));

            if (file.exists()) {
                Files.move(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            currentSaveFile = file;
            return true;
        } catch (IOException e) {
            System.err.println("[ERROR] Save failed: " + e.getMessage());
            if (backupFile.exists()) {
                try {
                    Files.move(backupFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    System.err.println("[ERROR] Backup restore also failed: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            tempFile.delete();
        }
    }

    public PlayerSaveData load() {
        return load(currentSaveFile);
    }

    public PlayerSaveData load(File file) {
        try {
            String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            if (json.trim().isEmpty()) throw new IOException("Empty save file");

            JsonElement element;
            try {
                element = JsonParser.parseString(json);
            } catch (JsonSyntaxException e) {
                throw new IOException("JSON format error: " + e.getMessage());
            }

            if (!element.isJsonObject()) throw new IOException("Root element is not a JSON object");
            JsonObject root = element.getAsJsonObject();

            if (!root.has("meta") || !root.has("resources") || !root.has("ownedTanks")) {
                throw new IOException("Missing required fields (meta/resources/ownedTanks)");
            }

            String version = root.getAsJsonObject("meta").get("version").getAsString();
            if (!SAVE_VERSION.equals(version)) {
                System.err.println("[WARN] Save version mismatch, attempting migration");
            }

            PlayerSaveData data = gson.fromJson(root, PlayerSaveData.class);
            currentSaveFile = file;
            return data;
        } catch (IOException e) {
            System.err.println("[ERROR] Load failed: " + e.getMessage());
            int choice = JOptionPane.showOptionDialog(null,
                "存档文件损坏：\n" + file.getName() + "\n\n" + e.getMessage(),
                "存档加载失败",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"选择其他存档", "创建新档"},
                "选择其他存档");
            if (choice == 0) {
                return promptUserSelectSave();
            } else {
                String name = JOptionPane.showInputDialog("请输入玩家名：");
                if (name == null || name.trim().isEmpty()) name = "Player";
                return PlayerSaveData.createNew(name.trim());
            }
        }
    }

    private PlayerSaveData promptUserSelectSave() {
        List<File> files = listSaveFiles();
        if (files.isEmpty()) {
            String name = JOptionPane.showInputDialog("请输入玩家名：");
            if (name == null || name.trim().isEmpty()) name = "Player";
            return PlayerSaveData.createNew(name.trim());
        }
        return load(files.get(0));
    }

    public List<File> listSaveFiles() {
        List<File> result = new ArrayList<>();
        File dir = new File(DEFAULT_SAVE_DIR);
        if (!dir.exists() || !dir.isDirectory()) return result;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json") && !name.endsWith(".tmp") && !name.endsWith(".bak"));
        if (files != null) {
            for (File f : files) result.add(f);
        }
        return result;
    }

    public boolean hasExistingSave() {
        return currentSaveFile.exists();
    }

    public boolean deleteSave(File file) {
        return file.delete();
    }
}
