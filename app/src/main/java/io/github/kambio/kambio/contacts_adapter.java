package io.github.kambio.kambio;

import java.io.File;

import io.github.kambio.kambio.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import android.net.Uri;

public class contacts_adapter extends BaseAdapter {
	public static final String LOGTAG = "contacts_adapter";

	Context ctx;
	LayoutInflater row_inflater;

	int[] all_kind_img_resrce;
	File[] all_img_files;
	int[] all_img_resrce;
	int[] all_text_resrce;
	String[] all_text;

	int selected_pos;

	int txt_sz;
	int img_height;
	int img_width;

	public contacts_adapter(Context the_ctx, int the_txt_sz, int the_img_height,
			int the_img_width) {

		ctx = the_ctx;

		selected_pos = -1;

		txt_sz = the_txt_sz;
		img_height = the_img_height;
		img_width = the_img_width;

		row_inflater = LayoutInflater.from(ctx);

		all_kind_img_resrce = null;
		all_img_files = null;
		all_img_resrce = null;
		all_text_resrce = null;
		all_text = null;
	}

	private void set_txt(TextView vw, int idx) {
		if ((all_text_resrce != null) && (idx >= 0) && (all_text_resrce.length > idx)) {
			int resid = all_text_resrce[idx];
			if(resid != -1){
				vw.setText(resid);
				return;
			}
		}
		String txt = "";
		if ((all_text != null) && (idx >= 0) && (all_text.length > idx)) {
			txt = all_text[idx];
			vw.setText(txt);
			return;
		}
		vw.setText(txt);
	}

	private void set_kind_img(ImageView vw, int idx) {
		int[] all_res = all_kind_img_resrce;
		if ((all_res != null) && (idx >= 0) && (all_res.length > idx)) {
			int res_id = all_res[idx];
			if (res_id != -1) {
				vw.setImageResource(res_id);
				return;
			}
		}
		vw.setImageResource(R.drawable.pick_drw);
		//vw.setImageResource(android.R.color.white);
	}

	private void set_img(ImageView vw, int idx) {
		File[] ffs = all_img_files;
		if ((ffs != null) && (idx >= 0) && (ffs.length > idx)) {
			File ff = ffs[idx];
			if (ff != null) {
				Uri uu = Uri.fromFile(ff);
				vw.setImageURI(uu);
				return;
			}
		}
		int[] all_res = all_img_resrce;
		if ((all_res != null) && (idx >= 0) && (all_res.length > idx)) {
			int res_id = all_res[idx];
			if (res_id != -1) {
				vw.setImageResource(res_id);
				return;
			}
		}
		vw.setImageResource(R.drawable.pick_drw);
	}

	public int getCount() {
		int max_sz = 0;
		if ((all_img_files != null) && (all_img_files.length > max_sz)) {
			max_sz = all_img_files.length;
		}
		if ((all_img_resrce != null) && (all_img_resrce.length > max_sz)) {
			max_sz = all_img_resrce.length;
		}
		if ((all_text != null) && (all_text.length > max_sz)) {
			max_sz = all_text.length;
		}

		return max_sz;
	}

	/**
	 * Since the data comes from an array, just returning the index is sufficent
	 * to get at the data. If we were using a more complex data structure, we
	 * would return whatever object represents one row in the list.
	 * 
	 * @see android.widget.ListAdapter#getItem(int)
	 */
	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View old_view, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unneccessary
		// calls
		// to findViewById() on each row.
		view_holder holder;

		if (old_view == null) {
			old_view = row_inflater.inflate(R.layout.contact_item, null);

			holder = new view_holder();
			holder.kind_icon = (ImageView) old_view.findViewById(R.id.kind_icon);
			holder.icon = (ImageView) old_view.findViewById(R.id.icon);
			holder.text = (TextView) old_view.findViewById(R.id.text);

			old_view.setTag(holder);
		} else {
			holder = (view_holder) old_view.getTag();
		}

		set_kind_img(holder.kind_icon, position);
		set_img(holder.icon, position);
		set_txt(holder.text, position);

		holder.icon.getLayoutParams().height = img_height;
		holder.icon.getLayoutParams().width = img_width;
		holder.text.setTextSize(txt_sz);

		if (position == selected_pos) {
			old_view.setBackgroundResource(android.R.color.holo_blue_light);
		} else {
			old_view.setBackgroundResource(android.R.color.white);
		}

		return old_view;
	}

	static class view_holder {
		ImageView kind_icon;
		ImageView icon;
		TextView text;
	}
}
