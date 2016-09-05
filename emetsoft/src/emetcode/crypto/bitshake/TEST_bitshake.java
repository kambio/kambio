package emetcode.crypto.bitshake;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import emetcode.crypto.bitshake.utils.config;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.data_packer;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.bitshake.utils.mer_twist;
import emetcode.util.devel.logger;
import emetcode.util.devel.thread_funcs;

public class TEST_bitshake {

	public static void TEST_get_op(String[] args) {
		byte[] key = "UNA CADENA CULAQUIERA".getBytes(config.UTF_8);
		mer_twist gen = new mer_twist(key);
		int num_ops = 20;
		swap_opers all_ops = new swap_opers(gen, num_ops, key);

		int max_ops = 501;

		for (int aa = 0; aa < max_ops; aa++) {
			long op = all_ops.get_mod_oper(aa, max_ops);
			assert (all_ops.ck_op(aa, max_ops, op));
			System.out.println("aa=" + aa + " op=" + op);

			// long idx0 = (aa / 10) * 10;
			// System.out.println("aa=" + aa + " idx0=" + idx0);
		}

		/*
		 * System.out.println("______________________________");
		 * 
		 * long op2 = 0; long aa2 = 0;
		 * 
		 * aa2 = 473; op2 = all_ops.get_mod_oper(aa2, max_ops);
		 * System.out.println("aa=" + aa2 + " op=" + op2);
		 * 
		 * aa2 = 493; op2 = all_ops.get_mod_oper(aa2, max_ops);
		 * System.out.println("aa=" + aa2 + " op=" + op2);
		 * 
		 * aa2 = 474; op2 = all_ops.get_mod_oper(aa2, max_ops);
		 * System.out.println("aa=" + aa2 + " op=" + op2);
		 * 
		 * aa2 = 494; op2 = all_ops.get_mod_oper(aa2, max_ops);
		 * System.out.println("aa=" + aa2 + " op=" + op2);
		 */
	}

	public static void TEST_concurr(String[] args) {
		if (args.length < 2) {
			System.out.println("args: (-r|-w) <file_nm> [<val>]");
			return;
		}
		String comm = args[0];
		String f_nm = args[1];

		mem_file.debug_msg("ENTER para empezar", true);

		if (comm.equals("-r")) {
			byte[] r_dat = mem_file.concurrent_read_encrypted_bytes(new File(
					f_nm), null);
			if (r_dat != null) {
				String dat_str = new String(r_dat, config.UTF_8);
				System.out.println("READ_STRING= '" + dat_str + "'");
			} else {
				System.out.println("COULD NOT READ FILE");
			}
			System.out.println("FINISHED_READING");
		} else {
			if (args.length < 3) {
				System.out.println("args: (-r|-w) <file_nm> [<val>]");
				return;
			}
			String val = args[2] + "\n";
			byte[] w_data = val.getBytes(config.UTF_8);
			mem_file.concurrent_write_encrypted_bytes(new File(f_nm), null,
					w_data);
			System.out.println("FINISHED_WRITING");
		}
	}

	public static void TEST_format(String[] args) {
		// File ff = new File("./archivo");
		// String s0 = "otro param";
		// String ss = String.format("Esta es otra prueba", ff, s0);
		String s2 = null;
		String ss = String.format("NULL %s prueba %s", s2);
		System.out.println(ss);
	}

	public static void test_rel_path(String[] args) {
		if (args.length < 2) {
			System.out.println("args: <base_dir> <file_nm>");
			return;
		}
		File b_dir = new File(args[0]);
		File ff = new File(args[1]);
		File rel_ff = file_funcs.get_rel_file(b_dir, ff);
		System.out.println("b_dir=" + b_dir);
		System.out.println("ff=" + ff);
		System.out.println("rel_ff=" + rel_ff);
	}

	public static byte[] get_rand_dat(mer_twist gg, int data_sz) {
		byte[] dat = new byte[data_sz];
		for (int ii = 0; ii < data_sz; ii++) {
			dat[ii] = (byte) (convert.to_interval(gg.nextInt(), Byte.MIN_VALUE,
					Byte.MAX_VALUE));
		}
		return dat;
	}

