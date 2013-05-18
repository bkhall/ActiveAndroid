package com.activeandroid;

import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import com.activeandroid.query.From;

/**
 * The Class ModelLoader.
 * 
 * @param <T>
 *            the generic type
 */
public class ModelLoader<T extends Model> extends AsyncTaskLoader<List<T>> {

	/** The m data set observer. */
	private DataSetObserver mDataSetObserver;

	/** The m query. */
	private From mQuery;

	/** The m results. */
	private List<T> mResults;

	/** Observer switch */
	private boolean mUseObserver;

	/**
	 * Instantiates a new model loader.
	 * 
	 * @param context
	 *            the context
	 * @param from
	 *            the from
	 */
	public ModelLoader(Context context, From from) {
		this(context, false, from);
	}

	/**
	 * Instantiates a new model loader.
	 * 
	 * @param context
	 *            the context
	 * @param boolean the useObserver switch
	 * @param from
	 *            the from
	 */
	public ModelLoader(Context context, boolean useObserver, From from) {
		super(context);
		mUseObserver = useObserver;
		mQuery = from;
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 * 
	 * @param toolData
	 *            the tool data
	 */
	@Override
	public void deliverResult(List<T> toolData) {
		mResults = toolData;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			super.deliverResult(toolData);
		}
	}

	/**
	 * This is where the bulk of our work is done. This function is called in a
	 * background thread and should generate a new set of data to be published
	 * by the loader.
	 * 
	 * @return the list
	 */
	@Override
	public List<T> loadInBackground() {
		return mQuery.execute();
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		// At this point we can release the resources associated with the list
		// if needed.
		mResults = null;

		// Stop monitoring for changes.
		if (mDataSetObserver != null) {
			Model.unregisterDataSetObserver(mQuery.getModelType(),
					mDataSetObserver);
			mDataSetObserver = null;
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (mResults != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(mResults);
		}

		// Start watching for changes in the job data.
		if (mDataSetObserver == null && mUseObserver) {
			mDataSetObserver = new DataSetObserver() {
				@Override
				public void onChanged() {
					super.onChanged();

					/*
					 * It's always a freakin' threading issue, ain't it?
					 * Directly calling onContentChanged here doesn't seem to
					 * consistently work, but posting it to the main thread
					 * does.
					 */
					new Handler(getContext().getMainLooper())
							.postAtFrontOfQueue(new Runnable() {
								@Override
								public void run() {
									ModelLoader.this.onContentChanged();
								}
							});
				}
			};

			Model.registerDataSetObserver(mQuery.getModelType(),
					mDataSetObserver);
		}

		if (takeContentChanged() || mResults == null) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
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
}
