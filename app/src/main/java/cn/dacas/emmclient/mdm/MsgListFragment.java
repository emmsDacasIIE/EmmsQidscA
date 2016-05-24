package cn.dacas.emmclient.mdm;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import java.util.ArrayList;
import java.util.List;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.util.BroadCastDef;

public class MsgListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<List<String>>, OnQueryTextListener {

	private String _curFilter; // if non-null, this is the current filter the
								// user has provided.
	private MsgListAdapter _adapter; // the adapter
	
	// ListFragment methods
	// -----------------------------------------------------------------------------------------
	/*
	 * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
	 * container, Bundle savedInstanceState) { return
	 * inflater.inflate(R.layout.fragment_main_list, container, false); }
	 */

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// set an empty filter
		_curFilter = null;

		// has menu
		setHasOptionsMenu(true);

		// set empty adapter
		_adapter = new MsgListAdapter(getActivity(), new ArrayList<String>());
		setListAdapter(_adapter);

		// start out with a progress indicator.
		setListShown(false);
		
		this.setEmptyText("未收到企业消息");
		
		// prepare the loader. Either re-connect with an existing one, or start
		// a new one.
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		registerMReceiver();
	}
	
	private void registerMReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BroadCastDef.OP_MSG);
		this.getActivity().registerReceiver(mBroadcastReciver, intentFilter);
	}
	
	private BroadcastReceiver mBroadcastReciver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (BroadCastDef.OP_MSG.equals(intent.getAction())) {
				MsgListFragment.this.getLoaderManager().restartLoader(0, null, MsgListFragment.this);
			}
		}
	};
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		this.getActivity().unregisterReceiver(mBroadcastReciver);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the options menu from XML
		inflater.inflate(R.menu.device_msg, menu);

		// Get the SearchView and set the searchable configuration
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
		searchView.setOnQueryTextListener(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		return super.onOptionsItemSelected(item);
	}

	// LoaderManager methods
	// -----------------------------------------------------------------------------------------
	@Override
	public Loader<List<String>> onCreateLoader(int id, Bundle args) {
		return new MsgListLoader(getActivity(), _curFilter);
	}

	@Override
	public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
		// Set the new data in the adapter.
		_adapter.setData(data);
		
		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<String>> loader) {
		// Android guides set data to null but I need check
		// what happen when I set data as null
		_adapter.setData(new ArrayList<String>());
	}

	// OnQueryTextListener methods
	// -----------------------------------------------------------------------------------------
	@Override
	public boolean onQueryTextChange(String newText) {
		// Called when the action bar search text has changed. sUpdate
		// the search filter, and restart the loader to do a new query
		// with this filter.
		_curFilter = !TextUtils.isEmpty(newText) ? newText : null;
		_adapter.setQuery(_curFilter);
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// Don't care about this.
		return false;
	}
}
