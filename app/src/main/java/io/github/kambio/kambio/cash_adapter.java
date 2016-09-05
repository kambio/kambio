package io.github.kambio.kambio;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.github.kambio.kambio.R;
import emetcode.economics.passet.deno_count;
import emetcode.economics.passet.deno_counter;
import emetcode.economics.passet.iso;
import emetcode.economics.passet.tag_denomination;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import android.net.Uri;

public class cash_adapter extends BaseAdapter {
	public static final String LOGTAG = "cash_adapter";

	public static final int ONE_MORE_NOTE = 200;
	public static final int ONE_LESS_NOTE = 201;

	app_base app;
	cash_activity ctx;
	LayoutInflater row_inflater;

	deno_count[] all_denos;
	deno_count[] chosen_denos;
	deno_count[] working_denos;

	BigDecimal sum_denos;

	int selected_pos;
	int selected_action;

	public cash_adapter(cash_activity the_ctx, deno_counter the_cter) {

		app = (app_base) the_ctx.getApplicationContext();

		ctx = the_ctx;

		selected_pos = -1;
		selected_action = -1;

		row_inflater = LayoutInflater.from(ctx);

		all_denos = null;
		chosen_denos = null;
		working_denos = null;
		sum_denos = BigDecimal.ZERO;
		fill_denos(the_cter);
	}

	private void set_view_sizes(view_holder holder) {
		holder.icon.getLayoutParams().height = app.ca_cash_image_list_height;
		holder.icon.getLayoutParams().width = app.ca_cash_image_list_width;

		holder.deno.setTextSize(app.ca_cash_deno_text_sz);
		holder.chosen.setTextSize(app.ca_cash_chosen_text_sz);
		holder.can_give.setTextSize(app.ca_cash_can_give_text_sz);
		holder.have.setTextSize(app.ca_cash_have_text_sz);
	}

	private void set_empty(view_holder holder) {
		holder.icon.setImageResource(R.drawable.pick_drw);
		holder.deno.setText("");
		holder.chosen.setText("");
		holder.can_give.setText("");
		holder.have.setText("");
	}

	deno_count get_count(int idx) {
		if (working_denos == null) {
			return null;
		}
		if (idx < 0) {
			return null;
		}
		if (idx >= working_denos.length) {
			return null;
		}
		return working_denos[idx];
	}

	private boolean in_cho() {
		return (working_denos == chosen_denos);
	}

	private void fix_sum() {
		sum_denos = sum_denos.stripTrailingZeros();
		if (sum_denos.compareTo(BigDecimal.ZERO) == 0) {
			sum_denos = BigDecimal.ZERO;
		}
	}

	private void set_deno(view_holder holder, int idx) {
		set_view_sizes(holder);
		deno_count dc = get_count(idx);
		if (dc == null) {
			set_empty(holder);
			return;
		}

		if (idx == selected_pos) {
			boolean was_zero = is_sum_zero();

			if (selected_action == ONE_MORE_NOTE) {
				if (dc.num_can_give > 0) {
					dc.num_can_give--;
					dc.num_chosen++;

					BigDecimal deno_val = dc.deno.get_decimal();
					sum_denos = sum_denos.add(deno_val);
				}
			}
			if (selected_action == ONE_LESS_NOTE) {
				if (dc.num_chosen > 0) {
					dc.num_can_give++;
					dc.num_chosen--;

					BigDecimal deno_val = dc.deno.get_decimal();
					sum_denos = sum_denos.subtract(deno_val);
				}
			}

			fix_sum();

			if (was_zero != is_sum_zero()) {
				ctx.update_chosen_item(is_sum_zero());
			}

			if (ctx != null) {
				int currcy_idx = ctx.cash_counter.currency_idx;
				String tot_str = sum_denos.toPlainString() + app_base.CHAR_SPC
						+ iso.get_currency_code(currcy_idx);
				ctx.total_chosen_txt.setText(tot_str);

				if (in_cho() && is_sum_zero()) {
					if (idx != 0) {
						throw new bad_cashapp(2);
					}
					selected_pos = -1;
					ctx.switch_only_chosen();
					working_denos = all_denos;
					chosen_denos = null;
					dc = get_count(idx);
				}
			}

			if (in_cho() && (dc.num_chosen == 0)) {
				update_chosen_denos();
				selected_pos = -1;
				dc = get_count(idx);
			}

			if (dc == null) {
				set_empty(holder);
				return;
			}
		}

		String num_deno_str = dc.deno.get_number_denomination();
		// num_deno_str = num_deno_str.replace(' ', '\u200a');

		set_img(holder.icon, (image_data) dc.cnt_data);
		set_txt(holder.deno, num_deno_str);
		set_txt(holder.chosen, "" + dc.num_chosen);
		set_txt(holder.can_give, "" + dc.num_can_give);
		set_txt(holder.have, "" + dc.num_have);

		if (idx == selected_pos) {
			holder.chosen
					.setBackgroundResource(android.R.color.holo_blue_light);
		} else if (dc.num_chosen > 0) {
			holder.chosen
					.setBackgroundResource(android.R.color.holo_green_light);
		} else {
			holder.chosen.setBackgroundResource(android.R.color.white);
		}
	}

	private void set_img(ImageView vw, image_data dat) {
		if (dat == null) {
			vw.setImageResource(R.drawable.pick_drw);
			return;
		}
		File ff = dat.img_file;
		if (ff != null) {
			Uri uu = Uri.fromFile(ff);
			vw.setImageURI(uu);
			return;
		}
		int res_id = dat.img_res;
		if (res_id != -1) {
			vw.setImageResource(res_id);
			return;
		}
		vw.setImageResource(R.drawable.pick_drw);
	}

