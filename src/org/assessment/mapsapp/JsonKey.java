package org.assessment.mapsapp;

/**
 * Class for keys that will be used for parsing JSON objects
 * NOTE: There are more keys, but they are not used in this app.
 * @author Sebastian Guillen
 */
public final class JsonKey {

	private JsonKey() {
		// Hide Utility Class Constructor
	}

	//In the JSON Array
	public static final String REC = "rec";
	public static final String VEHICLES = "vehicles";

	//Part of every JSON car
	public static final String CAR_NAME = "carName";
	public static final String ADDRESS = "address";
	public static final String POSITION = "position";;
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";


}