	public static byte[] get_rand_dat(mer_twist gg) {
		int min_sz = 1;
		// int max_sz = 18 * 1000000;
		int max_sz = 30;

		int data_sz = (int) (convert.to_interval(gg.nextInt(), min_sz, max_sz));
		return get_rand_dat(gg, data_sz);
	}

	static byte[] get_rand_key(mer_twist gg) {
		int min_kk_sz = 10;
		int max_kk_sz = 100;

		int kk_zz = (int) (convert.to_interval(gg.nextInt(), min_kk_sz,
				max_kk_sz));
		byte[] kk = get_rand_dat(gg, kk_zz);
		return kk;
	}

	static cryper get_rand_cryper(mer_twist gg) {
		int min_kk_sz = 10;
		int max_kk_sz = 1000;

		int kk_zz = (int) (convert.to_interval(gg.nextInt(), min_kk_sz,
				max_kk_sz));
		byte[] kk = get_rand_dat(gg, kk_zz);
		cryper cc = new cryper(kk);
		// System.out.println("KK=" + kk.length);
		return cc;
	}

	private static void enc_dec(mer_twist gg, byte[] dat, cryper cc) {
		int rr_cc1 = (int) (convert.to_interval(gg.nextInt(), 0, 2));
		if (rr_cc1 > 0) {
			// System.out.println("RR");
			cc.reset_cryper();
		}
		int min_reps = 1;
		int max_reps = 3;

		int num_rep = (int) (convert.to_interval(gg.nextInt(), min_reps,
				max_reps));
		// System.out.println("reps=" + num_rep);
		for (int aa = 0; aa < num_rep; aa++) {
			byte[] sha_bef = convert.calc_sha_bytes(dat);
			byte[] enc_dat = bitshaker.encrypt_bytes(dat, cc);
			//byte[] enc_dat = bitshaker.encrypt_bytes_with_sha(dat, cc);

			int rr_cc2 = (int) (convert.to_interval(gg.nextInt(), 0, 2));
			if (rr_cc2 > 0) {
				// System.out.println("rr");
				cc.reset_cryper();
			}

			//byte[] dat2 = bitshaker.decrypt_bytes_with_sha(enc_dat, cc);
			byte[] dat2 = bitshaker.decrypt_bytes(enc_dat, cc);
			byte[] sha_aft = convert.calc_sha_bytes(dat2);

			if (!Arrays.equals(dat, dat2)) {
				throw new bad_bitshake(2, "DATA DIFFER");
			}
			if (!Arrays.equals(sha_bef, sha_aft)) {
				throw new bad_bitshake(2, "SHA DIFFER");
			}
		}
	}

	static void test_shake2(String[] args) {
		byte[] key = "UNA CADENA CULAQUIERA".getBytes(config.UTF_8);
		if (args.length > 0) {
			key = args[0].getBytes(config.UTF_8);
		}
		mer_twist gg = new mer_twist(key);

		System.out.println("test_shake2");

		cryper cc = get_rand_cryper(gg);
		byte[] dat = get_rand_dat(gg);

		long num_rep = 0;
		while (true) {
			if ((num_rep % 10) == 0) {
				System.out.print('+');
			}
			enc_dec(gg, dat, cc);
			int rr_cc1 = (int) (convert.to_interval(gg.nextInt(), 0, 2));
			if (rr_cc1 > 0) {
				cc = get_rand_cryper(gg);
			}
			int rr_cc2 = (int) (convert.to_interval(gg.nextInt(), 0, 2));
			if (rr_cc2 > 0) {
				dat = get_rand_dat(gg);
				//System.out.println("\n" + dat.length + "\n");
				// System.out.println("DD=" + convert.bytes_to_hex_string(dat));
			}
			num_rep++;
		}
	}

