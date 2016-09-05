package emetcode.crypto.bitshake.utils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import emetcode.crypto.bitshake.bitshaker;
import emetcode.crypto.bitshake.bitsigner;
import emetcode.crypto.bitshake.signeer;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.bitshake.locale.L;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.logger;

public class transfer_crypto {

	// static

	static final boolean IN_DEBUG_1 = false;
	static final boolean IN_DEBUG_2 = true; // save_pvks & load_pvks

	private static final int[] DIR_SLICES_1 = { 2, 2, 3, 4 };

	private static final String PVKS_FNAM = "pvks.dat";
	private static final String SPENT_PVK_FNAM = "spent_pvk.dat";
	
	private static final String CRYPTOBOX_SUF = ".cryptobox";

	private static final String PUBLISHED_PVK_ID = "pub_pvk";
	private static final String PRIVATE_ID = "private";

	public static int SIGNATURE_NUM_BITS = 32;
	public static int PRIV_KEY_NUM_BYTES = 512;

	// attr

	private int num_signa_bits;
	private int num_prv_key_bytes;

	private byte[] sgn_pvk;
	private byte[] enc_pvk;
	private byte[] fill_data;

	public byte[] tgt_vrf_dat;
	private byte[] prv_vrf_dat;
	private byte[] sgn_puk;

	private String enc_puk_str;

	private String puk_file_id;
	
	private long creat_tm;

	// init meth

	public transfer_crypto(long tm) {
		init_transfer_crypto(tm);
	}

	private void init_transfer_crypto(long tm) {
		num_signa_bits = SIGNATURE_NUM_BITS;
		num_prv_key_bytes = PRIV_KEY_NUM_BYTES;

		sgn_pvk = null;
		enc_pvk = null;
		fill_data = null;

		tgt_vrf_dat = null;
		prv_vrf_dat = null;
		sgn_puk = null;

		enc_puk_str = null;

		puk_file_id = null;
		
		creat_tm = tm;
	}

	boolean is_virgin() {
		boolean c1 = (sgn_pvk == null);
		boolean c2 = (enc_pvk == null);
		boolean c3 = (fill_data == null);
		boolean c4 = (tgt_vrf_dat == null);
		boolean c5 = (prv_vrf_dat == null);
		boolean c6 = (sgn_puk == null);
		boolean vv = (c1 && c2 && c3 && c4 && c5 && c6);
		return vv;
	}
	
	public void set_cryptobox(String val){
		if(val == null){
			throw new bad_emetcode(2);
		}
		enc_puk_str = val;
	}

	public String get_cryptobox(){
		if(enc_puk_str == null){
			throw new bad_emetcode(2);
		}
		return enc_puk_str;
	}
	
	public boolean has_cryptobox(){
		return (enc_puk_str != null);
	}
	
	public void start_transfer_crypto(key_owner owr) {
		byte[] pr_dt = new byte[1];
		pr_dt[0] = 0;
		start_transfer_crypto(owr, pr_dt);
	}

	public void start_transfer_crypto(key_owner owr, byte[] pr_dt) {
		if (pr_dt == null) {
			throw new bad_emetcode(2);
		}
		prv_vrf_dat = pr_dt;

		sgn_pvk = owr.new_random_key(num_prv_key_bytes);
		enc_pvk = owr.new_random_key(num_prv_key_bytes);
		fill_data = owr.new_random_key(num_prv_key_bytes);

		signeer sgn = new signeer(null, sgn_pvk, num_signa_bits);

		sgn_puk = sgn.get_signer_data();

		enc_puk_str = encrypt_sgn_puk_to_str();

		puk_file_id = null;
		
		if (enc_puk_str == null) {
			throw new bad_emetcode(2);
		}
	}

	// rivate meth

	private static byte[] as_pvk_bytes(String pvk_str) {
		byte[] pvk = convert.hex_frm_string_to_bytes(pvk_str, PUBLISHED_PVK_ID);
		return pvk;
	}

