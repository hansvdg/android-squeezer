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

package uk.org.ngo.squeezer.itemlists;

import java.util.HashMap;
import java.util.Map;

import uk.org.ngo.squeezer.R;
import uk.org.ngo.squeezer.framework.SqueezerBaseItemView;
import uk.org.ngo.squeezer.model.SqueezerPlayer;
import uk.org.ngo.squeezer.util.ImageFetcher;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SqueezerPlayerView extends SqueezerBaseItemView<SqueezerPlayer> {
	private final LayoutInflater layoutInflater;
    private SqueezerPlayerListActivity activity;
	private static final Map<String, Integer> modelIcons = initializeModelIcons();

	public SqueezerPlayerView(SqueezerPlayerListActivity activity) {
		super(activity);
		this.activity = activity;
		layoutInflater = activity.getLayoutInflater();
	}

	@Override
    public View getAdapterView(View convertView, SqueezerPlayer item, ImageFetcher imageFetcher) {
		ViewHolder viewHolder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = layoutInflater.inflate(R.layout.icon_large_row_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.label = (TextView) convertView.findViewById(R.id.label);
			viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(viewHolder);
		} else
			viewHolder = (ViewHolder) convertView.getTag();

		viewHolder.label.setText(item.getName());
		viewHolder.label.setTextAppearance(getActivity(), item.equals(activity.getActivePlayer()) ? R.style.SqueezerCurrentTextItem : R.style.SqueezerTextItem);
		viewHolder.icon.setImageResource(getModelIcon(item.getModel()));

		convertView.setBackgroundResource(item.equals(activity.getActivePlayer()) ? R.drawable.list_item_background_current : R.drawable.list_item_background_normal);
		return convertView;
	}

	public void onItemSelected(int index, SqueezerPlayer item) throws RemoteException {
		getActivity().getService().setActivePlayer(item);
		getActivity().finish();
	};

	public String getQuantityString(int quantity) {
		return getActivity().getResources().getQuantityString(R.plurals.player, quantity);
	}

	private static Map<String, Integer> initializeModelIcons() {
		Map<String, Integer> modelIcons = new HashMap<String, Integer>();
		modelIcons.put("baby", R.drawable.icon_baby);
		modelIcons.put("boom", R.drawable.icon_boom);
		modelIcons.put("fab4", R.drawable.icon_fab4);
		modelIcons.put("receiver", R.drawable.icon_receiver);
		modelIcons.put("controller", R.drawable.icon_controller);
		modelIcons.put("sb1n2", R.drawable.icon_sb1n2);
		modelIcons.put("sb3", R.drawable.icon_sb3);
		modelIcons.put("slimp3", R.drawable.icon_slimp3);
		modelIcons.put("softsqueeze", R.drawable.icon_softsqueeze);
		modelIcons.put("squeezeplay", R.drawable.icon_squeezeplay);
		modelIcons.put("transporter", R.drawable.icon_transporter);
		return modelIcons;
	}

	private int getModelIcon(String model) {
		Integer icon = modelIcons.get(model);
		return (icon != null ? icon : R.drawable.icon_blank);
	}

	private static class ViewHolder {
		TextView label;
		ImageView icon;
	}

}
