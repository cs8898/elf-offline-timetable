package tk.cs8898.elfofflinett.services;

import android.app.IntentService;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.services.logic.FetchTimeTableLogic;
import tk.cs8898.elfofflinett.services.logic.NotificationLogic;

import static tk.cs8898.elfofflinett.services.NotificationService.ScheduledNotificationService.NOTIFY_JOB_ID;

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

    public static void scheduleFetchTimetable(Context context, boolean offline, boolean update) {
        Log.d(FetchTimeTableService.class.getSimpleName(),"Scheduling Fetch Request");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            assert jobScheduler != null;
            for(JobInfo job:jobScheduler.getAllPendingJobs()){
                if(job.getId() == ScheduledFetchTimetableService.FETCH_JOB_ID)
                    Log.d(FetchTimeTableService.class.getSimpleName(),"There is Already a Fetch Job Scheduled");
                    return;
            }
            ComponentName serviceComponent = new ComponentName(context, ScheduledFetchTimetableService.class);

            JobInfo.Builder builder = new JobInfo.Builder(ScheduledFetchTimetableService.FETCH_JOB_ID, serviceComponent);
            PersistableBundle extras = new PersistableBundle();
            extras.putString(EXTRA_URL, context.getString(R.string.timetable_url));
            extras.putString(EXTRA_OFFLINE,String.valueOf(offline));
            extras.putString(EXTRA_UPDATE, String.valueOf(update));
            builder.setRequiredNetworkType(offline?JobInfo.NETWORK_TYPE_NONE:JobInfo.NETWORK_TYPE_ANY)
                    .setExtras(extras)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false);
            if(offline)
                builder.setOverrideDeadline(0);
            jobScheduler.schedule(builder.build());
        } else {
            startActionFetchTimetable(context,offline,update);
        }
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static class ScheduledFetchTimetableService extends JobService {
        public static final int FETCH_JOB_ID = 889822;

        @Override
        public boolean onStartJob(JobParameters params) {
            if (params.getJobId() == FETCH_JOB_ID) {
                Log.d(ScheduledFetchTimetableService.class.getSimpleName(),"Starting Request");
                final String url = params.getExtras().getString(EXTRA_URL);
                final boolean offline = Boolean.parseBoolean(params.getExtras().getString(EXTRA_OFFLINE, "false"));
                final boolean update = Boolean.parseBoolean(params.getExtras().getString(EXTRA_UPDATE,"false"));
                FetchTimeTableLogic.handleActionFetchTimetable(this.getApplicationContext(),url, offline, update);
            }
            return false;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return false;
        }
    }
}
