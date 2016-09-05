package emetcode.crypto.bitshake.utils;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import emetcode.crypto.bitshake.bad_bitshake;
import emetcode.crypto.bitshake.bitshaker;
import emetcode.crypto.bitshake.cryper;
import emetcode.crypto.bitshake.generate;

public class key_owner {

	public static final int NUM_BITS_MINI_SHA = 32;

	private static int keid_sz = 0;

	private static void init_all_static() {
		byte[] dummy_key = "DUMMY".getBytes(config.UTF_8);
		String dummy_keid = calc_keid(dummy_key);
		keid_sz = dummy_keid.length();
	}

	static {
		init_all_static();
	};

	private static final ReentrantLock code_lock = new ReentrantLock();

	private mer_twist randomizer;
	private cryper the_cryper;
	private String keid;
	private String mikid;

	public static key_owner get_copy(key_owner owr) {
		byte[] kk2 = owr.the_cryper.get_key_copy();
		return new key_owner(kk2);
	}

	public key_owner(byte[] s_key) {
		init_key_owner(s_key);
	}

	private void init_key_owner(byte[] s_key) {
		if (s_key == null) {
			throw new bad_bitshake(2);
		}

		the_cryper = new cryper(s_key);
		init_keids(s_key);
		init_randomizer(s_key);
	}

	private void init_keids(byte[] kk) {
		byte[] keid_bts = calc_keid_bytes(kk);
		mikid = calc_mini_sha_str(keid_bts);
		keid = convert.bytes_to_hex_string(keid_bts);
	}

	private void init_randomizer(byte[] kk) {
		assert (kk != null);
		byte[] seed = get_new_seed(kk, config.NUM_BYTES_SEED);
		randomizer = new mer_twist(seed);
	}

	public boolean has_secret() {
		boolean c1 = (randomizer != null);
		boolean c2 = (the_cryper != null);
		boolean ok1 = (c1 && c2);
		return ok1;
	}

	public byte[] get_copy_of_secret_key() {
		return the_cryper.get_key_copy();
	}

	public cryper get_cryper() {
		return the_cryper;
	}

	public String get_keid() {
		return keid;
	}

	public String get_mikid() {
		return mikid;
	}

	private static String calc_keid(byte[] key) {
		String sha_str = convert.bytes_to_hex_string(calc_keid_bytes(key));
		return sha_str;
	}

	private static byte[] calc_keid_bytes(byte[] key) {
		assert (key != null);
		byte[] cp_key = Arrays.copyOf(key, key.length);
		byte[] enc_k = bitshaker.encrypt_bytes(cp_key, key);
		byte[] sha_bts = convert.calc_sha_bytes(enc_k);
		return sha_bts;
	}

	public static int get_keid_sz() {
		return keid_sz;
	}

	private static String calc_mini_sha_str(byte[] sha_bts) {
		byte[] sel_bits = convert.calc_mini_sha_arr(sha_bts, NUM_BITS_MINI_SHA);
		String sgn_str = convert.bytes_to_hex_string(sel_bits);
		return sgn_str;
	}
	
	private byte[] generate_seed(byte[] kk, int num_bytes_key) {
		mer_twist rr = new mer_twist(kk);
		byte[] tmp_seed = new byte[num_bytes_key];
		long seed_num_bits = bit_array.num_bits(tmp_seed);
		for(int aa = 0; aa < seed_num_bits; aa++){
			long vv = rr.nextLong();
			long bool_1 = convert.to_interval(vv, 0, 2);
			boolean bb = (bool_1 == 1);
			
			if(bb){
				bit_array.set_bit(tmp_seed, aa);
			} else {
				bit_array.reset_bit(tmp_seed, aa);
			}
		}
		return tmp_seed;
	}
	

	private byte[] get_new_seed(byte[] kk, int num_bytes_key) {
		int ini_seed_sz = config.NUM_BYTES_SEED;
		
		//SecureRandom sc = new SecureRandom();
		//byte[] tmp_seed = sc.generateSeed(ini_seed_sz);
		byte[] tmp_seed = generate_seed(kk, ini_seed_sz);

		tmp_seed = bitshaker.encrypt_bytes(tmp_seed, kk);

		byte[] to_enc = convert.to_byte_array(System.currentTimeMillis());
		byte[] arr_tm = bitshaker.encrypt_bits(to_enc, kk);
		tmp_seed = bitshaker.encrypt_bytes(tmp_seed, arr_tm);

		if (config.has_valid_ip_descrip()) {
			byte[] ip_bts = config.CURRENT_IP_DESCRP.getBytes(config.UTF_8);
			to_enc = Arrays.copyOf(ip_bts, ip_bts.length);
			byte[] arr_ip = bitshaker.encrypt_bytes(to_enc, kk);
			tmp_seed = bitshaker.encrypt_bytes(tmp_seed, arr_ip);
		}
		if (config.has_valid_gps_descrip()) {
			byte[] gps_bts = config.CURRENT_GPS_DESCRP.getBytes(config.UTF_8);
			to_enc = Arrays.copyOf(gps_bts, gps_bts.length);
			byte[] arr_gps = bitshaker.encrypt_bytes(to_enc, kk);
			tmp_seed = bitshaker.encrypt_bytes(tmp_seed, arr_gps);
		}

		assert (tmp_seed.length == num_bytes_key);
		return tmp_seed;
	}

	public byte[] new_random_key(int max_bytes) {
		int num_bytes_type_long = 8;
		int tot_vals = (max_bytes / num_bytes_type_long);
		byte[] nxt_k = new byte[tot_vals * num_bytes_type_long];
		ByteBuffer buff = ByteBuffer.wrap(nxt_k);
		for (int aa = 0; aa < tot_vals; aa++) {
			long val = 0;
			code_lock.lock();
			try {
				val = generate.gen_oper(randomizer, the_cryper.get_key());
			} finally {
				code_lock.unlock();
			}
			buff.putLong(val);
		}
		assert (buff.position() == buff.limit());
		return nxt_k;
	}

	public long new_random_long() {
		long val = 0;
		code_lock.lock();
		try {
			val = generate.gen_oper(randomizer, the_cryper.get_key());
		} finally {
			code_lock.unlock();
		}
		return val;
	}

	public SecureRandom new_SecureRandom() {
		byte[] seed = new_random_key(config.NUM_BYTES_SEED);
		SecureRandom sc = new SecureRandom(seed);
		return sc;
	}

}
