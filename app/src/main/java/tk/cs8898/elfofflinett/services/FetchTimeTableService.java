package tk.cs8898.elfofflinett.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.services.logic.FetchTimeTableLogic;

/**
 * Fetches the TimeTable from the url provided by @string/timetableurl
 */
public class FetchTimeTableService extends IntentService {
    private static final String ACTION_FETCH_TIMETABLE = "tk.cs8898.elfofflinett.action.fetchtimetable";
    private static final String EXTRA_URL = "tk.cs8898.elfofflinett.extra.url";
    private static final String EXTRA_OFFLINE = "tk.cs8898.elfofflinett.extra.offline";
    private static final String EXTRA_UPDATE = "tk.cs8898.elfofflinett.extra.update";

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
                FetchTimeTableLogic.handleActionFetchTimetable(this.getApplicationContext(),url, offline, update);
            }
        }
        stopSelf();
    }
}