	private static String as_pvk_string(byte[] pvk) {
		String str1 = convert.bytes_to_hex_frm_string(pvk, PUBLISHED_PVK_ID);
		return str1;
	}

	private static String as_signa_string(byte[] signa) {
		String str1 = convert.bytes_to_hex_frm_string(signa,
				config.SIGNATURE_ID);
		return str1;
	}

	private static byte[] as_signa_bytes(String signa) {
		byte[] sgn_bts = convert.hex_frm_string_to_bytes(signa,
				config.SIGNATURE_ID);
		return sgn_bts;
	}

	private String encrypt_pvks_to_str(key_owner owr) {
		byte[] enc1 = encrypt_pvks(owr);
		String str1 = convert.bytes_to_hex_frm_string(enc1, PRIVATE_ID);
		return str1;
	}

	private byte[] encrypt_pvks(key_owner owr) {
		byte[] per_pvk = owr.get_copy_of_secret_key();
		byte[] data_bytes = pack_pvks();

		byte[] pvks_sha = convert.calc_sha_bytes(data_bytes);
		byte[] enc_pv_data = bitshaker.encrypt_bytes(data_bytes, per_pvk);

		byte[] pk_enc = data_packer.pack_enc(enc_pv_data, pvks_sha);
		return pk_enc;
	}

	private void decrypt_pvks_str(String enc_pv_str, key_owner owr) {
		byte[] dec1 = convert.hex_frm_string_to_bytes(enc_pv_str, PRIVATE_ID);
		decrypt_pvks(dec1, owr);
	}

	private void decrypt_pvks(byte[] pk_enc, key_owner owr) {
		if (pk_enc == null) {
			throw new bad_emetcode(4);
		}

		List<byte[]> lst1 = data_packer.unpack_enc(pk_enc);
		if (lst1.size() != 2) {
			throw new bad_emetcode(2);
		}

		byte[] enc_pv_data = lst1.get(0);
		byte[] pvks_sha = lst1.get(1);

		byte[] per_pvk = owr.get_copy_of_secret_key();
		byte[] data_bytes = bitshaker.decrypt_bytes(enc_pv_data, per_pvk);

		byte[] calc_pvks_sha = convert.calc_sha_bytes(data_bytes);
		if (!Arrays.equals(calc_pvks_sha, pvks_sha)) {
			throw new bad_emetcode(2, L.invalid_sha_while_decryptig_transfer);
		}

		unpack_pvks(data_bytes);
	}

	private String encrypt_sgn_puk_to_str() {
		byte[] enc2 = encrypt_sgn_puk();
		String str2 = convert.bytes_to_hex_frm_string(enc2, config.SIGNER_ID);
		return str2;
	}

	private byte[] encrypt_sgn_puk() {
		if (enc_pvk == null) {
			throw new bad_emetcode(2);
		}
		if (prv_vrf_dat == null) {
			throw new bad_emetcode(2);
		}
		if (sgn_puk == null) {
			throw new bad_emetcode(2);
		}

		byte[] data_bytes = pack_puk();

		byte[] puk_sha = convert.calc_sha_bytes(data_bytes);
		byte[] enc_pu_data = bitshaker.encrypt_bytes(data_bytes, enc_pvk);

		byte[] pk_enc = data_packer.pack_enc(enc_pu_data, puk_sha);
		return pk_enc;
	}

	private void decrypt_sgn_puk_str(String enc_pu_str) {
		byte[] dec2 = convert.hex_frm_string_to_bytes(enc_pu_str,
				config.SIGNER_ID);
		decrypt_sgn_puk(dec2);
	}

