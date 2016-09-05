package io.github.kambio.kambio;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.economics.passet.paccount;
import emetcode.economics.passet.tag_denomination;
import emetcode.net.netmix.nx_conn_id;

public class debug_funcs {
	public static final String LOGTAG = "debug_funcs";

	public static final String UNKNOWN_STR = "unknown";

	public static paccount create_dbg_channel(paccount loc_pss, String test_nm,
			key_owner owr) {
		nx_conn_id test_coid = new nx_conn_id();
		paccount rem_pss = loc_pss.get_sub_paccount(test_coid);
		rem_pss.curr_user.legal_name = test_nm;
		rem_pss.write_current_user(owr);

		File co_ff = loc_pss.get_dir_base().get_coid_file(test_coid);
		try {
			co_ff.createNewFile();
		} catch (IOException e) {
			Log.d(LOGTAG, "Cannot create coid file " + co_ff);
		}

		return rem_pss;
	}

	public static void create_all_dbg_channels(Context ctx, paccount loc_pss,
			key_owner owr) {
		for (int aa = 0; aa < 3; aa++) {
			String nm = "chan_" + aa;
			paccount pp = create_dbg_channel(loc_pss, nm, owr);
			File im_ff = pp.get_current_user_image_file();
			byte[] imd_dat = misce.read_futbol_image(ctx, aa + 1);
			if (imd_dat != null) {
				mem_file.concurrent_write_encrypted_bytes(im_ff, null, imd_dat);
			}
		}
	}

	public static void create_all_test_passets(paccount loc_pss,
			int curr_idx, key_owner owr) {

		int num = 1;
		tag_denomination deno = new tag_denomination(curr_idx);

		num = 1;
		deno.multiplier = 1;
		deno.ten_exponent = 0;
		loc_pss.issue_passets(num, owr, deno, null, null);

		num = 3;
		deno.multiplier = 2;
		deno.ten_exponent = 1;
		loc_pss.issue_passets(num, owr, deno, null, null);

		num = 2;
		deno.multiplier = 5;
		deno.ten_exponent = 2;
		loc_pss.issue_passets(num, owr, deno, null, null);
	}
}
