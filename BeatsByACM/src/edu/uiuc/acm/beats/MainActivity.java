package edu.uiuc.acm.beats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	private static final String BEATS_SERVER = "http://castor.kevnwang.com:4000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	synchronized public void run() {
        		new UpdateNowPlayingTask().execute(BEATS_SERVER + "/v1/now_playing");
        	}
        }, 0, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    
    private String downloadUrl(String myurl) throws IOException {
    	InputStream is = null;
    	StringBuilder builder = new StringBuilder();
    	
    	try {
    		URL url = new URL(myurl);
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		conn.setReadTimeout(10000);
    		conn.setConnectTimeout(15000);
    		conn.setRequestMethod("GET");
    		conn.setDoInput(true);
    		conn.connect();
    		int response = conn.getResponseCode();
    		Log.d("MainActivity", "The response is: " + response);
    		is = conn.getInputStream();
    		
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    		String line;
    		while ((line = reader.readLine()) != null) {
    			builder.append(line);
    		}
    	}
    	finally {
    		if (is != null) {
    			is.close();
    		}
    	}
    	return builder.toString();
    }
    
    private class UpdateNowPlayingTask extends AsyncTask<String, Void, String> {
    	@Override
    	protected String doInBackground(String... urls) {
    		try {
    			return downloadUrl(urls[0]);
    		}
    		catch (Exception e) {
    			e.printStackTrace();
    			return "";
    		}
    	}
    	
    	@Override
    	protected void onPostExecute(String result) {
			TextView titleText = (TextView) findViewById(R.id.titleText);
			TextView artistText = (TextView) findViewById(R.id.artistText);
			TextView albumText = (TextView) findViewById(R.id.albumText);
			ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
    		try {
    			JSONObject jsonObject = new JSONObject(result);
    			JSONObject media = jsonObject.getJSONObject("media");
    			JSONObject playerStatus = jsonObject.getJSONObject("player_status");
    			titleText.setText(media.getString("title"));
    			artistText.setText(media.getString("artist"));
    			albumText.setText(media.getString("album"));
    			int currentTime = playerStatus.getInt("current_time");
    			int duration = playerStatus.getInt("duration");
    			progressBar.setMax(duration);
    			progressBar.setProgress(currentTime);
    		}
    		catch (Exception e) {
    		}
    	}
    }
}
