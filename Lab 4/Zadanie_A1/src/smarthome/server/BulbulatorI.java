package smarthome.server;

import Smarthome.Bulbulator;
import com.zeroc.Ice.Current;

public class BulbulatorI implements Bulbulator {
    private static final long serialVersionUID = -2448962912780867888L;
    private boolean bulbing = false;
    @Override
    public boolean isBulbing(Current current) {
        System.out.println("isBulbing "+ bulbing);
        return bulbing;
    }

    @Override
    public void setBulbing(boolean onOff, Current current) {
        System.out.println("setBulbing "+ onOff);
        bulbing = onOff;
    }
}
