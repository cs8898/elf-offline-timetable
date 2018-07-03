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
import tk.cs8898.elfofflinett.model.Common;
import tk.cs8898.elfofflinett.model.database.MarkedActsService;
import tk.cs8898.elfofflinett.model.entity.StageEntity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class FetchTimeTableService extends IntentService {
    private static final String ACTION_FETCH_TIMETABLE = "tk.cs8898.elfofflinett.action.fetchtimetable";
    private static final String EXTRA_URL = "tk.cs8898.elfofflinett.extra.url";
    private static final String EXTRA_OFFLINE = "tk.cs8898.elfofflinett.extra.offline";
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
    public static void startActionFetchTimetable(Context context, String url) {
        startActionFetchTimetable(context, url, false);
    }

    public static void startActionFetchTimetable(Context context, String url, boolean offline) {
        Intent intent = new Intent(context, FetchTimeTableService.class);
        intent.setAction(ACTION_FETCH_TIMETABLE);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_OFFLINE, offline);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_TIMETABLE.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                final boolean offline = intent.getBooleanExtra(EXTRA_OFFLINE, false);
                handleActionFetchTimetable(url, offline);
            }
        }
    }

    /**
     * Handle action Fetch Timetable in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchTimetable(String url, boolean offline) {
        if (!offline && Common.isOnline(getApplicationContext())) {
            OkHttpClient okHttpClient = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                ResponseBody body = response.body();
                if(body == null)
                    throw new IOException("Can't get response Body");
                String json = body.string();
                FileOutputStream fileOutputStream = openFileOutput(LOCAL_FILE, MODE_PRIVATE);
                PrintWriter printWriter = new PrintWriter(fileOutputStream);
                printWriter.print(json);
                printWriter.close();
                fileOutputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e("FetchTimeTableService", "Error while Fetching Online File");
            }
        }
        try {
            FileInputStream fileInputStream = openFileInput(LOCAL_FILE);
            byte[] data = new byte[fileInputStream.available()];
            int len = fileInputStream.read(data);
            fileInputStream.close();
            if(len!=data.length)
                throw new IOException("Wasn't able to read all data");

            String jsonBody = new String(data, "UTF-8");

            Gson gson = new Gson();

            StageEntity[] stages = gson.fromJson(jsonBody, StageEntity[].class);
            MarkedActsService.setAllActs(getApplicationContext(),stages);
            Log.d("FetchTimeTableService", "fetched " + stages.length + " stages");

        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            Log.e("FetchTimeTableService", "No Offline File Present");
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            Log.e("FetchTimeTableService", "Error while fetching Local File");
            //e.printStackTrace();
        }
    }
}
