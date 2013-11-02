package org.assessment.mapsapp;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * This class is responsible for mapping JSON strings into objects.
 * @author Sebastian Guillen
 */
public final class JSONMapper {

	private static final String TAG = JSONMapper.class.getSimpleName();

	private JSONMapper() {
		// Hide Utility Class Constructor
	}

	public static JSONObject getJsonObject(String results) {
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(results);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			Log.e(TAG, results);
		}
		return jsonObject;
	}

}