	private void decrypt_sgn_puk(byte[] pk_enc) {
		if (pk_enc == null) {
			throw new bad_emetcode(2);
		}
		if (enc_pvk == null) {
			throw new bad_emetcode(2);
		}
		if (prv_vrf_dat != null) {
			throw new bad_emetcode(2);
		}
		if (sgn_puk != null) {
			throw new bad_emetcode(2);
		}

		List<byte[]> lst1 = data_packer.unpack_enc(pk_enc);
		if (lst1.size() != 2) {
			throw new bad_emetcode(2);
		}

		byte[] enc_pu_data = lst1.get(0);
		byte[] puk_sha = lst1.get(1);

		byte[] data_bytes = bitshaker.decrypt_bytes(enc_pu_data, enc_pvk);

		byte[] calc_puk_sha = convert.calc_sha_bytes(data_bytes);
		if (!Arrays.equals(calc_puk_sha, puk_sha)) {
			throw new bad_emetcode(2, L.invalid_sha_while_decryptig_transfer);
		}

		unpack_puk(data_bytes);

		if (!file_funcs.all_zero(prv_vrf_dat)) {
			if (tgt_vrf_dat == null) {
				throw new bad_emetcode(2);
			}
			if (!Arrays.equals(tgt_vrf_dat, prv_vrf_dat)) {
				throw new bad_emetcode(2);
			}
		}
	}

	private boolean check_transfer_signa(byte[] target_data, byte[] signa) {
		if (sgn_puk == null) {
			throw new bad_emetcode(2);
		}
		boolean ck_val = bitsigner.check_signature(target_data, sgn_puk, signa);
		return ck_val;
	}

	private byte[] sign_transfer(byte[] target_data) {
		if (target_data == null) {
			throw new bad_emetcode(2);
		}
		if (sgn_pvk == null) {
			throw new bad_emetcode(2);
		}

		signeer sgn1 = new signeer(target_data, sgn_pvk, num_signa_bits);
		// signeer sgn2 = new signeer(sgn_puk);

		byte[] signa = sgn1.get_signature(sgn1);
		if (signa == null) {
			throw new bad_emetcode(2);
		}

		return signa;
	}

	private byte[] pack_pvks() {
		int num_bts_ints = 5 * 4;
		int full_sz = num_bts_ints + sgn_pvk.length + enc_pvk.length
				+ fill_data.length;

		byte[] data_bytes = new byte[full_sz];
		ByteBuffer buff = ByteBuffer.wrap(data_bytes);

		buff.putInt(num_prv_key_bytes);
		buff.putInt(num_signa_bits);
		buff.putInt(sgn_pvk.length);
		buff.putInt(fill_data.length);
		buff.putInt(enc_pvk.length);

		buff.put(sgn_pvk);
		buff.put(fill_data);
		buff.put(enc_pvk);

		if (buff.remaining() != 0) {
			throw new bad_emetcode(2);
		}
		return data_bytes;
	}

	private void unpack_pvks(byte[] data_bytes) {
		if (data_bytes == null) {
			throw new bad_emetcode(2);
		}
		ByteBuffer buff = ByteBuffer.wrap(data_bytes);

		int num_prv_key_bytes = buff.getInt();
		int num_signa_bits = buff.getInt();
		int sgn_pvk_sz = buff.getInt();
		int fill_data_sz = buff.getInt();
		int enc_pvk_sz = buff.getInt();

		if (num_prv_key_bytes < 0) {
			throw new bad_emetcode(2);
		}
		if (num_signa_bits < 0) {
			throw new bad_emetcode(2);
		}
		if (sgn_pvk_sz < 0) {
			throw new bad_emetcode(2);
		}
		if (fill_data_sz < 0) {
			throw new bad_emetcode(2);
		}
		if (enc_pvk_sz < 0) {
			throw new bad_emetcode(2);
		}

		int dat_sz = data_bytes.length;

		if (sgn_pvk_sz > dat_sz) {
			throw new bad_emetcode(2);
		}
		if (fill_data_sz > dat_sz) {
			throw new bad_emetcode(2);
		}
		if (enc_pvk_sz > dat_sz) {
			throw new bad_emetcode(2);
		}

		int tot = sgn_pvk_sz + fill_data_sz + enc_pvk_sz;
		if (tot != buff.remaining()) {
			throw new bad_emetcode(2);
		}

		sgn_pvk = new byte[sgn_pvk_sz];
		fill_data = new byte[fill_data_sz];
		enc_pvk = new byte[enc_pvk_sz];

		buff.get(sgn_pvk);
		buff.get(fill_data);
		buff.get(enc_pvk);

		if (buff.remaining() != 0) {
			throw new bad_emetcode(2);
		}
	}

