package cn.dacas.emmclient.mdm;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.dacas.emmclient.db.EmmClientDb;
import cn.dacas.emmclient.main.EmmClientApplication;

public class MsgListLoader extends AsyncTaskLoader<List<String>> {
	private List<String> _data;
	private String _query;
	private Context _context;

	/**
	 * 
	 * @param context
	 *            Context The context
	 * @param queryFiler
	 *            String Set to null for not use it
	 */
	public MsgListLoader(Context context, String queryFiler) {
		super(context);
		_context = context;
		_query = queryFiler;

	}
	


	/**
	 * This is where the bulk of our work is done. This function is called in a
	 * background thread and should generate a new set of data to be published
	 * by the loader.
	 */
	@Override
	public List<String> loadInBackground() {
		List<String> result = new ArrayList<String>();

		Cursor mCursor = EmmClientApplication.mDb.getAllItemsOfTable(
				EmmClientDb.DEVICEMSG_DATABASE_TABLE, null);
		
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToLast();
			int msgIdx = mCursor.getColumnIndexOrThrow("msg");
			int timeIdx = mCursor.getColumnIndexOrThrow("time");

			do {
				try {
					String msg = mCursor.getString(msgIdx);
					String time = mCursor.getString(timeIdx);
					
					int index = TextUtils.isEmpty(_query) ? 0 : msg.toLowerCase()
							.indexOf(_query.toLowerCase());
					if(index != -1){
						JSONObject json = new JSONObject();
						json.put("t", time);
						json.put("m", msg);
						result.add(json.toString());
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (mCursor.moveToPrevious());
		}
		
		try {
			if (null != mCursor) {
				mCursor.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(List<String> data) {
		if (isReset()) {
			// The Loader has been reset; ignore the result and invalidate the
			// data.
			onReleaseResources(data);
		}
		List<String> oldData = _data;
		_data = data;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately deliver
			// its results.
			super.deliverResult(data);
		}

		// At this point we can release the resources associated with 'oldApps'
		// if needed; now
		// that the new result is delivered we know that it is no longer in use.
		if (oldData != null && oldData != data) {
			onReleaseResources(oldData);
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (_data != null) {
			// Deliver any previously loaded data immediately.
			deliverResult(_data);
		}

		if (takeContentChanged() || _data == null) {
			// When the observer detects a change, it should call
			// onContentChanged()
			// on the Loader, which will cause the next call to
			// takeContentChanged()
			// to return true. If this is ever the case (or if the current data
			// is
			// null), we force a new load.
			forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	@Override
	public void onCanceled(List<String> data) {
		super.onCanceled(data);
		// At this point we can release the resources associated with 'apps' if
		// needed.
		onReleaseResources(data);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		// At this point we can release the resources associated with 'apps'
		// if needed.
		if (_data != null) {
			onReleaseResources(_data);
			_data = null;
		}
	}

	/**
	 * Helper function to take care of releasing resources associated with an
	 * actively loaded data set.
	 */
	protected void onReleaseResources(List<String> apps) {
		// For a simple List<> there is nothing to do. For something
		// like a Cursor, we would close it here.
	}
}