	private void set_txt(TextView vw, String txt) {
		if (txt == null) {
			vw.setText("");
			return;
		}
		vw.setText(txt);
	}

	public int getCount() {
		if (working_denos != null) {
			return working_denos.length;
		}
		return 0;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View old_view, ViewGroup parent) {
		view_holder holder;

		if (old_view == null) {
			old_view = row_inflater.inflate(R.layout.cash_item, null);

			holder = new view_holder();
			holder.icon = (ImageView) old_view
					.findViewById(R.id.cash_item_icon);
			holder.deno = (TextView) old_view.findViewById(R.id.cash_item_deno);
			holder.chosen = (TextView) old_view
					.findViewById(R.id.cash_item_chosen);
			holder.can_give = (TextView) old_view
					.findViewById(R.id.cash_item_can_give);
			holder.have = (TextView) old_view.findViewById(R.id.cash_item_have);

			old_view.setTag(holder);
		} else {
			holder = (view_holder) old_view.getTag();
		}

		holder.pos = position;
		set_deno(holder, position);

		if (position == selected_pos) {
			old_view.setBackgroundResource(android.R.color.holo_blue_light);
			holder.can_give
					.setBackgroundResource(android.R.color.holo_orange_light);
			holder.have
					.setBackgroundResource(android.R.color.holo_orange_light);
		} else {
			old_view.setBackgroundResource(android.R.color.white);
			holder.can_give.setBackgroundResource(android.R.color.white);
			holder.have.setBackgroundResource(android.R.color.white);
		}

		return old_view;
	}

	static class view_holder {
		int pos = -1;
		ImageView icon = null;
		TextView deno = null;
		TextView chosen = null;
		TextView can_give = null;
		TextView have = null;
	}

	static class image_data {
		File img_file = null;
		int img_res = -1;
	}

	private void fill_denos(deno_counter user_cter) {
		all_denos = null;
		working_denos = null;
		sum_denos = BigDecimal.ZERO;

		if (user_cter == null) {
			return;
		}
		if (!app.ca_has_passet()) {
			return;
		}
		int w_currcy = app.ca_get_passet().get_working_currency();
		if ((app.ca_notes_image_files_currency_idx != w_currcy)
				|| (app.ca_notes_image_files == null)) {
			fill_notes_image_files();
		}

		deno_counter full_cter = app.ca_notes_image_files;
		if (full_cter == null) {
			throw new bad_cashapp(2);
		}

		List<deno_count> all_cnt = new ArrayList<deno_count>();

		tag_denomination curr_dd = tag_denomination.first_deno(w_currcy);
		while (true) {
			deno_count deco = user_cter.get_deno_count(curr_dd, false);
			if (deco != null) {
				deno_count base_deco = full_cter.get_deno_count(curr_dd, false);
				deco.cnt_data = base_deco.cnt_data;

				BigDecimal cho_val = deco.get_chosen_decimal();
				sum_denos = sum_denos.add(cho_val);

				all_cnt.add(deco);
			}

			if (curr_dd.is_last_deno()) {
				break;
			}
			curr_dd.inc_deno();
		}

		fix_sum();

		if (all_cnt.isEmpty()) {
			return;
		}

		all_denos = all_cnt.toArray(new deno_count[0]);
		working_denos = all_denos;
	}

	private void fill_notes_image_files() {
		app.ca_notes_image_files = null;
		app.ca_notes_image_files_currency_idx = -1;

		int w_currcy = app.ca_get_passet().get_working_currency();

		deno_counter cter = new deno_counter(w_currcy);
		tag_denomination curr_dd = tag_denomination.first_deno(w_currcy);
		while (true) {
			deno_count deco = cter.get_deno_count(curr_dd);
			image_data dat = new image_data();
			deco.cnt_data = dat;

			if (deco.deno.currency_idx != w_currcy) {
				throw new bad_cashapp(2);
			}

			File img_ff = app.ca_get_note_image_file(deco.deno);
			if (img_ff.exists()) {
				dat.img_file = img_ff;
			}

			String img_res_nam = app_base.ca_get_note_image_res_name(deco.deno);
			int res_id = ctx.getResources().getIdentifier(img_res_nam,
					"drawable", ctx.getPackageName());

			if (res_id != 0) {
				dat.img_res = res_id;
			} else {
				dat.img_res = R.drawable.base_cashnote_drw;
			}

			if (curr_dd.is_last_deno()) {
				break;
			}
			curr_dd.inc_deno();
		}

		app.ca_notes_image_files = cter;
		app.ca_notes_image_files_currency_idx = w_currcy;
	}

	boolean is_sum_zero() {
		return (sum_denos == BigDecimal.ZERO);
	}

	void update_chosen_denos() {
		if (is_sum_zero()) {
			selected_pos = -1;
			working_denos = all_denos;
			chosen_denos = null;
			if (ctx != null) {
				ctx.only_chosen = false;
				misce.show_message(ctx, R.string.help_sum_zero, null, false);
			}
			return;
		}
		if (working_denos == null) {
			chosen_denos = null;
			working_denos = null;
			return;
		}
		List<deno_count> cho_cnt = new ArrayList<deno_count>();
		for (deno_count dc : working_denos) {
			if (dc.num_chosen > 0) {
				cho_cnt.add(dc);
			}
		}
		chosen_denos = cho_cnt.toArray(new deno_count[0]);
		working_denos = chosen_denos;
	}

}