	private byte[] pack_puk() {
		int num_bts_ints = 3 * 4;
		int full_sz = num_bts_ints + prv_vrf_dat.length + sgn_puk.length;

		byte[] data_bytes = new byte[full_sz];
		ByteBuffer buff = ByteBuffer.wrap(data_bytes);

		buff.putInt(num_signa_bits);
		buff.putInt(prv_vrf_dat.length);
		buff.putInt(sgn_puk.length);

		buff.put(prv_vrf_dat);
		buff.put(sgn_puk);

		if (buff.remaining() != 0) {
			throw new bad_emetcode(2);
		}
		return data_bytes;
	}

	private void unpack_puk(byte[] data_bytes) {
		if (data_bytes == null) {
			throw new bad_emetcode(2);
		}
		ByteBuffer buff = ByteBuffer.wrap(data_bytes);

		int num_signa_bits = buff.getInt();
		int pre_dat_sz = buff.getInt();
		int sgn_puk_sz = buff.getInt();

		if (num_signa_bits < 0) {
			throw new bad_emetcode(2);
		}
		if (pre_dat_sz < 0) {
			throw new bad_emetcode(2);
		}
		if (sgn_puk_sz < 0) {
			throw new bad_emetcode(2);
		}

		int dat_sz = data_bytes.length;
		int tot_sz = pre_dat_sz + sgn_puk_sz;

		if (tot_sz > dat_sz) {
			throw new bad_emetcode(2);
		}
		if (tot_sz != buff.remaining()) {
			throw new bad_emetcode(2);
		}

		prv_vrf_dat = new byte[pre_dat_sz];
		sgn_puk = new byte[sgn_puk_sz];

		buff.get(prv_vrf_dat);
		buff.get(sgn_puk);

		if (buff.remaining() != 0) {
			throw new bad_emetcode(2);
		}
	}
	
	private void delete_pvks(File dir_pvks){
		if (enc_puk_str == null) {
			throw new bad_emetcode(2);
		}
		File ff = get_pvks_file(dir_pvks);
		file_funcs.path_delete(dir_pvks, ff);
		//file_funcs.concurrent_delete_file(ff);
	}

	private String get_puk_file_id(){
		if (enc_puk_str == null) {
			throw new bad_emetcode(2);
		}
		if(puk_file_id != null){
			return puk_file_id;
		}

		byte[] puk_bts = enc_puk_str.getBytes(config.UTF_8);
		byte[] puk_sha = convert.calc_sha_bytes(puk_bts);
		long kk_nm = convert.calc_minisha_long(puk_sha);
		String nm = "" + creat_tm + "_" + kk_nm;
		
		puk_file_id = nm;
		return puk_file_id;
	}

	private String get_puk_id_dir_name(){
		String dir1 = file_funcs.as_sliced_dir_path(get_puk_file_id(), DIR_SLICES_1);
		return dir1;
	}
	
	private File get_puk_id_dir(File base_dd){
		File dir1 = new File(base_dd, get_puk_id_dir_name());
		return dir1;
	}
	
	private File get_pvks_file(File base_dd){
		File dd = get_puk_id_dir(base_dd);
		File pvk_ff = new File(dd, PVKS_FNAM);
		return pvk_ff;
	}

	private String get_cryptobox_file_name(){
		String nm = get_puk_file_id() + CRYPTOBOX_SUF;
		return nm;
	}
	
	// public meth
	
	public boolean has_cryptobox_key(){
		return (enc_pvk != null);
	}
	
	public String get_cryptobox_key(){
		if(enc_pvk == null){
			throw new bad_emetcode(2);
		}
		return as_pvk_string(enc_pvk);
	}