	static void pack_unpack(mer_twist gg) {
		List<byte[]> all_dat_1 = new ArrayList<byte[]>();

		int min_lst_sz = 1;
		int max_lst_sz = 20;

		int lst_sz = (int) (convert.to_interval(gg.nextInt(), min_lst_sz,
				max_lst_sz));
		for (int aa = 0; aa < lst_sz; aa++) {
			byte[] dat = get_rand_dat(gg);
			all_dat_1.add(dat);
		}

		byte[] pk = data_packer.pack_list(all_dat_1);

		List<byte[]> all_dat_2 = data_packer.unpack_list(pk);

		if (all_dat_1.size() != all_dat_2.size()) {
			throw new bad_bitshake(2);
		}
		for (int ii = 0; ii < all_dat_1.size(); ii++) {
			byte[] dd1 = all_dat_1.get(ii);
			byte[] dd2 = all_dat_2.get(ii);
			if (!Arrays.equals(dd1, dd2)) {
				throw new bad_bitshake(2);
			}
		}
	}

	public static void test_packer(String[] args) {
		byte[] key = "UNA CADENA CULAQUIERA".getBytes(config.UTF_8);
		if (args.length > 0) {
			key = args[0].getBytes(config.UTF_8);
		}
		mer_twist gg = new mer_twist(key);

		System.out.println("test_packer");

		long num_rep = 0;
		while (true) {
			if ((num_rep % 10) == 0) {
				System.out.print('x');
			}
			pack_unpack(gg);
			num_rep++;
		}
	}

	public static void shake_long_val(mer_twist gg) {
		long vv = gg.nextLong();
		long kk = gg.nextLong();
		long ee1 = bitshaker.encrypt_long(vv, kk);
		long vv2 = bitshaker.decrypt_long(ee1, kk);
		if (vv2 != vv) {
			throw new bad_bitshake(2);
		}
	}

	public static void test_hex_conv(mer_twist gg) {
		byte[] dat1 = get_rand_dat(gg);
		byte[] hex_dat = convert.bytes_to_hex_bytes(dat1);
		byte[] dat2 = convert.hex_bytes_to_bytes(hex_dat);
		if (!Arrays.equals(dat1, dat2)) {
			throw new bad_bitshake(2);
		}
	}

	public static void test_read_write(mer_twist gg) {
		byte[] dat1 = get_rand_dat(gg);
		File ff = new File("./test_read_write");

		mem_file.concurrent_write_encrypted_bytes(ff, null, dat1);
		byte[] dat2 = mem_file.concurrent_read_encrypted_bytes(ff, null);

		if (dat1 == dat2) {
			throw new bad_bitshake(2);
		}
		if (!Arrays.equals(dat1, dat2)) {
			throw new bad_bitshake(2);
		}
	}

	static void test_shake(String[] args) {
		byte[] key = "UNA CADENA CULAQUIERA".getBytes(config.UTF_8);
		if (args.length > 0) {
			key = args[0].getBytes(config.UTF_8);
		}
		mer_twist gg = new mer_twist(key);

		System.out.println("test_shake");

		long num_rep = 0;
		while (true) {
			if ((num_rep % 10) == 0) {
				System.out.print('w');
			}
			// shake_long_val(gg);
			// test_hex_conv(gg);
			test_file_encry_dec(gg);
			// test_read_write(gg);
			num_rep++;
		}
	}

	public static void main(String[] args) {
		// TEST_locks(args);
		// TEST_concurr(args);
		// TEST_get_op(args);
		// TEST_format(args);
		// test_rel_path(args);
		// test_shake(args);
		//test_shake2(args);
		// test_packer(args);
		// TEST_multi_lock(args);
		test_trust_file(args);
	}

