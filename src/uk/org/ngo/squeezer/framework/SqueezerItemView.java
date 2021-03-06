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


import uk.org.ngo.squeezer.util.ImageFetcher;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.Adapter;


/**
 * Defines view logic for a {@link SqueezerItem}
 *
 * @author Kurt Aaholst
 *
 * @param <T>
 *            Denotes the class of the item this class implements view logic for
 */
public interface SqueezerItemView<T extends SqueezerItem> {

	/**
	 * @return The activity associated with this view logic
	 */
	SqueezerItemListActivity getActivity();

	/**
	 * @return {@link Resources#getQuantityString(int, int)}
	 */
	String getQuantityString(int quantity);

    /**
     * Get a View that displays the data at the specified position in the data
     * set. {@see Adapter#getView(int, View, android.view.ViewGroup)}
     * 
     * @param convertView The old view to reuse, per
     *            {@link Adapter#getView(int, View, android.view.ViewGroup)}
     * @param item The item to display.
     * @param imageFetcher An {@link ImageFetcher} configured to load image
     *            thumbnails.
     * @return A View corresponding to the data from the specified item.
     */
    View getAdapterView(View convertView, T item, ImageFetcher mImageFetcher);

    /**
     * <p>
     * Called by {@link Adapter#getView(int, View, ViewGroup)}
     * 
     * @param convertView
     * @param item
     * @return
     */
    View getAdapterView(View convertView, String label);

	/**
	 * @return The generic argument of the implementation
	 */
    Class<T> getItemClass();

    /**
     * @return the creator for the current {@link SqueezerItem} implementation
     */
	Creator<T> getCreator();

	/**
	 * Implement the action to be taken when an item is selected.
	 * @param index Position in the list of the selected item.
	 * @param item The selected item. This may be null if
	 * @throws RemoteException
	 */
	void onItemSelected(int index, T item) throws RemoteException;

    /**
     * Creates the context menu, and sets the menu's title to the name of the
     * item that it is the context menu.
     * <p>
     * Subclasses with no context menu should override this method and do
     * nothing.
     * <p>
     * Subclasses with a context menu should call this method, then inflate
     * their context menu and perform any adjustments to it before returning.
     * 
     * @param menu
     * @param v
     * @param menuInfo
     * @see OnCreateContextMenuListener#onCreateContextMenu(ContextMenu, View,
     *      android.view.ContextMenu.ContextMenuInfo)
     */
    public void onCreateContextMenu(ContextMenu menu, View v,
            SqueezerItemView.ContextMenuInfo menuInfo);

	/**
	 * Perform the selected action from the context menu for the selected item.
	 *
	 * @param selectedItem The item the context menu was generated for
	 * @param menuItem The selected menu action
	 * @return True if the action was consumed
	 * @throws RemoteException
	 * @see {@link Activity#onContextItemSelected(MenuItem)}
	 */
	public boolean doItemContext(MenuItem menuItem, int index, T selectedItems) throws RemoteException;

    /**
     * Extra menu information provided to the
     * {@link android.view.View.OnCreateContextMenuListener#onCreateContextMenu(ContextMenu, View, ContextMenuInfo) }
     * callback when a context menu is brought up for this SqueezerItemView.
     */
    public static class ContextMenuInfo implements ContextMenu.ContextMenuInfo {

        /**
         * The position in the adapter for which the context menu is being
         * displayed.
         */
        public int position;

        /**
         * The {@link SqueezerItem} for which the context menu is being
         * displayed.
         */
        public SqueezerItem item;

        /**
         * The {@link SqueezerItemAdapter} that is bridging the content to the
         * listview.
         */
        public SqueezerItemAdapter<?> adapter;

        /**
         * A {@link android.view.MenuInflater} that can be used to inflate a
         * menu resource.
         */
        public MenuInflater menuInflater;

        public ContextMenuInfo(int position, SqueezerItem item, SqueezerItemAdapter<?> adapter,
                MenuInflater menuInflater) {
            this.position = position;
            this.item = item;
            this.adapter = adapter;
            this.menuInflater = menuInflater;
        }
    }
}
