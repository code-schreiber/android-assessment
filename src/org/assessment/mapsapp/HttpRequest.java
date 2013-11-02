package org.assessment.mapsapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.util.Log;

/**
 * This class handles connections to the Internet
 * @author Sebastian Guillen
 */
public final class HttpRequest {

	private static final String TAG = HttpRequest.class.getSimpleName();

	/** Connection timeout in milliseconds **/
	private static final int TIMEOUT = 20000;
	/** URL of Cars **/
	private static final String carsUrl = "https://www.drive-now.com/php/metropolis/json.vehicle_filter?cit=6099";

	private HttpRequest() {
		// Hide Utility Class Constructor
	}

	/** Get the cars in a JSON object
	 * @return A JSON object with all results
	 */
	public static JSONObject getCars() {
		return request(carsUrl);
	}

	/** Get request results and return the corresponding JSON object TODO all jdocs
	 * @param request the URL
	 * @return The list result
	 */
	private static JSONObject request(String request) {
		String results;
		JSONObject json = new JSONObject();
		try {
			results = doRequest(request);
		} catch (ClientProtocolException e) {
			return json;
		} catch (IOException e) {
			return json;
		}
		json = JSONMapper.getJsonObject(results);
		return json;
	}

	/**
	 * Do a HTTP Request
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @return the response string
	 */
	protected static String doRequest(String url) throws IOException {
		Log.d(TAG, url);
		String responseString = "";
		DefaultHttpClient httpclient = new DefaultHttpClient(getTimeout());
		HttpResponse  response = httpclient.execute(new HttpGet(url));
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			out.close();
			responseString = out.toString();
			Log.d(TAG, out.size()+" Bytes");
		} else {
			// Close the connection.
			response.getEntity().getContent().close();
			Log.e(TAG, statusLine.getReasonPhrase());
		}
		return responseString;
	}

	/** Set the timeout in milliseconds until a connection is established.
	 the default value is zero, that means the timeout is not used. **/
	private static HttpParams getTimeout() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
		return httpParams;
	}

}
