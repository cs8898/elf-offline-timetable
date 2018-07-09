package tk.cs8898.elfofflinett.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.model.Common;
import tk.cs8898.elfofflinett.model.database.MarkedActsService;
import tk.cs8898.elfofflinett.model.entity.StageEntity;

/**
 * Fetches the TimeTable from the url provided by @string/timetableurl
 */
public class FetchTimeTableService extends IntentService {
    private static final String ACTION_FETCH_TIMETABLE = "tk.cs8898.elfofflinett.action.fetchtimetable";
    private static final String EXTRA_URL = "tk.cs8898.elfofflinett.extra.url";
    private static final String EXTRA_OFFLINE = "tk.cs8898.elfofflinett.extra.offline";
    private static final String EXTRA_UPDATE = "tk.cs8898.elfofflinett.extra.update";
    private static final String LOCAL_FILE = "elfofflinett.json";

    public FetchTimeTableService() {
        super("FetchTimeTableService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchTimetable(Context context) {
        startActionFetchTimetable(context, false, true);
    }

    public static void startActionFetchTimetable(Context context, boolean offline, boolean update) {
        Intent intent = new Intent(context, FetchTimeTableService.class);
        intent.setAction(ACTION_FETCH_TIMETABLE);
        intent.putExtra(EXTRA_URL, context.getString(R.string.timetable_url));
        intent.putExtra(EXTRA_OFFLINE, offline);
        intent.putExtra(EXTRA_UPDATE, update);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_TIMETABLE.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                final boolean offline = intent.getBooleanExtra(EXTRA_OFFLINE, false);
                final boolean update = intent.getBooleanExtra(EXTRA_UPDATE, false);
                handleActionFetchTimetable(url, offline, update);
            }
        }
    }

    /**
     * Handle action Fetch Timetable in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchTimetable(String url, boolean offline, boolean update) {
        loadOffline(update);
        if (!offline && Common.isOnline(getApplicationContext())) {
            if(fetchOnline(url))
                loadOffline(update);
        }
    }

    private boolean fetchOnline(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            ResponseBody body = response.body();
            if (body == null)
                throw new IOException("Can't get response Body Online");
            String json = body.string();
            FileOutputStream fileOutputStream = openFileOutput(LOCAL_FILE, MODE_PRIVATE);
            PrintWriter printWriter = new PrintWriter(fileOutputStream);
            printWriter.print(json);
            printWriter.close();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            //e.printStackTrace();
            Log.e("FetchTimeTableService", "Error while Fetching Online File");
            return false;
        }
    }

    private boolean loadOffline(boolean update) {
        try {
            FileInputStream fileInputStream = openFileInput(LOCAL_FILE);
            byte[] data = new byte[fileInputStream.available()];
            int len = fileInputStream.read(data);
            fileInputStream.close();
            if (len != data.length)
                throw new IOException("Wasn't able to read all offline data");

            String jsonBody = new String(data, "UTF-8");

            Gson gson = new Gson();

            StageEntity[] stages = gson.fromJson(jsonBody, StageEntity[].class);
            MarkedActsService.setAllActs(getApplicationContext(), stages, update);
            Log.d("FetchTimeTableService", "fetched " + stages.length + " stages");
            return true;
        } catch (IOException e) {
            //e.printStackTrace();
            Log.e("FetchTimeTableService", "Error while Fetching Offline File");
            return false;
        }
    }
}
