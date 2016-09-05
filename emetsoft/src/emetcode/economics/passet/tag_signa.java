package emetcode.economics.passet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.transfer_crypto;
import emetcode.util.devel.logger;

public class tag_signa {

	static final boolean IN_DEBUG_1 = true; // did verif
	static final boolean IN_DEBUG_2 = false; // adding giv_box
	static final boolean IN_DEBUG_3 = false; // loading giv_kbox

	public static final String NO_PREV_KEY = "Invalid previous key";
	public static final String NO_SIGNATURE = "Invalid signature";

	static final String previous_key_field = " previous key";
	static final String signature_field = " signature";

	private transfer_crypto giv_kbox;

	public tag_signa() {
		init_tag_signa();
	}

	public tag_signa(tag_signa orig) {
		init_tag_signa();
		if (orig == null) {
			throw new bad_passet(2);
		}
	}

	void init_tag_signa() {
		giv_kbox = null;
	}

	private void add_signa_lines_to(List<String> txt, String title) {

		String prev_kbox_key = NO_PREV_KEY;
		if (has_giv_box()) {
			prev_kbox_key = giv_kbox.get_cryptobox_key();
			if (IN_DEBUG_2) {
				logger.debug("adding_prev_kbox_key=\n" + prev_kbox_key + "\n");
			}
			if (prev_kbox_key == null) {
				throw new bad_passet(2);
			}
		}
		parse.add_next_field_to(txt, title, previous_key_field, prev_kbox_key);

		byte[] sgn_sha = parse.calc_sha_lines(txt);
		String trans_signa = NO_SIGNATURE;
		if (has_giv_box()) {
			trans_signa = giv_kbox.get_passet_signature(sgn_sha);
		}

		long mini_sha = 0;
		if (IN_DEBUG_1) {
			mini_sha = convert.calc_minisha_long(sgn_sha);
		}

		parse.add_next_field_to(txt, title, signature_field, trans_signa);
		if (has_giv_box()) {
			boolean sgn_ok = giv_kbox.check_passet_signature(sgn_sha,
					trans_signa);
			if (!sgn_ok) {
				throw new bad_passet(2);
			}
			if (IN_DEBUG_1) {
				// logger.info(txt);
				logger.debug("added_signature_ok for=" + get_rela_recep_file()
						+ " mini_sha=" + mini_sha + " txt_sz=" + txt.size());
			}
		}

		parse.check_line_list(txt);
	}

	private void init_signa_with(ListIterator<String> it1, String title,
			List<String> all_lines) {
		String prev_kbox_key = parse.get_next_field_from(it1, title,
				previous_key_field);
		if (has_giv_box()) {
			if (NO_PREV_KEY.equals(prev_kbox_key)) {
				throw new bad_passet(2, "prev_key_str=" + prev_kbox_key);
			}
			giv_kbox.decrypt_cryptobox(prev_kbox_key);
		}

		int pre_signa_idx = it1.nextIndex();
		byte[] sgn_sha = parse.calc_sha_lines(all_lines, 0, pre_signa_idx);

		String signa_str = parse.get_next_field_from(it1, title,
				signature_field);

		long mini_sha = 0;
		if (IN_DEBUG_1) {
			mini_sha = convert.calc_minisha_long(sgn_sha);
		}

		if (has_giv_box()) {
			File rela_ff = null;
			if (IN_DEBUG_1) {
				rela_ff = get_rela_recep_file();
			}
			boolean sgn_ok = giv_kbox
					.check_passet_signature(sgn_sha, signa_str);
			if (!sgn_ok) {
				if (IN_DEBUG_1) {
					// logger.info(all_lines);
					logger.debug("BAD_SIGNATURE_verif for=" + rela_ff
							+ " mini_sha=" + mini_sha + " txt_sz="
							+ all_lines.size());
				}
				throw new bad_passet(2);
			}
			if (IN_DEBUG_1) {
				logger.debug("SIGNATURE_OK for=" + rela_ff + " mini_sha="
						+ mini_sha);
			}
		}
		if (IN_DEBUG_1) {
			if (giv_kbox == null) {
				//String stk = logger.get_stack_str();
				logger.debug("NULL_GIV_KBOX. mini_sha=" + mini_sha);
			}
		}
	}

	void spend_in(File spent_dd, File pvks_base) {
		if (giv_kbox == null) {
			return;
		}
		giv_kbox.spend_puk(spent_dd, pvks_base);
	}

	private File get_rela_recep_file() {
		return giv_kbox.get_cryptobox_file(new File("."));
	}

	File get_recep_file(File base_dd) {
		return giv_kbox.get_cryptobox_file(base_dd);
	}

	private boolean has_giv_box() {
		return (giv_kbox != null);
	}

	static void spend_all(List<tag_signa> all_signa, File spent_dd,
			File pvks_base) {
		for (tag_signa tg_sgn : all_signa) {
			tg_sgn.spend_in(spent_dd, pvks_base);
		}
	}

	static void add_list_signa_lines_to(List<tag_signa> all_signa,
			List<String> txt, String title, boolean is_root) {
		for (tag_signa oo : all_signa) {
			if (!is_root && !oo.has_giv_box()) {
				throw new bad_passet(2);
			}
			oo.add_signa_lines_to(txt, title);
		}
	}

	static boolean all_signas_have_giv_box(List<tag_signa> all_signa,
			boolean ck_kbox_key) {
		for (tag_signa oo : all_signa) {
			if (!oo.has_giv_box()) {
				return false;
			}
			if (!oo.giv_kbox.has_cryptobox()) {
				return false;
			}
			if (ck_kbox_key) {
				if (!oo.giv_kbox.has_cryptobox_key()) {
					throw new bad_passet(2);
				}
			}
		}
		return true;
	}

	static void init_list_signa_with(List<tag_signa> all_signa,
			ListIterator<String> it1, String title, List<String> all_lines,
			boolean is_root) {
		for (tag_signa oo : all_signa) {
			if (!is_root && !oo.has_giv_box()) {
				// throw new bad_passet(2);
			}
			oo.init_signa_with(it1, title, all_lines);
		}
	}

	static List<tag_signa> get_list_signa(int sz) {
		List<tag_signa> lst = new ArrayList<tag_signa>();
		for (int aa = 0; aa < sz; aa++) {
			tag_signa oo = new tag_signa();
			lst.add(oo);
		}
		return lst;
	}

	static void init_signas_with_prev_list(List<tag_signa> all_signa,
			List<tag_transfer> all_prev, int idx) {
		tag_transfer.ck_prev_sz_and_idx(all_prev, idx);
		all_signa.clear();
		for (tag_transfer prev_tra : all_prev) {
			tag_signa ii = new tag_signa();
			int sg_idx = -1;
			if (prev_tra.is_direct_transfer()) {
				// sg_idx = prev_tra.get_passet_idx();
				sg_idx = 0;
			} else if (prev_tra.is_multi_in_transfer()) {
				sg_idx = 0;
			} else if (prev_tra.is_multi_out_transfer()) {
				if (!prev_tra.is_valid_idx(idx)) {
					throw new bad_passet(2);
				}
				sg_idx = idx;
			}

			ii.giv_kbox = prev_tra.get_receiver_kbox(sg_idx);
			if (IN_DEBUG_2) {
				logger.debug("adding_kbox=" + ii.giv_kbox);
			}
			all_signa.add(ii);
		}
	}
}
