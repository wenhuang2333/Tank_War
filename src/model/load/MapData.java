package model.load;

import java.util.List;
import java.util.ArrayList;
import java.awt.Point;

public class MapData {
    private List<Point> brickPositions;
    private List<Point> ironPositions;
    private int width, height;

    public MapData() {
        brickPositions = new ArrayList<>();
        ironPositions = new ArrayList<>();
    }

    public static MapData parse(java.io.File file) {
        MapData data = new MapData();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eqIdx = line.indexOf('=');
                if (eqIdx < 0) continue;
                String type = line.substring(0, eqIdx).trim();
                String coords = line.substring(eqIdx + 1).trim();
                if (coords.isEmpty()) continue;

                String[] pairs = coords.split(";");
                for (String pair : pairs) {
                    pair = pair.trim();
                    if (pair.isEmpty()) continue;
                    String[] xy = pair.split(",");
                    if (xy.length != 2) continue;
                    try {
                        int x = Integer.parseInt(xy[0].trim());
                        int y = Integer.parseInt(xy[1].trim());
                        if ("BRICK".equalsIgnoreCase(type)) {
                            data.brickPositions.add(new Point(x, y));
                        } else if ("IRON".equalsIgnoreCase(type)) {
                            data.ironPositions.add(new Point(x, y));
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("[WARN] Map parse error: " + e.getMessage());
        }
        return data;
    }

    public static MapData createEmpty() {
        return new MapData();
    }

    public List<Point> getBrickPositions() { return brickPositions; }
    public List<Point> getIronPositions() { return ironPositions; }
}
