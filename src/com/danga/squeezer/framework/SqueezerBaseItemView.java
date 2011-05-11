package com.danga.squeezer.framework;

import java.lang.reflect.Field;

import android.os.RemoteException;
import android.os.Parcelable.Creator;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.danga.squeezer.R;
import com.danga.squeezer.ReflectUtil;
import com.danga.squeezer.Util;
import com.danga.squeezer.itemlists.SqueezerAlbumListActivity;
import com.danga.squeezer.itemlists.SqueezerArtistListActivity;
import com.danga.squeezer.itemlists.SqueezerSongListActivity;

public abstract class SqueezerBaseItemView<T extends SqueezerItem> implements SqueezerItemView<T> {
	protected static final int CONTEXTMENU_BROWSE_SONGS = 0;
	protected static final int CONTEXTMENU_BROWSE_ALBUMS = 1;
	protected static final int CONTEXTMENU_BROWSE_ARTISTS = 2;
	protected static final int CONTEXTMENU_PLAY_ITEM = 3;
	protected static final int CONTEXTMENU_ADD_ITEM = 4;
	protected static final int CONTEXTMENU_INSERT_ITEM = 5;
	protected static final int CONTEXTMENU_BROWSE_ALBUM_SONGS = 6;
	protected static final int CONTEXTMENU_BROWSE_ARTIST_ALBUMS = 7;
	protected static final int CONTEXTMENU_BROWSE_ARTIST_SONGS = 8;

	private SqueezerItemListActivity activity;
	private SqueezerItemAdapter<T> adapter;
	private Class<T> itemClass;
	private Creator<T> creator;

	public SqueezerBaseItemView(SqueezerItemListActivity activity) {
		this.activity = activity;
	}

	public SqueezerItemListActivity getActivity() {
		return activity;
	}
	
	public SqueezerItemAdapter<T> getAdapter() {
		return adapter;
	}

	public void setAdapter(SqueezerItemAdapter<T> adapter) {
		this.adapter = adapter;
	}

	@SuppressWarnings("unchecked")
	public Class<T> getItemClass() {
		if (itemClass  == null) {
			itemClass = (Class<T>) ReflectUtil.getGenericClass(getClass(), SqueezerItemView.class, 0);
			if (itemClass  == null)
				throw new RuntimeException("Could not read generic argument for: " + getClass());
		}
		return itemClass;
	}

	@SuppressWarnings("unchecked")
	public Creator<T> getCreator() {
		if (creator == null) {
			Field field;
			try {
				field = getItemClass().getField("CREATOR");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			try {
				creator = (Creator<T>) field.get(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return creator;
	}

	public View getAdapterView(View convertView, T item) {
		return Util.getListItemView(getActivity(), convertView, item.getName());
	}
	
	public boolean doItemContext(MenuItem menuItem, int index, T selectedItem) throws RemoteException {
		switch (menuItem.getItemId()) {
		case CONTEXTMENU_BROWSE_SONGS:
			SqueezerSongListActivity.show(activity, selectedItem);
			return true;
		case CONTEXTMENU_BROWSE_ALBUMS:
			SqueezerAlbumListActivity.show(activity, selectedItem);
			return true;
		case CONTEXTMENU_BROWSE_ARTISTS:
			SqueezerArtistListActivity.show(activity, selectedItem);
			return true;
		case CONTEXTMENU_PLAY_ITEM:
			if (activity.play(selectedItem))
				Toast.makeText(activity, activity.getString(R.string.ITEM_PLAYING, selectedItem.getName()), Toast.LENGTH_SHORT).show();
			return true;
		case CONTEXTMENU_ADD_ITEM:
			if (activity.add(selectedItem))
				Toast.makeText(activity, activity.getString(R.string.ITEM_ADDED, selectedItem.getName()), Toast.LENGTH_SHORT).show();
			return true;
		case CONTEXTMENU_INSERT_ITEM:
			if (activity.insert(selectedItem))
				Toast.makeText(activity, activity.getString(R.string.ITEM_INSERTED, selectedItem.getName()), Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

}