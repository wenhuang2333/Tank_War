package model.manager;

import model.vo.SuperElement;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ElementManager {
    private static ElementManager instance;
    private Map<String, List<SuperElement>> elementMap;

    private ElementManager() {
        elementMap = new HashMap<>();
        elementMap.put("players", new CopyOnWriteArrayList<>());
        elementMap.put("boss", new CopyOnWriteArrayList<>());
        elementMap.put("bullet", new CopyOnWriteArrayList<>());
        elementMap.put("brick", new CopyOnWriteArrayList<>());
        elementMap.put("iron", new CopyOnWriteArrayList<>());
        elementMap.put("background", new CopyOnWriteArrayList<>());
        elementMap.put("explosion", new CopyOnWriteArrayList<>());
    }

    public static ElementManager getInstance() {
        if (instance == null) {
            synchronized (ElementManager.class) {
                if (instance == null) instance = new ElementManager();
            }
        }
        return instance;
    }

    public void addElement(String type, SuperElement e) {
        List<SuperElement> list = elementMap.get(type);
        if (list != null) list.add(e);
    }

    public void removeElement(String type, SuperElement e) {
        List<SuperElement> list = elementMap.get(type);
        if (list != null) list.remove(e);
    }

    public List<SuperElement> getElements(String type) {
        return elementMap.get(type);
    }

    public Map<String, List<SuperElement>> getAllElements() {
        return elementMap;
    }

    public void clearAll() {
        for (List<SuperElement> list : elementMap.values()) {
            list.clear();
        }
    }
}