	public void decrypt_cryptobox(String puk_enckey_str) {
		if (enc_puk_str == null) {
			throw new bad_emetcode(2);
		}
		if (puk_enckey_str == null) {
			throw new bad_emetcode(2);
		}

		enc_pvk = as_pvk_bytes(puk_enckey_str);
		decrypt_sgn_puk_str(enc_puk_str);
	}

	public boolean check_passet_signature(byte[] target_data, String signa) {
		byte[] sgn_bts = as_signa_bytes(signa);
		boolean sgn_ok = check_transfer_signa(target_data, sgn_bts);
		return sgn_ok;
	}

	public String get_passet_signature(byte[] target_data) {
		byte[] sgna = sign_transfer(target_data);
		String sgn_str = as_signa_string(sgna);
		return sgn_str;
	}

	public boolean is_puk_spent(File dir_spent) {
		File puk_dir = get_puk_id_dir(dir_spent);
		if (!puk_dir.exists()) {
			return false;
		}
		if (!puk_dir.isDirectory()) {
			throw new bad_emetcode(2);
		}

		File ff2 = new File(puk_dir, SPENT_PVK_FNAM);

		if (!ff2.exists()) {
			throw new bad_emetcode(2);
		}

		byte[] read_pvk = mem_file.concurrent_read_encrypted_bytes(ff2, null);
		if (read_pvk == null) {
			throw new bad_emetcode(2);
		}

		if (!Arrays.equals(enc_pvk, read_pvk)) {
			throw new bad_emetcode(2);
		}

		return true;
	}
	
	public void spend_puk(File dir_spent, File dir_pvks) {
		File dir_sha = file_funcs.get_dir(get_puk_id_dir(dir_spent));

		File ff2 = new File(dir_sha, SPENT_PVK_FNAM);

		mem_file.concurrent_write_encrypted_bytes(ff2, null, enc_pvk);
		if (!ff2.exists()) {
			throw new bad_emetcode(2, String.format(L.cannot_write_spend_file,
					ff2));
		}
		
		delete_pvks(dir_pvks);
	}

	public static String get_mikid_str(String puk) {
		if (puk == null) {
			throw new bad_emetcode(2);
		}
		byte[] orig = puk.getBytes();
		byte[] arr1 = Arrays.copyOf(orig, orig.length);
		key_owner tmp_owr = new key_owner(arr1);
		String id_str = tmp_owr.get_mikid();
		return id_str;
	}

	public void save_pvks_in_dir(File base_dd, key_owner owr) {
		if (enc_puk_str == null) {
			throw new bad_emetcode(2);
		}
		
		File f_nam = get_pvks_file(base_dd);
		String pvks_str = encrypt_pvks_to_str(owr);
		if (pvks_str == null) {			
			throw new bad_emetcode(2, String.format(L.cannot_save_null_transfer_pvks, f_nam));
		}
		
		file_funcs.mk_parent_dir(f_nam);
		
		boolean w_ok = mem_file.write_string(f_nam, pvks_str);
		if (!w_ok) {
			throw new bad_emetcode(2);
		}
		if(IN_DEBUG_2){
			logger.debug("SAVED_PVKS_FILE=" + f_nam);
		}
	}
	
	public void load_pvks_from_dir(File base_dd, key_owner owr) {
		if (enc_puk_str == null) {
			throw new bad_emetcode(2);
		}
		
		File f_nam = get_pvks_file(base_dd);
		String pvks_str = mem_file.read_string(f_nam);
		if (pvks_str == null) {
			throw new bad_emetcode(2, String.format(L.cannot_read_file, f_nam));
		}
		decrypt_pvks_str(pvks_str, owr);
		decrypt_sgn_puk_str(enc_puk_str);
		if(IN_DEBUG_2){
			logger.debug("LOADED_PVKS_FILE=" + f_nam);
		}
	}
	
	public File get_cryptobox_file(File base_dd){
		File ff = new File(base_dd, get_cryptobox_file_name());
		return ff;
	}
	
	public long get_creation_time(){
		return creat_tm;
	}
}
