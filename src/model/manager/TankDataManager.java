package model.manager;

import model.vo.TankData;
import java.util.Map;
import java.util.HashMap;

public class TankDataManager {
    private static TankDataManager instance;
    private Map<Integer, TankData> tankDataMap;

    private TankDataManager() {
        tankDataMap = new HashMap<>();
        initTankData();
    }

    public static TankDataManager getInstance() {
        if (instance == null) {
            synchronized (TankDataManager.class) {
                if (instance == null) instance = new TankDataManager();
            }
        }
        return instance;
    }

    private void initTankData() {
        tankDataMap.put(1, new TankData(1, "克伦威尔", 420, 67, 30, 30, 10, 1, 30, 5, 5, 3, 880));
        tankDataMap.put(2, new TankData(2, "M24霞飞", 340, 30, 45, 50, 15, 1, 40, 6, 7, 1, 520));
        tankDataMap.put(3, new TankData(3, "谢尔曼萤火虫", 200, 120, 20, 55, 10, 1, 60, 4, 10, 3, 400));
        tankDataMap.put(4, new TankData(4, "约瑟夫IS", 450, 150, 100, 15, 2, 4, 25, 3, 3, 8, 300));
        tankDataMap.put(5, new TankData(5, "猎豹", 370, 250, 70, 60, 10, 3, 75, 6, 5, 4, 450));
        tankDataMap.put(6, new TankData(6, "T-34", 300, 40, 35, 70, 10, 1, 40, 5, 15, 5, 330));
        tankDataMap.put(7, new TankData(7, "豹式坦克", 400, 100, 50, 75, 10, 3, 100, 3, 4, 2, 540));
        tankDataMap.put(8, new TankData(8, "虎式坦克", 350, 80, 56, 65, 10, 2, 110, 4, 4, 2, 540));
    }

    public TankData getTankData(int id) {
        return tankDataMap.get(id);
    }

    public Map<Integer, TankData> getAllTankData() {
        return tankDataMap;
    }
}
