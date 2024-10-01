package smarthome.server;

import Smarthome.InvalidTemperature;
import Smarthome.Refrigerator;
import Smarthome.TemperatureRange;
import com.zeroc.Ice.Current;

public class RefrigeratorI implements Refrigerator {
    private static final long serialVersionUID = -2448962912780867999L;
    private float curr_temp;
    private float desired_temp;
    private TemperatureRange tempRange;
    public RefrigeratorI(TemperatureRange tempRange){
        this.tempRange = tempRange;
        curr_temp = tempRange.minTemp;
        desired_temp = (tempRange.minTemp + tempRange.maxTemp)/2;
        new Thread(this::update).start();
    }
    @Override
    public float getTemperature(Current current) {
        System.out.println("getTemperature " + curr_temp);
        return curr_temp;
    }

    @Override
    public TemperatureRange getTempRange(Current current) {
        System.out.println("getTempRange " + tempRange);
        return tempRange;
    }

    @Override
    public void setTemperature(float temp, Current current) throws InvalidTemperature {
        System.out.println("setTemperature " + temp);
        if (temp < tempRange.minTemp || temp > tempRange.maxTemp){
            throw new InvalidTemperature("Temperature is not in range between (" + tempRange.minTemp + ", " + tempRange.maxTemp +")");
        }
        this.desired_temp = temp;
    }

    public void update(){
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (desired_temp > curr_temp) {
                curr_temp += 1;
            } else if (desired_temp < curr_temp) {
                curr_temp -= 1;
            }
        }
    }
}
