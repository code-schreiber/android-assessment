package org.assessment.mapsapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final double SEARCH_RADIUS_KM = 2;
	private static final GeoPoint BERLIN = new GeoPoint(52.51, 13.40);


	private IMapController controller;
	private MapView map;

	private static class Car {

		public GeoPoint position;
		public String name;
		public String address;

		public Car(GeoPoint position, String name, String address) {
			this.position = position;
			this.name = name;
			this.address = address;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initLocation();
		initSearchButton();
	}

	private void initLocation() {
		this.map = (MapView) this.findViewById(R.id.mapview);
		this.map.setBuiltInZoomControls(true);
		this.controller = map.getController();

		updateLocation(BERLIN);
	}

	private void initSearchButton() {
		final EditText latitude = (EditText)findViewById(R.id.latitude);
		final EditText longitude = (EditText) findViewById(R.id.longitude);
		android.widget.Button goButton = (android.widget.Button)findViewById(R.id.go);
		goButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tryToSearch(latitude, longitude);
			}
		});
	}

	private void tryToSearch(EditText latitude, EditText longitude) {
		String latitudeString = latitude.getText().toString();
		String longitudeString = longitude.getText().toString();
		if (latitudeString.length() == 0 || longitudeString.length() == 0) {
			Toast.makeText(this, "Please enter latitude and longitude", Toast.LENGTH_SHORT).show();
		} else {
			hideKeyboard();
			double lat = Double.parseDouble(latitudeString);
			double lon = Double.parseDouble(longitudeString);
			GeoPoint location = new GeoPoint(lat, lon);
			updateLocation(location);
		}
	}

	private void updateLocation(GeoPoint location){
		this.controller.setZoom(12);
		this.controller.setCenter(location);

		List<Car> carsInCloseRadius = filterCarsByDistance(getCars(), location, SEARCH_RADIUS_KM);

		// Show cars on map
		showCarsOnMap(carsInCloseRadius);
	}

	private List<Car> getCars() {
		List<Car> cars = new ArrayList<Car>();
		JSONObject json = null;
		try {
			// Read the JSON data from https://www.drive-now.com/php/metropolis/json.vehicle_filter?cit=6099
			json = new AsyncRequest().execute().get();
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage());
		} catch (ExecutionException e) {
			Log.e(TAG, e.getMessage());
		}

		if(json == null || json.length() == 0){
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_LONG).show();
		}
		else{
			// Parse the data
			JSONArray carsInJson = parseCars(json);
			//fill a list of Car objects
			cars = getCarList(carsInJson);
		}
		return cars;
	}

	/** Filter the list of cars by a location within the specified radius
	 * @return a list of cars in the specified radius
	 */
	private List<Car> filterCarsByDistance(List<Car> cars, GeoPoint location, double searchRadiusKm) {
		List<Car> filteredCars = new ArrayList<Car>();
		double radiusInMeters = searchRadiusKm * 1000;
		// If the car is within radius, add it
		for (Car car : cars) {
			if(location.distanceTo(car.position) < radiusInMeters){
				filteredCars.add(car);
			}
		}
		String toastText = "Found "+ filteredCars.size()+" cars near you.";
		Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
		return filteredCars;
	}

	private void showCarsOnMap(List<Car> cars) {
		List<OverlayItem> items = new ArrayList<OverlayItem>();

		for (Car car : cars){
			items.add(new OverlayItem(car.name, car.address, car.position));
		}

		OnItemGestureListener<OverlayItem> listener = new OnItemGestureListener<OverlayItem>() {
			@Override
			public boolean onItemLongPress(int arg0, OverlayItem item) {
				Toast toast = Toast.makeText(getApplicationContext(), item.getTitle() + ", " + item.getSnippet(), Toast.LENGTH_LONG);
				toast.show();
				return false;
			}
			@Override
			public boolean onItemSingleTapUp(int arg0, OverlayItem item) {
				Toast toast = Toast.makeText(getApplicationContext(), item.getTitle() + ", " + item.getSnippet(), Toast.LENGTH_LONG);
				toast.show();
				return false;
			}};
			ItemizedIconOverlay<OverlayItem> overlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), items, listener);

			this.map.getOverlays().clear();
			this.map.getOverlays().add(overlay);
			this.map.invalidate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	/** Filters the JSON to only have relevant elements
	 * Cars are located in rec->vehicles->vehicles
	 * @param json the complete JSON object
	 * @return JSON array of cars
	 */
	private JSONArray parseCars(JSONObject json) {
		JSONArray filteredJson = new JSONArray();
		try {
			filteredJson = json.getJSONObject(JsonKey.REC).getJSONObject(JsonKey.VEHICLES).getJSONArray(JsonKey.VEHICLES);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		return filteredJson;
	}

	/** Get a list of the cars in the JSON array
	 * @param carsInJson the JSON array
	 * @return a list of cars
	 */
	private List<Car> getCarList(JSONArray carsInJson) {
		List<Car> cars = new ArrayList<Car>();
		JSONObject jsonCar = null;
		for (int i = 0; i < carsInJson.length(); i++) {
			try {
				jsonCar = carsInJson.getJSONObject(i);
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
			cars.add(getCarFromJsonObject(jsonCar));
		}
		return cars;
	}

	/**
	 * Important JSON nodes are: position->latitude, position->longitude, carName and address
	 * @return a car from the JSON object
	 */
	private Car getCarFromJsonObject(JSONObject jsonCar) {
		String name = null;
		String address = null;
		JSONObject pos;
		double lat = 0;
		double lon = 0;
		try {
			name = jsonCar.getString(JsonKey.CAR_NAME);
			address = jsonCar.getString(JsonKey.ADDRESS);
			pos = jsonCar.getJSONObject(JsonKey.POSITION);
			lat = Double.parseDouble(pos.getString(JsonKey.LATITUDE));
			lon = Double.parseDouble(pos.getString(JsonKey.LONGITUDE));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		GeoPoint position = new GeoPoint(lat, lon);
		Car car = new Car(position, name, address);
		return car;
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}

	/**
	 * Responsible for getting the request asynchronously
	 */
	public class AsyncRequest extends AsyncTask<String, Integer, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... params) {
			return HttpRequest.getCars();
		}
	}

}
