#ifndef SMARTH_ICE
#define SMARTH_ICE

module Smarthome
{
    exception InvalidTemperature {
        string message;
    }

    exception AboveMaxTemperature{
        string message;
    }

    struct TemperatureRange {
        float minTemp;
        float maxTemp;
    }

    struct FurnaceStatus {
        float currentTemp;
        float maxTemp;
        bool isFlame;
    }

    interface Refrigerator {
        idempotent float getTemperature();
        idempotent TemperatureRange getTempRange();
        void setTemperature(float temp) throws InvalidTemperature;
    }

    interface Bulbulator {
        idempotent bool isBulbing();
        void setBulbing(bool onOff);
    }

    interface Furnace {
        idempotent FurnaceStatus getStatus(); // get status of furnace
    }

    interface GasFurnace extends Furnace {
        idempotent TemperatureRange getTempRange(); // check which temperature you can set
        void setTemperature(float temp) throws InvalidTemperature; // set temperature
    }

    interface CoalFurnace extends Furnace {
        void setMaxTemperature(float temp) throws InvalidTemperature; // set max temperature
        bool isDispenserOn(); // check if dispenser is adding coal
        void turnDispenser() throws AboveMaxTemperature; // try to turn on/off dispenser
    }
};

#endif