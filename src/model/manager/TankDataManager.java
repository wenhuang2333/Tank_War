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
        tankDataMap.put(1, new TankData(1, "克伦威尔", 420, 67, 30, 30, 50, 1.0, 30, 5.0, 5, 3, 880));
        tankDataMap.put(2, new TankData(2, "M24霞飞", 340, 30, 45, 50, 55, 0.5, 40, 5.5, 7, 1, 520));
        tankDataMap.put(3, new TankData(3, "谢尔曼萤火虫", 200, 120, 20, 55, 50, 0.5, 60, 4.0, 10, 3, 400));
        tankDataMap.put(4, new TankData(4, "约瑟夫IS", 450, 150, 100, 15, 20, 4.0, 25, 3.0, 3, 8, 300));
        tankDataMap.put(5, new TankData(5, "猎豹", 370, 250, 70, 60, 60, 3.0, 75, 6.0, 5, 4, 450));
        tankDataMap.put(6, new TankData(6, "T-34", 300, 40, 35, 70, 50, 1.0, 40, 5.0, 15, 5, 330));
        tankDataMap.put(7, new TankData(7, "豹式坦克", 400, 100, 50, 75, 55, 3.0, 100, 3.0, 4, 2, 540));
        tankDataMap.put(8, new TankData(8, "虎式坦克", 350, 80, 56, 65, 55, 2.0, 110, 4.0, 4, 2, 540));
    }

    public TankData getTankData(int id) {
        return tankDataMap.get(id);
    }

    public Map<Integer, TankData> getAllTankData() {
        return tankDataMap;
    }
}
