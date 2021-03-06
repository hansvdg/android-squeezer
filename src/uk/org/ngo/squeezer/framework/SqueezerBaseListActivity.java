/*
 * Copyright (c) 2011 Kurt Aaholst <kaaholst@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.ngo.squeezer.framework;


import java.util.List;

import uk.org.ngo.squeezer.R;
import uk.org.ngo.squeezer.util.ImageCache.ImageCacheParams;
import uk.org.ngo.squeezer.util.ImageFetcher;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A generic base class for an activity to list items of a particular
 * SqueezeServer data type. The data type is defined by the generic type
 * argument, and must be an extension of {@link SqueezerItem}. You must provide
 * an {@link SqueezerItemView} to provide the view logic used by this activity.
 * This is done by implementing
 * {@link SqueezerItemListActivity#createItemView()}.
 * <p>
 * When the activity is first created ({@link #onCreate(Bundle)}), an empty
 * {@link SqueezerItemListAdapter} is created using the provided
 * {@link SqueezerItemView}. See {@link SqueezerItemListActivity} for see
 * details of ordering and receiving of list items from SqueezeServer, and
 * handling of item selection.
 * 
 * @param <T> Denotes the class of the items this class should list
 * @author Kurt Aaholst
 */
public abstract class SqueezerBaseListActivity<T extends SqueezerItem> extends SqueezerItemListActivity {
	private SqueezerItemAdapter<T> itemAdapter;
	private TextView loadingLabel;
	private SqueezerItemView<T> itemView;

    /** An ImageFetcher for loading thumbnails. */
    protected ImageFetcher mImageFetcher;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_list);
        mListView = (ListView) findViewById(R.id.item_list);
		loadingLabel = (TextView) findViewById(R.id.loading_label);
		itemView = createItemView();

        mListView.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // XXX: Adapter should implement onItemClickListener and pass
                // this down to the views.
    			T item = getItemAdapter().getItem(position);
    			if (item != null && item.getId() != null) {
    	   			try {
                        // XXX: Why does this need to be itemView?
                        // Using "view" should suffice.
    					itemView.onItemSelected(position, item);
    	            } catch (RemoteException e) {
    	                Log.e(getTag(), "Error from default action for '" + item + "': " + e);
    	            }
    			}
    		}
    	});

        mListView.setOnScrollListener(new ScrollListener());

        // Get an ImageFetcher to scale artwork to the size of the icon view.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int iconSize = (int) (metrics.density *
                Math.max(R.dimen.album_art_icon_height, R.dimen.album_art_icon_width));
        mImageFetcher = new ImageFetcher(this, iconSize);
        ImageCacheParams imageCacheparams = new ImageCacheParams(this, "thumbnails");
        mImageFetcher.addImageCache(getSupportFragmentManager(), imageCacheparams);

        // Delegate context menu creation to the adapter.
        mListView.setOnCreateContextMenuListener(getItemAdapter());
	}

    public ImageFetcher getImageFetcher() {
        return mImageFetcher;
    }

	/**
	 * @return A new view logic to be used by this activity
	 */
	abstract protected SqueezerItemView<T> createItemView();

    @Override
    public final boolean onContextItemSelected(MenuItem menuItem) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) menuItem.getMenuInfo();

        if (getService() != null) {
            try {
                return itemAdapter.doItemContext(menuItem, menuInfo.position);
            } catch (RemoteException e) {
                Log.e(getTag(), "Error context menu action '" + menuInfo + "' for '"
                        + menuInfo.position + "': " + e);
            }
        }

        return super.onContextItemSelected(menuItem);
    }

	@Override
	protected void onServiceConnected() throws RemoteException {
		registerCallback();
		orderItems();
	}

	@Override
    public void onPause() {
        if (getService() != null) {
        	try {
				unregisterCallback();
			} catch (RemoteException e) {
                Log.e(getTag(), "Error unregistering list callback: " + e);
			}
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mImageFetcher != null) {
            mImageFetcher.closeCache();
        }
    }

    /**
     * @return The current {@link SqueezerItemView}, creating it if necessary
     */
    public SqueezerItemView<T> getItemView() {
        return itemView == null ? (itemView = createItemView()) : itemView;
    }

    /**
     * @return The current {@link SqueezerItemAdapter}, creating it if
     *         necessary.
     */
    public SqueezerItemAdapter<T> getItemAdapter() {
        return itemAdapter == null ? (itemAdapter = createItemListAdapter(getItemView()))
                : itemAdapter;
    }

	/**
	 * @return The {@link ListView} used by this activity
	 */
    public ListView getListView() {
        return mListView;
    }


    protected SqueezerItemAdapter<T> createItemListAdapter(SqueezerItemView<T> itemView) {
        return new SqueezerItemListAdapter<T>(itemView, mImageFetcher);
	}

	/**
	 * Order items from the start, and prepare an adapter to receive them
	 * @throws RemoteException
	 */
	public void orderItems() {
		reorderItems();
        mListView.setVisibility(View.GONE);
		loadingLabel.setVisibility(View.VISIBLE);
		clearItemListAdapter();
	}

	public void onItemsReceived(final int count, final int start, final List<T> items) {
		getUIThreadHandler().post(new Runnable() {
			public void run() {
                mListView.setVisibility(View.VISIBLE);
				loadingLabel.setVisibility(View.GONE);
				getItemAdapter().update(count, start, items);
			}
		});
	}

	/**
	 * Set the adapter to handle the display of the items, see also {@link #setListAdapter(android.widget.ListAdapter)}
	 * @param listAdapter
	 */
	private void clearItemListAdapter() {
        mListView.setAdapter(getItemAdapter());
	}

    protected class ScrollListener extends SqueezerItemListActivity.ScrollListener {
        ScrollListener() {
            super();
        }

        /**
         * Pauses cache disk fetches if the user is flinging the list, or if
         * their finger is still on the screen.
         */
        @Override
        public void onScrollStateChanged(AbsListView listView, int scrollState) {
            super.onScrollStateChanged(listView, scrollState);

            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING ||
                scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                mImageFetcher.setPauseWork(true);
            } else {
                mImageFetcher.setPauseWork(false);
            }
        }
    }
}
