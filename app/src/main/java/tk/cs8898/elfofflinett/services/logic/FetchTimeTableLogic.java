package tk.cs8898.elfofflinett.services.logic;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tk.cs8898.elfofflinett.model.Common;
import tk.cs8898.elfofflinett.model.bus.BusProvider;
import tk.cs8898.elfofflinett.model.bus.messages.MessageDatasetReady;
import tk.cs8898.elfofflinett.model.database.MarkedActsService;
import tk.cs8898.elfofflinett.model.entity.ActEntity;
import tk.cs8898.elfofflinett.model.entity.StageEntity;
import tk.cs8898.elfofflinett.services.FetchTimeTableService;

public class FetchTimeTableLogic {

    private static final String LOCAL_FILE = "elfofflinett.json";

    public static Thread getThreadActionFetchTimetable(final Context context, final String url, final boolean offline, final boolean update){
        return new Thread(){
            @Override
            public void run(){
                handleActionFetchTimetable(context,url,offline,update);
            }
        };
    }

    public static void handleActionFetchTimetable(Context context, String url, boolean offline, boolean update) {
        if(!loadOffline(context, update)){
            offline=false;//Force Loading Online when there was no offline file
        }
        if (!offline && Common.isOnline(context)) {
            if(fetchOnline(context, url))
                loadOffline(context, update);
        }
        BusProvider.getInstance().post(new MessageDatasetReady(FetchTimeTableService.class));
    }

    private static boolean fetchOnline(Context context, String url) {
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
            FileOutputStream fileOutputStream = context.openFileOutput(LOCAL_FILE, Context.MODE_PRIVATE);
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

    private static boolean loadOffline(Context context, boolean update) {
        try {
            FileInputStream fileInputStream = context.openFileInput(LOCAL_FILE);
            byte[] data = new byte[fileInputStream.available()];
            int len = fileInputStream.read(data);
            fileInputStream.close();
            if (len != data.length)
                throw new IOException("Wasn't able to read all offline data");

            String jsonBody = new String(data, "UTF-8");

            Gson gson = new Gson();

            StageEntity[] stages = gson.fromJson(jsonBody, StageEntity[].class);
            for(int d = 10; d <= 15; d++) {
                for (int h = 0; h <= 23; h++) {
                    stages[0].getActs().add(new ActEntity("July " + d + ", 2018 " + String.format(Locale.GERMAN, "%02d", h) + ":00", "0-" + d + "-" + h, "July " + d + ", 2018 " + String.format(Locale.GERMAN, "%02d", h) + ":45"));
                    stages[1].getActs().add(new ActEntity("July " + d + ", 2018 " + String.format(Locale.GERMAN, "%02d", h) + ":15", "1-" + d + "-" + h, "July " + d + ", 2018 " + String.format(Locale.GERMAN, "%02d", h) + ":45"));
                    stages[2].getActs().add(new ActEntity("July " + d + ", 2018 " + String.format(Locale.GERMAN, "%02d", h) + ":30", "2-" + d + "-" + h, "July " + d + ", 2018 " + String.format(Locale.GERMAN, "%02d", (h+1)%24) + ":00"));
                    stages[3].getActs().add(new ActEntity("July " + d + ", 2018 " + String.format(Locale.GERMAN, "%02d", h) + ":45", "3-" + d + "-" + h, "July " + d + ", 2018 " + String.format(Locale.GERMAN, "%02d", (h+1)%24) + ":15"));
                }
            }
            MarkedActsService.setAllActs(context.getApplicationContext(), stages, update);
            Log.d("FetchTimeTableService", "fetched " + stages.length + " stages");
            return true;
        } catch (IOException e) {
            //e.printStackTrace();
            Log.e("FetchTimeTableService", "Error while Fetching Offline File");
            return false;
        }
    }
}