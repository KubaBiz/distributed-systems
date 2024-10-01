import Ice
import sys
import Smarthome

class SmartHomeClient:
    def __init__(self):
        args = ["--Ice.Config=config.client"]
        self.communicator = Ice.initialize(args)
        self.refrigerators = {}
        self.bulbulators = {}
        self.furnaces = {}

        #first server
        try:
            base1 = self.communicator.stringToProxy("Refrigerator/refrigerator1:tcp -h 127.0.0.2 -p 10000 -z -t 10000 : udp -h 127.0.0.2 -p 10000 -z")
            base2 = self.communicator.stringToProxy("Refrigerator/refrigerator2:tcp -h 127.0.0.2 -p 10000 -z -t 10000 : udp -h 127.0.0.2 -p 10000 -z")
            base3 = self.communicator.stringToProxy("CoalFurnace/coalfurnace1:tcp -h 127.0.0.2 -p 10000 -z -t 10000 : udp -h 127.0.0.2 -p 10000 -z")

            self.refrigerator1 = Smarthome.RefrigeratorPrx.checkedCast(base1)
            self.refrigerator2 = Smarthome.RefrigeratorPrx.checkedCast(base2)
            self.coal_furnace1 = Smarthome.CoalFurnacePrx.checkedCast(base3)

            self.refrigerators["refrigerator1"] = self.refrigerator1
            self.refrigerators["refrigerator2"] = self.refrigerator2
            self.furnaces["coal_furnace1"] = self.coal_furnace1
        except Ice.Exception:
            print("Couldn't connect to the first server")
            self.close()
            sys.exit(1)

        #second server
        try:
            base4 = self.communicator.stringToProxy("Refrigerator/refrigerator3:tcp -h 127.0.0.3 -p 10000 -z -t 10000 : udp -h 127.0.0.3 -p 10000 -z")
            base5 = self.communicator.stringToProxy("Bulbulator/bulbulator1:tcp -h 127.0.0.3 -p 10000 -z -t 10000 : udp -h 127.0.0.3 -p 10000 -z")
            base6 = self.communicator.stringToProxy("GasFurnace/gasfurnace1:tcp -h 127.0.0.3 -p 10000 -z -t 10000 : udp -h 127.0.0.3 -p 10000 -z")

            self.refrigerator3 = Smarthome.RefrigeratorPrx.checkedCast(base4)
            self.bulbulator1 = Smarthome.BulbulatorPrx.checkedCast(base5)
            self.gas_furnace1 = Smarthome.GasFurnacePrx.checkedCast(base6)

            self.refrigerators["refrigerator3"] = self.refrigerator3
            self.bulbulators["bulbulator1"] = self.bulbulator1
            self.furnaces["gas_furnace1"] = self.gas_furnace1
        except Ice.Exception:
            print("Couldn't connect to the second server")
            self.close()
            sys.exit(1)

    def handle_refrigerator(self, refrigerator):
        while True:
            command = input("""status
get range
set temp [float]
exit
Choose command for """ + refrigerator + ": ")
            command = command.strip()
            if command == "status":
                print("----------------")
                print(refrigerator, "- current temperature:", self.refrigerators[refrigerator].getTemperature())
                print("----------------")
            elif command == "get range":
                print("----------------")
                message = self.refrigerators[refrigerator].getTempRange()
                print(refrigerator, " - temperature range: [", message.minTemp,", ", message.maxTemp, "]", sep="")
                print("----------------")
            elif command.startswith("set temp"):
                if len(command.split(" ")) < 3:
                    print("----------------")
                    print("No temperature provided")
                    print("----------------")
                    continue
                try:
                    number = float(command.split(" ")[2])
                except:
                    print("----------------")
                    print("Problem with converting third argument to number")
                    print("----------------")
                    continue
                try:
                    self.refrigerators[refrigerator].setTemperature(number)
                except Smarthome.InvalidTemperature as ex:
                    print("----------------")
                    print(ex.message)
                    print("----------------")
            elif command == "exit":
                break
            else:
                print("----------------")
                print("???")
                print("----------------")

    def handle_bulbulator(self, bulbulator):
        while True:
            command = input("""status
set bulbing [True/False]
exit
Choose command for """ + bulbulator + ": ")
            command = command.strip()
            if command == "status":
                print("----------------")
                print(bulbulator, "- is it bulbing:", self.bulbulators[bulbulator].isBulbing())
                print("----------------")
            elif command.startswith("set bulbing"):
                if len(command.split(" ")) < 3:
                    print("----------------")
                    print("No argument provided")
                    print("----------------")
                    continue
                try:
                    setting = {"true": True, "false": False}[command.split(" ")[2].lower()]
                except:
                    print("----------------")
                    print("Problem with converting third argument to boolean")
                    print("----------------")
                    continue
                self.bulbulators[bulbulator].setBulbing(setting)
                print("----------------")
            elif command == "exit":
                break
            else:
                print("----------------")
                print("???")
                print("----------------")

    def handle_furnace(self, furnace: str):
        if furnace.startswith("gas"):
            self.handle_gas_furnace(furnace)
        elif furnace.startswith("coal"):
            self.handle_coal_furnace(furnace)

    def handle_gas_furnace(self, gas_furnace):
        while True:
            command = input("""status
get range
set temp [float]
exit
Choose command for """ + gas_furnace + ": ")
            command = command.strip()
            if command == "status":
                message = self.furnaces[gas_furnace].getStatus()
                print("----------------")
                print(gas_furnace)
                print("Current temperature:", message.currentTemp)
                print("Maximum temperature:", message.maxTemp)
                print("Is flame present:", message.isFlame)
                print("----------------")
            elif command == "get range":
                print("----------------")
                message = self.furnaces[gas_furnace].getTempRange()
                print(gas_furnace, " - temperature range: [", message.minTemp,", ", message.maxTemp, "]", sep="")
                print("----------------")
            elif command.startswith("set temp"):
                if len(command.split(" ")) < 3:
                    print("----------------")
                    print("No temperature provided")
                    print("----------------")
                    continue
                try:
                    number = float(command.split(" ")[2])
                except:
                    print("----------------")
                    print("Problem with converting third argument to number")
                    print("----------------")
                    continue
                try:
                    self.furnaces[gas_furnace].setTemperature(number)
                except Smarthome.InvalidTemperature as ex:
                    print("----------------")
                    print(ex.message)
                print("----------------")
            elif command == "exit":
                break
            else:
                print("----------------")
                print("???")
                print("----------------")

    def handle_coal_furnace(self, coal_furnace):
        while True:
            command = input("""status
set max temp [float]
check dispenser
turn dispenser
exit
Choose command for """ + coal_furnace + ": ")
            command = command.strip()
            if command == "status":
                message = self.furnaces[coal_furnace].getStatus()
                print("----------------")
                print(coal_furnace)
                print("Current temperature:", message.currentTemp)
                print("Maximum temperature:", message.maxTemp)
                print("Is flame present:", message.isFlame)
                print("----------------")
            elif command.startswith("set max temp"):
                if len(command.split(" ")) < 4:
                    print("----------------")
                    print("No temperature provided")
                    print("----------------")
                    continue
                try:
                    number = float(command.split(" ")[3])
                except:
                    print("----------------")
                    print("Problem with converting fourth argument to number")
                    print("----------------")
                    continue
                try:
                    self.furnaces[coal_furnace].setMaxTemperature(number)
                except Smarthome.InvalidTemperature as ex:
                    print("----------------")
                    print(ex.message)
                    print("----------------")
            elif command == "check dispenser":
                print("----------------")
                print(coal_furnace, " - is dispenser on:", self.furnaces[coal_furnace].isDispenserOn())
                print("----------------")
            elif command == "turn dispenser":
                print("----------------")
                try:
                    message = self.furnaces[coal_furnace].turnDispenser()
                except Smarthome.AboveMaxTemperature as ex:
                    print("----------------")
                    print(ex.message)
                    print("----------------")
            elif command == "exit":
                break
            else:
                print("----------------")
                print("???")
                print("----------------")

    def close(self):
        if self.communicator:
            try:
                self.communicator.destroy()
            except Ice.Exception as ex:
                print(ex)

if __name__ == "__main__":
    client = SmartHomeClient()
    while True:
        command = input(
"""----------------
show devices
set [deviceName]
exit
Choose command: """)
        command = command.strip()
        if command == "show devices":
            print("----------------")
            print([key for key in client.refrigerators.keys()])
            print([key for key in client.bulbulators.keys()])
            print([key for key in client.furnaces.keys()])
        elif command.split(" ")[0] == "set":
            print("----------------")
            if len(command.split(" ")) < 2:
                print("No device name provided")
                continue
            device = command.split(" ")[1]
            if device in client.refrigerators.keys():
                client.handle_refrigerator(device)
            elif device in client.bulbulators.keys():
                client.handle_bulbulator(device)
            elif device in client.furnaces.keys():
                client.handle_furnace(device)
            else:
                print("Device not found")
                
        elif command == "exit":
            break
        else:
            print("----------------")
            print("???")


    client.close()