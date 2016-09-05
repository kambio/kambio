package emetcode.economics.passet;

import java.io.File;

import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.net_funcs;

public class TEST_create_refs {

	public static void main(String[] args) {

		test_choose_refs(args);
		System.out.println("END_OF_TESTS.");
	}

	public static void test_choose_refs(String[] args) {
		if (args.length < 6) {
			System.out
					.println("CANNOT CREATE REFS. Invalid args in test_choose_refs.");
			return;
		}

		File top_dir = file_funcs.as_canonical(new File(args[0]));

		byte[] key1 = args[1].getBytes(config.UTF_8);
		key_owner owr = new key_owner(key1);

		int min = Integer.parseInt(args[2]);
		int max = Integer.parseInt(args[3]);
		int num_doms = Integer.parseInt(args[4]);
		int num_refs = Integer.parseInt(args[5]);

		if (top_dir.exists()) {
			file_funcs.delete_dir(top_dir);
			System.out.println("Deleted \n\t" + top_dir);
		}
		top_dir.mkdirs();

		System.out.println("Created \n\t" + top_dir);

		paccount pcc1 = new paccount();
		pcc1.set_base_dir(top_dir, owr, net_funcs.TCP_NET, null);
		pcc1.set_working_currency(config.DEFAULT_CURRENCY);

		create_all_refs(pcc1, min, max, num_doms, num_refs);
	}

	public static void create_all_refs(paccount pcc1, int min, int max,
			int num_doms, int num_refs) {
		if (min < tag_denomination.MIN_EXPO) {
			min = tag_denomination.MIN_EXPO;
		}
		if (max > tag_denomination.MAX_EXPO) {
			max = tag_denomination.MAX_EXPO;
		}
		if (min > max) {
			min = max;
		}
		if (num_doms > 100) {
			num_doms = 100;
		}
		if (num_doms < 1) {
			num_doms = 1;
		}
		if (num_refs > 100) {
			num_refs = 100;
		}
		if (num_refs < 1) {
			num_refs = 1;
		}

		tag_denomination deno = new tag_denomination(pcc1.get_working_currency());

		int tot_id = 1;

		for (int expo = min; expo <= max; expo++) {
			deno.ten_exponent = expo;
			deno.multiplier = 1;
			tot_id = create_deno_refs(pcc1, deno, num_doms, num_refs, tot_id);
			deno.multiplier = 2;
			tot_id = create_deno_refs(pcc1, deno, num_doms, num_refs, tot_id);
			deno.multiplier = 5;
			tot_id = create_deno_refs(pcc1, deno, num_doms, num_refs, tot_id);
		}
	}

	public static int create_deno_refs(paccount pcc1,
			tag_denomination deno, int num_doms, int num_refs, int tot_id) {
		throw new bad_emetcode(2); // FIX WHEN TESTING.

		/*
		for (int dom = 0; dom < num_doms; dom++) {
			// String dom_pth = pro1.get_dom_path(deno, "dom_" + (dom + 1));
			for (int ref = 0; ref < num_refs; ref++) {
				String to_sha = "id_sha_" + tot_id;
				List<String> tmp_lst = new ArrayList<String>(1);
				tmp_lst.add(to_sha);
				byte[] sha_bts = parse.calc_sha_lines(tmp_lst);
				String id_sha_str = convert.bytes_to_hex_string(sha_bts);

				File sel_ff = pro1.get_paref_file(dom_pth, id_sha_str);
				pro1.save_paref_file(sel_ff);
				if (!sel_ff.exists()) {
					logger.info("CANNOT save file " + sel_ff);
				}
				tot_id++;
			}
		}
		return tot_id;
		 */
	}

}
