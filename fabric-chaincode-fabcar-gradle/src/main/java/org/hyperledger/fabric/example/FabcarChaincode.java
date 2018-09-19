package org.hyperledger.fabric.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.handler.ssl.OpenSsl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

public class FabcarChaincode extends ChaincodeBase {

	private static Log _logger = LogFactory.getLog(FabcarChaincode.class);

	private Gson gson;

	public FabcarChaincode() {
		this.gson = new GsonBuilder().create();
	}

	/**
	 * The Init method is called when the Smart Contract 'fabcar' is instantiated by the
	 * blockchain network Best practice is to have any Ledger initialization in separate
	 * function -- see initLedger()
	 * 
	 * @see org.hyperledger.fabric.shim.ChaincodeBase#init(org.hyperledger.fabric.shim.ChaincodeStub)
	 */
	@Override
	public Response init(ChaincodeStub stub) {
		try {
			_logger.info("=========== Instantiated fabcar chaincode ===========");
			return newSuccessResponse();
		}
		catch (Throwable e) {
			return newErrorResponse(e);
		}
	}

	/**
	 * The Invoke method is called as a result of an application request to run the Smart
	 * Contract 'fabcar'. The calling application program has also specified the
	 * particular smart contract function to be called, with arguments
	 * 
	 * @see org.hyperledger.fabric.shim.ChaincodeBase#invoke(org.hyperledger.fabric.shim.ChaincodeStub)
	 */
	@Override
	public Response invoke(ChaincodeStub stub) {
		try {
			_logger.info("Invoke java simple chaincode");
			String func = stub.getFunction();
			List<String> params = stub.getParameters();

			if (func.equals("queryCar")) {
				return queryCar(stub, params);
			}
			if (func.equals("initLedger")) {
				return initLedger(stub, params);
			}
			if (func.equals("createCar")) {
				return createCar(stub, params);
			}
			if (func.equals("queryAllCars")) {
				return queryAllCars(stub, params);
			}
			if (func.equals("changeCarOwner")) {
				return changeCarOwner(stub, params);
			}

			return newErrorResponse("Received unknown function " + func + " invocation");
		}
		catch (Throwable e) {
			return newErrorResponse(e);
		}
	}

	private Response queryCar(ChaincodeStub stub, List<String> args) {
		if (args.size() != 1) {
			return newErrorResponse(
					"Incorrect number of arguments. Expecting CarNumber ex: CAR01");
		}
		String carNumber = args.get(0);

		byte[] carAsJsonBytes = stub.getState(carNumber);

		if (carAsJsonBytes == null || carAsJsonBytes.length <= 0) {
			return newErrorResponse(carNumber + " does not exist: ");
		}

		return newSuccessResponse(carAsJsonBytes);
	}

	private Response initLedger(ChaincodeStub stub, List<String> args) {
		Car[] cars = new Car[] { new Car("Toyota", "Prius", "blue", "Tomoko"),
				new Car("Ford", "Mustang", "red", "Brad"),
				new Car("Hyundai", "Tucson", "green", "Jin Soo"),
				new Car("Volkswagen", "Passat", "yellow", "Max"),
				new Car("Tesla", "S", "black", "Adriana"),
				new Car("Peugeot", "205", "purple", "Michel"),
				new Car("Chery", "S22L", "white", "Aarav"),
				new Car("Fiat", "Punto", "violet", "Pari"),
				new Car("Tata", "Nano", "indigo", "Valeria"),
				new Car("Holden", "Barina", "brown", "Shotaro") };

		for (int i = 0; i < cars.length; i++) {
			stub.putState("CAR" + i, gson.toJson(cars[i]).getBytes());
			_logger.info("Added " + cars[i]);
		}

		return newSuccessResponse("Success init ledger");
	}

	private Response createCar(ChaincodeStub stub, List<String> args) {
		if (args.size() != 5) {
			return newErrorResponse("Incorrect number of arguments. Expecting 5");
		}

		Car car = new Car(args.get(1), args.get(2), args.get(3), args.get(4));

		stub.putState(args.get(0), gson.toJson(car).getBytes());

		return newSuccessResponse("Success add of " + args.get(0));
	}

	private Response queryAllCars(ChaincodeStub stub, List<String> args) {
		String startKey = "CAR0";
		String endKey = "CAR999";

		QueryResultsIterator<KeyValue> iterator = stub.getStateByRange(startKey, endKey);

		List<Map<String, Object>> lstResult = new ArrayList<>();
		for (KeyValue keyValue : iterator) {
			Map<String, Object> mpRecord = new HashMap<>();
			mpRecord.put("Key", keyValue.getKey());
			mpRecord.put("Record",
					gson.fromJson(new String(keyValue.getValue()), Car.class));
		}

		return newSuccessResponse("Success", gson.toJson(lstResult).getBytes());
	}

	private Response changeCarOwner(ChaincodeStub stub, List<String> args) {
		if (args.size() != 2) {
			return newErrorResponse("Incorrect number of arguments. Expecting 2");
		}

		Car car = gson.fromJson(new String(stub.getState(args.get(0))), Car.class);

		car.setOwner(args.get(1));

		stub.putState(args.get(0), gson.toJson(car).getBytes());

		return newSuccessResponse("Success change of owner of " + args.get(0));
	}

	public static void main(String[] args) {
		System.out.println("OpenSSL avaliable: " + OpenSsl.isAvailable());
		new FabcarChaincode().start(args);
	}

	public static class Car {

		private String make;

		private String model;

		private String colour;

		private String owner;

		public Car() {
		}

		public Car(String make, String model, String colour, String owner) {
			this.make = make;
			this.model = model;
			this.colour = colour;
			this.owner = owner;
		}

		public String getMake() {
			return make;
		}

		public void setMake(String make) {
			this.make = make;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public String getColour() {
			return colour;
		}

		public void setColour(String colour) {
			this.colour = colour;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

	}

}
