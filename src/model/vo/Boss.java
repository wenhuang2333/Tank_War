package model.vo;

import thread.ai.AIController;

public class Boss extends Players {
    private AIController ai;

    public Boss() {
        super();
    }

    public Boss(TankData data, AIController ai) {
        super(data);
        this.ai = ai;
    }

    @Override
    public void update() {
        if (ai != null) {
            ai.decide(this);
        }
        super.update();
    }

    public AIController getAi() { return ai; }
    public void setAi(AIController ai) { this.ai = ai; }
}
