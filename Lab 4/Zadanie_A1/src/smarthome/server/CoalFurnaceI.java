package smarthome.server;

import Smarthome.AboveMaxTemperature;
import Smarthome.CoalFurnace;
import Smarthome.FurnaceStatus;
import Smarthome.InvalidTemperature;
import com.zeroc.Ice.Current;

public class CoalFurnaceI implements CoalFurnace {
    private static final long serialVersionUID = -2448962912780867777L;
    private float curr_temp = 10f;
    private float max_temp = 80f;
    private float limit;
    private boolean isFlame = true;

    private int fuel = 15;
    private boolean dispenserOn = true;

    public CoalFurnaceI(float limit){
        this.limit = limit;
        new Thread(this::update).start();
    }

    @Override
    public FurnaceStatus getStatus(Current current) {
        System.out.println("getStatus " + curr_temp + " " + max_temp + " " + isFlame);
        return new FurnaceStatus(curr_temp, max_temp, isFlame);
    }

    @Override
    public void setMaxTemperature(float temp, Current current) throws InvalidTemperature {
        System.out.println("setMaxTemperature " + temp);
        if (temp > limit){
            throw new InvalidTemperature("Max temperature cannot exceed limit set by producer");
        }
        max_temp = temp;
    }

    @Override
    public boolean isDispenserOn(Current current) {
        System.out.println("isDispenserOn " + dispenserOn);
        return dispenserOn;
    }

    @Override
    public void turnDispenser(Current current) throws AboveMaxTemperature {
        System.out.println("turnDispenser " + dispenserOn);
        if (curr_temp > max_temp && !dispenserOn){
            throw new AboveMaxTemperature("Current temperature exceeds maximum temperature. Dispenser remains off");
        }
        dispenserOn = !dispenserOn;
    }

    public void update(){
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (curr_temp > max_temp){
                curr_temp -= 1;
                continue;
            }
            if (isFlame){
                if (dispenserOn){
                    fuel = 15;
                    curr_temp = curr_temp < max_temp ? curr_temp+1 : curr_temp;
                }
                else if (fuel > 0) {
                    fuel -= 1;
                    curr_temp = curr_temp < max_temp ? curr_temp+1 : curr_temp;
                }
                else {
                    isFlame = false;
                }
            }
            else {
                curr_temp = curr_temp > 0 ? curr_temp-1 : curr_temp;
            }
        }

    }
}
