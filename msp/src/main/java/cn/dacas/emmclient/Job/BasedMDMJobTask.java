package cn.dacas.emmclient.Job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import cn.dacas.emmclient.model.SerializableCMD;
import cn.dacas.emmclient.msgpush.PushMsgReceiver;
import cn.dacas.emmclient.util.QDLog;



/**
 * Created by Sun RX on 2016-12-6.
 * A Based JobTask Class.
 */

public class BasedMDMJobTask extends Job {
    protected static int PRIORITY = 1;
    protected String text;
    protected SerializableCMD cmd;
    private final String TAG = "JOB";
    public BasedMDMJobTask(int priority,
                           String text,
                           SerializableCMD cmd) {
        // This job requires network connectivity,
        // and should be persisted in case the application exits before job is completed.
        super(new Params(priority).requireNetwork().persist());
        this.text = text;
        PRIORITY = priority;
        this.cmd = cmd;
        QDLog.i(TAG,text+"  goin");
    }

    public BasedMDMJobTask(String text,
                           SerializableCMD cmd) {
        this(PRIORITY,text,cmd);
    }
    @Override
    public void onAdded() {
        // Job has been saved to disk.
        // This is a good place to dispatch a UI event to indicate the job will eventually run.
        // In this example, it would be good to update the UI with the newly posted tweet.
        QDLog.i(TAG,text+"  Onadded");
    }
    @Override
    public void onRun() throws Throwable {
        // Job logic goes here. In this example, the network call to post to Twitter is done here.
        // All work done here should be synchronous, a job is removed from the queue once
        // onRun() finishes.
        QDLog.i(TAG,text+"  onRun");
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount,
                                                     int maxRunCount) {
        // An error occurred in onRun.
        // Return value determines whether this job should retry or cancel. You can further
        // specify a backoff strategy or change the job's priority. You can also apply the
        // delay to the whole group to preserve jobs' running order.
        QDLog.e(TAG,throwable.toString());
        PushMsgReceiver.getMsgWorker().sendStatusToServer("Error",cmd.cmdUUID,null);
        return RetryConstraint.CANCEL;
        //return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
}