	public static void test_file_encry_dec(mer_twist gg) {
		byte[] the_key = get_rand_key(gg);
		boolean bb_sha = ((int) (convert.to_interval(gg.nextInt(), 0, 2)) > 0);

		byte[] dat0 = get_rand_dat(gg);
		byte[] dat1 = Arrays.copyOf(dat0, dat0.length);

		// byte[] enc_dat = bitshaker.encrypt_bytes_with_sha(dat1, the_key);
		// byte[] dat2 = bitshaker.decrypt_bytes_with_sha(enc_dat, the_key);
		byte[] enc_dat = bitshaker.encrypt_file_bytes(dat1, the_key, bb_sha);
		byte[] dat2 = bitshaker.decrypt_file_bytes(enc_dat, the_key, bb_sha);
		// byte[] hex_dat = convert.bytes_to_hex_bytes(dat1);
		// byte[] dat2 = convert.hex_bytes_to_bytes(hex_dat);
		// byte[] enc_dat = bitshaker.process_bitshake_bytes(dat1, null,
		// the_key, true, bb_sha);
		// byte[] dat2 = bitshaker.process_bitshake_bytes(enc_dat, null,
		// the_key, false, bb_sha);

		if (dat0 == dat2) {
			throw new bad_bitshake(2);
		}
		if (!Arrays.equals(dat0, dat2)) {
			throw new bad_bitshake(2, "DATA FILES DIFFER");
		}
		// System.out.print('.');
	}

	private static Runnable get_reader_runner(final boolean is_w,
			final long ww_tm, final File ff, final key_owner o_owr, final int aa) {

		//final key_owner owr = null;

		Runnable rr1 = new Runnable() {
			public void run() {
				logger.info("Started " + Thread.currentThread().getName());
				long num_rr = 0;
				while (true) {
					num_rr++;
					if(is_w){
						String vv = "BY_" + aa;
						mem_file.write_encrypted_string(ff, null, vv);
						logger.info("WRIT=" + vv);
					} else {
						String ss = mem_file.read_encrypted_string(ff, null);
						logger.info("READ=" + ss);
					}
					Thread.yield();
					if ((num_rr % 1000) == 0) {
						System.out.print(aa);
					}
					try {
						Thread.sleep(ww_tm);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		return rr1;
	}

	public static void TEST_multi_lock(String[] args) {

		logger.info("Starting TEST_multi_lock");

		mer_twist gg = new mer_twist("GG_FOR_TEST_multi_lock".getBytes());
		File ff = new File("/home/jose/tmp/multi_read.dat");
		key_owner owr = new key_owner("UNA_CLAVE_CUALQUIERA".getBytes());

		mem_file.write_encrypted_string(ff, null, "PRIMERA_CADENA");

		String ss = mem_file.read_encrypted_string(ff, null);
		System.out.println(ss);

		int NUM_THDS = 10;
		for (int aa = 0; aa < NUM_THDS; aa++) {
			boolean is_w = (((int) (convert.to_interval(gg.nextLong(), 0, 2))) > 0);
			long ww_tm = convert.to_interval(gg.nextLong(), 0, 100);
			String nm_thd = Thread.currentThread().getName();
			if(is_w){
				nm_thd += "-writer-" + aa;
			} else {
				nm_thd += "-reader-" + aa;
			}
			Runnable rr_reader = get_reader_runner(is_w, ww_tm, ff, owr, aa);
			// Thread thd_gli =
			thread_funcs.start_thread(nm_thd, rr_reader, false);
		}
	}
	
	public static void test_trust_file(String[] args) {
		if(args.length < 2){
			System.out.println("Faltan args");
			return;
		}
		
		String tag_nm = "trust_me";
		key_owner owr = new key_owner("UNA_CLAVE_CUALQUIERA".getBytes(config.UTF_8));
		
		String op = args[0];
		File ff = new File(args[1]);
		if(op.equals("-create")){
			file_funcs.create_label_file(ff, owr, tag_nm);
			System.out.println("created trusteds file for= '" + ff + "'");
		} else
		if(op.equals("-can")){
			boolean ok = file_funcs.has_label_file(ff, owr, tag_nm);
			if(ok){
				System.out.println("CAN trust file= '" + ff + "'");
			} else {
				System.out.println("canNOT trust file= '" + ff + "'");
			}
		} else {
			System.out.println("invalid op=" + op);
		}		
	}
	
}
