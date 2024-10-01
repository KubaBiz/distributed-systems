package smarthome.server;

import Smarthome.TemperatureRange;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import java.util.Objects;

public class IceServer {
	public void t1(String[] args) {
		int status = 0;
		Communicator communicator = null;

		try {
			// 1. Inicjalizacja ICE - utworzenie communicatora
			communicator = Util.initialize(args);

			// 2. Konfiguracja adaptera
			// METODA 1 (polecana produkcyjnie): Konfiguracja adaptera Adapter1 jest w pliku konfiguracyjnym podanym jako parametr uruchomienia serwera
			ObjectAdapter adapter;
			// adapter = communicator.createObjectAdapter("Adapter1");
			if (Objects.equals(args[0], "1")) {
				adapter = communicator.createObjectAdapter("Smarthome1");
				adapter.add(new RefrigeratorI(new TemperatureRange(-5, 15)), new Identity("refrigerator1", "Refrigerator"));
				adapter.add(new RefrigeratorI(new TemperatureRange(-10, 10)), new Identity("refrigerator2", "Refrigerator"));
				adapter.add(new CoalFurnaceI(100), new Identity("coalfurnace1", "CoalFurnace"));
			}
			else if (Objects.equals(args[0], "2")) {
				adapter = communicator.createObjectAdapter("Smarthome2");
				adapter.add(new RefrigeratorI(new TemperatureRange(-15, 5)), new Identity("refrigerator3", "Refrigerator"));
				adapter.add(new BulbulatorI(), new Identity("bulbulator1", "Bulbulator"));
				adapter.add(new GasFurnaceI(new TemperatureRange(20, 80)), new Identity("gasfurnace1", "GasFurnace"));
			}
			else {
				status = 1;
				System.err.println("Specified instance of server unimplemented");
				System.exit(status);
				return;
			}

			// 3. Utworzenie serwanta/serwantów
			// CalcI calcServant1 = new CalcI();
			// CalcI calcServant2 = new CalcI();
			//CalcI calcServant3 = new CalcI();
			// 4. Dodanie wpisów do tablicy ASM, skojarzenie nazwy obiektu (Identity) z serwantem
			// adapter.add(calcServant1, new Identity("calc11", "calc"));
			// adapter.add(calcServant2, new Identity("calc22", "calc"));
			// adapter.add(calcServant1, new Identity("calc33", "calc"));
			//adapter.add(calcServant3, new Identity("calc33", "calc"));

			// 5. Aktywacja adaptera i wejście w pętlę przetwarzania żądań
			adapter.activate();

			System.out.println("Entering event processing loop...");

			communicator.waitForShutdown();

		} catch (Exception e) {
			e.printStackTrace(System.err);
			status = 1;
		}
		if (communicator != null) {
			try {
				communicator.destroy();
			} catch (Exception e) {
				e.printStackTrace(System.err);
				status = 1;
			}
		}
		System.exit(status);
	}


	public static void main(String[] args) {
		IceServer app = new IceServer();
		app.t1(args);
	}
}