package net.mitchtech.adb;

import java.io.IOException;

import net.mitchtech.adb.templight.R;

import org.microbridge.server.AbstractServerListener;
import org.microbridge.server.Server;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

public class TempLightActivity extends Activity {

	private final String TAG = TempLightActivity.class.getSimpleName();

	private int mLightLevel = 0;
	private int mTemperature = 0;

	// Create TCP server (based on MicroBridge LightWeight Server).
	// Note: This Server runs in a separate thread.
	Server mServer = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		// Create TCP server (based on MicroBridge LightWeight Server)
		try {
			mServer = new Server(4568); // Use ADK port
			mServer.start();
		} catch (IOException e) {
			Log.e(TAG, "Unable to start TCP server", e);
			System.exit(-1);
		}

		mServer.addListener(new AbstractServerListener() {

			@Override
			public void onReceive(org.microbridge.server.Client client, byte[] data) {

				if (data.length < 4)
					return;
				mLightLevel = (data[0] & 0xff) | ((data[1] & 0xff) << 8);
				mTemperature = (data[2] & 0xff) | ((data[3] & 0xff) << 8);
				// Any update to UI can not be carried out in a non UI thread
				// like the one used for Server. Hence runOnUIThread is used.
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.i(TAG, "light: " + mLightLevel);
						Log.i(TAG, "temp: " + mTemperature);
						new TempUpdateTask().execute(mTemperature);
						new LightUpdateTask().execute(mLightLevel);
					}
				});
			}
		});
	} // End of TCP Server code

	// UpdateData Asynchronously sends the value received from ADK Main Board.
	// This is triggered by onReceive()
	class TempUpdateTask extends AsyncTask<Integer, Integer, String> {
		// Called to initiate the background activity
		@Override
		protected String doInBackground(Integer... sensorValue) {
			String returnString = String.valueOf(sensorValue[0]) + " F";
			return (returnString); // This goes to result
		}

		// Called when there's a status to be updated
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			// Not used in this case
		}

		// Called once the background activity has completed
		@Override
		protected void onPostExecute(String result) {
			// Init TextView Widget to display ADC sensor value in numeric.
			TextView tvAdcvalue = (TextView) findViewById(R.id.tvTemp);
			tvAdcvalue.setText(String.valueOf(result));
		}
	}
	
	// UpdateData Asynchronously sends the value received from ADK Main Board.
	// This is triggered by onReceive()
	class LightUpdateTask extends AsyncTask<Integer, Integer, String> {
		// Called to initiate the background activity
		@Override
		protected String doInBackground(Integer... sensorValue) {
			// Init SeeekBar Widget to display ADC sensor value in SeekBar
			// Max value of SeekBar is set to 1024
			SeekBar sbLightLevel = (SeekBar) findViewById(R.id.sbLight);
			sbLightLevel.setProgress(sensorValue[0]);
			String returnString = String.valueOf(sensorValue[0]);
			return (returnString); // This goes to result
		}

		// Called when there's a status to be updated
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			// Not used in this case
		}

		// Called once the background activity has completed
		@Override
		protected void onPostExecute(String result) {
			// Init TextView Widget to display ADC sensor value in numeric.
			TextView tvLightLevel = (TextView) findViewById(R.id.tvLight);
			tvLightLevel.setText(String.valueOf(result));
		}
	}

}
