package smarthome.server;

import Smarthome.FurnaceStatus;
import Smarthome.GasFurnace;
import Smarthome.InvalidTemperature;
import Smarthome.TemperatureRange;
import com.zeroc.Ice.Current;

public class GasFurnaceI implements GasFurnace {
    private static final long serialVersionUID = -2448962912780867666L;
    private float curr_temp;
    private float desired_temp;
    private TemperatureRange tempRange;
    private boolean isFlame = true;

    public GasFurnaceI(TemperatureRange tempRange){
        this.tempRange = tempRange;
        curr_temp = tempRange.minTemp;
        desired_temp = (tempRange.maxTemp+tempRange.minTemp)/2;
        new Thread(this::update).start();
    }
    @Override
    public FurnaceStatus getStatus(Current current) {
        System.out.println("getStatus " + curr_temp + " " + desired_temp + " " + isFlame);
        return new FurnaceStatus(curr_temp, desired_temp, isFlame);
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
            if (isFlame){
                curr_temp = curr_temp + 1;
                isFlame = curr_temp < desired_temp;
            }
            else {
                curr_temp = curr_temp - 1;
                isFlame = curr_temp < desired_temp;
            }
        }
    }
}
