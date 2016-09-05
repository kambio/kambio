

package emetcode.economics.passet;

import java.io.File;
import java.io.FileFilter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mer_twist;
import emetcode.crypto.bitshake.utils.transfer_crypto;
import emetcode.crypto.gamal.gamal_generator;
import emetcode.net.netmix.TEST_netmix;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_std_coref;
import emetcode.util.devel.net_funcs;

public class TEST_passet {

public static void
main(String[] args){
	//logger.info("HOLA");
	
	/*
	try{
		test_issuance(args);
	} catch (bad_passet bd){
		logger.error(bd, bd.msg);
	}*/

	//test_slice_name(args);
	//test_trust_dom(args);
	//test_decimal(args);
	//test_str_funcs(args);
	//test_join_deno(args);
	//test_split_deno(args);
	//test_deno_paths(args);
	//test_file_nam(args);
	//test_list_dir(args);
	//test_copy_dir(args);
	//test_dir_iterator(args);
	//test_path_funcs(args);
	//test_id_sha_str(args);
	//test_str_format(args);
	//test_deno_param(args);
	//test_properties(args);
	//test_maps(args);
	//test_providers(args);
	//test_format(args);
	//test_File_funcs(args);
	//test_utf8(args);
	//test_keys(args);
	//test_bytes_funcs(args);
	//test_transf_descrip(args);
	//test_input_eq(args);
	//test_files_eq(args);
	//test_give(args);
	//test_split_passet(args);
	//test_join_passet(args);
	//test_save_trissuers(args);
	test_chomarks(args);

	System.out.println("END_OF_TESTS.");
}

public static void
test_File_funcs(String[] args){
	File curr = new File("./pro1");
	curr = file_funcs.get_dir(curr);
}


public static void
test_bytes_funcs(String[] args){
	//int max_sz = 10 * config.KB_1;
	int max_sz = config.MB_1;
	Runtime rt = Runtime.getRuntime();
	try{
		//String f_nam = "tmp_ff.dat";

		mer_twist gen1 = new mer_twist("JOSE".getBytes());
		for(int aa = 0; aa < 100; aa++){
			long val1 = gen1.nextLong();
			int f_sz = (int)(convert.to_interval(val1, 0, max_sz));

			System.out.print("." + f_sz);

			byte[] bts = new byte[f_sz];
			for(int bb = 0; bb < bts.length; bb++){
				long val2 = gen1.nextLong();
				bts[bb] = (byte)(convert.to_interval(val2, Byte.MIN_VALUE, Byte.MAX_VALUE));
			}

			//mem_file.write_bytes(f_nam, bts);
			//byte[] bts2 = mem_file.read_bytes(f_nam);

			String str2 = convert.bytes_to_hex_string(bts);
			byte[] bts2 = convert.hex_string_to_bytes(str2);
			boolean ok2 = Arrays.equals(bts, bts2);
			if(! ok2){
				System.out.println("TEST FAILED. NOT EQUAL !!!");
				break;
			}
			
			String str3 = convert.bytes_to_hex_frm_string(bts, "pedro");
			byte[] bts3 = convert.hex_frm_string_to_bytes(str3, "pedro");
			boolean ok3 = Arrays.equals(bts, bts3);
			if(! ok3){
				System.out.println("TEST FAILED. NOT EQUAL !!!");
				break;
			}

			//System.out.println(str3);
		}
	} catch(OutOfMemoryError ex1){
		rt.gc();
		System.out.println("MEM_OUT");
	}
}	

public static void
test_keys(String[] args){
	byte[] key1 = "JOSE_LUIS".getBytes(config.UTF_8);
	tag_person per = new tag_person();

	List<String> issuer_lines = per.get_person_lines(config.no_title);
	byte[] issuer_sha = parse.calc_sha_lines(issuer_lines);

	key_owner owr = new key_owner(key1);

	long tm1 = System.currentTimeMillis();
	transfer_crypto tra1 = new transfer_crypto(tm1);
	tra1.start_transfer_crypto(owr, issuer_sha);

	//String ss1 = tra1.encrypt_pvks_to_str(owr);
	//String ss2 = tra1.encrypt_sgn_puk_to_str();

//	long tm2 = System.currentTimeMillis();
//	transfer_crypto tra2 = new transfer_crypto(tm2);

	System.out.println("FINISHED");
}

public static void
test_utf8(String[] args){
	/*char max_ch = 100;
	if(args.length > 0){
		max_ch = (char)Integer.parseInt(args[0]);
	}*/

	char min_ch = 30000;
	char max_ch = 30200;

	System.out.println("min_ch=" + (int)min_ch);
	System.out.println("max_ch=" + (int)max_ch);

	int num_ch = max_ch - min_ch;
	//char[] ch_arr1 = new char[num_ch];
	//CharBuffer ch_buff = CharBuffer.wrap(ch_arr1);
	StringBuilder str1 = new StringBuilder(num_ch);
	for(char ii = min_ch; ii < max_ch; ii++){
		if(! Character.isISOControl(ii) && Character.isDefined(ii)
			&& ! Character.isWhitespace(ii)
		){
			str1.append(ii);
			//str1.append('a');
			//ch_buff.put(ii);
		}
	}

	String str2 = str1.toString();

	String str3 = "";
	//pr_tools.to_passt_format(str2);

	System.out.println(str3);

	//ByteArrayInputStream is1 = new ByteArrayInputStream(str3.getBytes(config.UTF_8));
	//line_reader lr1 =  new line_reader(is1, 100);
	String str4 = "INVALID";
	//str4 = read_next_passet_line(lr1);

	System.out.println(str2);
	System.out.println("__________________________________________");
	System.out.println(str4);

	if(! str4.equals(str2)){
		System.out.println("NOT_EQUAL !!!");
	}

	//String str3 = 
	/*
	ch_buff.clear();
	ByteBuffer bt_buff = config.UTF_8.encode(ch_buff);
	System.out.println("cap=" + bt_buff.capacity());
	System.out.println("lim=" + bt_buff.limit());
	System.out.println("pos=" + bt_buff.position());
	System.out.println("rem=" + bt_buff.remaining());

	mem_file.write_bytes("ff2", bt_buff.array(), 0, bt_buff.limit());
	*/

	//String[] countryCodes = Locale.getISOCountries();

	//System.out.println(Arrays.toString(countryCodes));
}

public static void
test_signing(String[] args){
	
	try{
		Set<String> all_algo3 = Security.getAlgorithms("SecureRandom");
		//System.out.println(all_algo1);
		//System.out.println(all_algo2);
		System.out.println(all_algo3);

		String str = "This is string to sign";
		byte[] strByte = str.getBytes("UTF-8");

		//String algo1 = "EC";
		//String algo2 = "SHA1PRNG";
		//String algo3 = "SHA1withECDSA";

		String algo1 = "EC";
		//String algo2 = "SHA1PRNG";
		String algo2 = "PKCS11";
		String algo3 = "SHA512withECDSA";

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algo1);
		SecureRandom random = SecureRandom.getInstance(algo2);

		keyGen.initialize(256, random);

		KeyPair pair = keyGen.generateKeyPair();
		PrivateKey priv = pair.getPrivate();
		PublicKey pub = pair.getPublic();

		Signature dsa = Signature.getInstance(algo3);
		dsa.initSign(priv);
		dsa.update(strByte);
		byte[] realSig = dsa.sign();
		System.out.println("Signature: " + new BigInteger(1, realSig).toString(16));

		Signature dsa2 = Signature.getInstance(algo3);
		dsa2.initVerify(pub);
		dsa2.update(strByte);
		boolean verifSig = dsa2.verify(realSig);
		System.out.println("Verification: " + verifSig);

	} catch(Exception ex1){
		System.out.println("NO SIGNATURE");
	}
	
}


public static void
test_maps(String[] args){
	//res.init_country_codes();
	Map<String, String> dir = iso.country_codes;

	for (Map.Entry<String, String> entry : dir.entrySet()){
		System.out.println(entry.getKey() + "/" + entry.getValue());
	}

}

public static void
test_properties(String[] args){
	Properties pps = System.getProperties();
	pps.list(System.out);

	Map<String,String> env = System.getenv();
	for(Map.Entry<String, String> ee : env.entrySet()){
		System.out.println(ee.getKey() + "=" + ee.getValue());
	}
	
	byte[] address = null;
	try {
		address = NetworkInterface.getNetworkInterfaces().nextElement().getHardwareAddress();
	} catch (SocketException e) {
		e.printStackTrace();
	}
	System.out.println("address = " + Arrays.toString(address));	
	// os.arch
	// os.name
	// os.version
	// user.name
	// MACHTYPE
}

public static void
test_deno_param(String[] args){
	if(args.length < 1){
		System.out.println("<deno_param> has format [<mult>][/][z][<expo>]");
		return;
	}
	System.out.println("PARAM=" + args[0]);
	tag_denomination deno = 
			tag_denomination.parse_short_text_denomination(args[0]);
	parse.print_lines(System.out, deno.get_denomination_lines(config.no_title));
}

public static void
test_id_sha_str(String[] args){
	List<String> all_lines = new ArrayList<String>();
	all_lines.add("UNALINEA");
	//byte[] sha_bts = parse.calc_sha_lines(all_lines);
	//String the_sha_str = convert.bytes_to_hex_string(sha_bts);
	//String the_sha_str = tag_transfer.calc_transfer_id(12, sha_bts);
	String the_sha_str = null;
	System.out.println("the_sha_str=" + the_sha_str);
	//System.out.println("SZ=" + the_sha_str.length());

	String f_nam = null;
	//String f_nam = paccount.get_passet_file_name(the_sha_str, false);
	File ff = new File(f_nam);
	//String id_sha = passet.get_passet_id_from(ff);
	
	System.out.println("f_nam=" + f_nam);
	System.out.println("ff=" + ff);
//	System.out.println("id_sha=" + id_sha);
//	
//	if(id_sha.equals(the_sha_str)){
//		System.out.println("SON_IGUALES_TODO_OK");
//	}
	
	//String fm = String.format("%30s %s %s ", "cadena1", "cadena2", "cadena1");
	//System.out.println(fm);
}

/*
public static void
test_path_funcs(String[] args){
	if(args.length < 1){
		System.out.println("Faltan args.");
		return;
	}
	String func = args[0];
	if(func.equals("mov")){
		if(args.length < 4){
			System.out.println("Faltan args. mov pnt_dir to_mov dest_dir.");		
			return;
		}
		File pnt_dir = new File(args[1]);
		File to_mov = new File(args[2]);
		File dest = new File(args[3]);
		file_funcs.path_move(pnt_dir, to_mov, dest);
	}
	else if(func.equals("del")){
		if(args.length < 3){
			System.out.println("Faltan args. del pnt_dir to_del.");		
			return;
		}
		File pnt_dir = new File(args[1]);
		File to_del = new File(args[2]);
		file_funcs.path_delete(pnt_dir, to_del);
	}
	else {
		System.out.println("arg error.");		
		return;
	}
}
*/

public static void
test_dir_iterator(String[] args){
	if(args.length < 1){
		System.out.println("Faltan args.");
		return;
	}
	Iterator<File> it1 = file_funcs.get_pos_iter(new File(args[0]));
	//Iterator<File> it1 = file_funcs.get_pre_order_file_iterator(new File(args[0]));
	while(it1.hasNext()){
		File ff = it1.next();
		System.out.println("" + ff);		
	}
}

public static void
test_copy_dir(String[] args){
	if(args.length < 2){
		System.out.println("move_dir args:\n\t src_dir dst_dir.");
		return;
	}
	//file_funcs.copy_dir(new File(args[0]), new File(args[1]));
	file_funcs.move_dir(new File(args[0]), new File(args[1]));
}

public static void
test_list_dir(String[] args){
	if(args.length < 1){
		System.out.println("list_dir args:\t dir");
		return;
	}
	
	FileFilter filt1 = new FileFilter(){
		public boolean accept(File ff){
			if(ff.isDirectory()){ return false; }
			return ff.getName().startsWith("play");
		}
	};
	
	List<File> all_ff = file_funcs.list_dir(new File(args[0]), null, filt1);
	for(File ff : all_ff){ System.out.println("" + ff); }
}

public static void
test_file_nam(String[] args){
	File dd1 = new File("/alsdkfj");
	String sub = "asdf/qwer/zxcv/asdf.ss";
	File ff = new File(dd1, sub);
	System.out.println("" + ff);
	
	File ff1 = new File(".");
	File ff2 = file_funcs.as_canonical(ff1);
	if(ff1.equals(ff2)){
		System.out.println("EQUALS 1");
	} else {
		System.out.println("NOT equals 1");
	}
	if(ff1.compareTo(ff2) == 0){
		System.out.println("EQUALS 2");
	} else {
		System.out.println("NOT equals 2");
	}
	if(file_funcs.is_same_file(ff1, ff2)){
		System.out.println("EQUALS 3");
	} else {
		System.out.println("NOT equals 3");
	}
	System.out.println("" + ff1);
	System.out.println("" + ff2);
	
}

public static void
test_deno_paths(String[] args){
	if(args.length < 1){
		System.out.println("args:\t dir");
		return;
	}
	File base = file_funcs.as_canonical(new File(args[0]));
	System.out.println("" + base);
	
	List<File> refs = Arrays.asList(base.listFiles()); 
	List<String> to_add = file_funcs.files_to_rel_paths(base.getParentFile(), refs);
	for(String ss: to_add){
		System.out.println(ss);
	}
}

public static void
test_format(String[] args){
	String a_sha = convert.calc_sha_str("JOSE".getBytes());

	int[] secs = {2, 2, 3, 4};
	String as_dir0 = file_funcs.as_sliced_dir_path(a_sha, secs);
	String as_dir = file_funcs.as_sliced_dir_path(a_sha, secs);

	System.out.println(as_dir0);
	System.out.println();
	System.out.println(a_sha);
	System.out.println();
	System.out.println(as_dir);
	System.out.println();
	
	byte[] key1 = "JOSE_LUIS".getBytes(config.UTF_8);
	key_owner owr1 = new key_owner(key1);
	
	paccount pcc = new paccount();
	pcc.set_base_dir(new File("./pro1"), owr1, net_funcs.TCP_NET, null);
	pcc.set_working_currency(config.DEFAULT_CURRENCY);
	//File full_nam = new File(pro.spent_dir, as_dir0);
	
	//System.out.println(full_nam.getPath());

	//File ff1 = new File(full_nam, transfer_crypto.SPENT_DUE_TIME_FNAM);
	//System.out.println(ff1);
	System.out.println();
	
	//full_nam = file_funcs.get_dir(full_nam);

}

public static void
test_split_deno(String[] args){
	if(args.length < 1){
		System.out.println("Bad arguments");
		return;
	}
	tag_denomination deno = 
			tag_denomination.parse_short_text_denomination(args[0]);
	
	int cop_idx = iso.get_currency_idx("COP");
	deno.currency_idx = cop_idx;

	System.out.println("TO_SPLIT= " + deno.get_number_denomination());
	
	//denomination_title.spl
	
	Stack<tag_denomination> s_denos = new Stack<tag_denomination>();
	s_denos.push(deno);
	tag_denomination.rec_split_deno(s_denos, -1);
	
	String deno_title = "deno_lines";
	deno_counter cnt1 = tag_denomination.count_num_have_denos(s_denos, deno.currency_idx);
	List<String> all_ln = cnt1.get_deno_counter_lines(deno_title);
	
	System.out.println("COUNTER1");
	cnt1.print_all_deno_count(System.out);
	System.out.println("____________________________________________");
	
	System.out.println("WRITING COUNTER");
	parse.print_line_list(all_ln);
	System.out.println("____________________________________________");
	
	File f_nm = new File("./counter.txt");
	parse.write_encrypted_lines(f_nm, null, all_ln);
	
	List<String> all_ln2 = parse.read_encrypted_lines(f_nm, null);
	deno_counter cnt2 = new deno_counter();
	cnt2.init_deno_counter_with(all_ln2, deno_title);

	System.out.println("READING COUNTER2");
	parse.print_line_list(all_ln2);
	System.out.println("____________________________________________");

	System.out.println("COUNTER2");
	cnt2.print_all_deno_count(System.out);
	System.out.println("____________________________________________");
	
	for(tag_denomination dd: s_denos){
		System.out.println(">> " + dd.get_number_denomination()); 		
	}
}

public static void
test_join_deno(String[] args){
	if(args.length < 1){
		System.out.println("Bad arguments");
		return;
	}
	
	List<tag_denomination> all_deno = new ArrayList<tag_denomination>();
	for(int aa = 0; aa < args.length; aa++){
		tag_denomination deno = 
				tag_denomination.parse_short_text_denomination(args[aa]);
		all_deno.add(deno);
		System.out.println(">> " + deno.get_number_denomination()); 		
		//System.out.println(">> " + deno.get_short_text_denomination(true)); 		
	}

	System.out.println("JOINED AS=");
	
//	deno_counter in_cter = new deno_counter();
//	in_cter.init_with_denos(all_deno);
//	deno_counter cter = tag_denomination.join_denos(in_cter);
//	cter.print_all_deno_count(System.out);			
}

/*
public static void
test_issuance(String[] args){

	//dbg_check_bytes_funcs(args);

	//String home = System.getProperty("user.home");
	//System.out.println(home);

	if(! config.all_global_ok()){ 
		System.out.println("Initialization failed");
		return; 
	}
	assert(config.JAR_SHA_VALUE != null);
	assert(config.JAR_SIGNATURE != null);

	System.out.println(config.JAR_SHA_VALUE);
	System.out.println(config.JAR_SIGNATURE);

	//person_title per1 = new person_title();
	passet pro1 = new passet();
	
	denomination_title deno = new denomination_title(pro1.working_currency);
	deno.ten_exponent = 4;
	deno.multiplier = 5;
	//pro1.passet_amount = deno;

	//pro1.core_data.issuer.tr_person = per1;

	byte[] key1 = "JOSE_LUIS".getBytes(config.UTF_8);
	key_owner owr1 = new key_owner(key1);

	pro1.set_base_dir(new File("./pro1"), owr1);
	pro1.set_working_currency(config.DEFAULT_CURRENCY);
	//assert(pro1.has_user());
	
	person_title per2 = new person_title();
	passet pro2 = new passet();
	pro2.core_data.issuer.tr_person = per2;
	pro2.set_base_dir(new File("./pro2"), owr1);
	pro2.set_working_currency(config.DEFAULT_CURRENCY);

	//byte[] key2 = "PEDRO_JUAN".getBytes(config.UTF_8);
	//mer_twist gen2 = new mer_twist(key2);
	//key_owner owr2 = new key_owner(gen2, key2);

	pro1.issue_passets(5, owr1, deno, null, true, null);

	file_funcs.copy_all_files(pro1.passet_dir, pro2.passet_dir);

	String aux_coid = owr1.get_mikid();
	pro1.set_connection_id(aux_coid + "conn");

	List<File> all_recep = new ArrayList<File>();
	
	pro1.make_all_receptacles(owr1, null, all_recep, pro1);
	pro1.sign_all_receptacles(owr1);
	pro1.check_all_mine(owr1, null);
	pro1.check_all_mine(owr1, null);
	pro1.sign_ack_all(owr1);

	pro1.make_all_receptacles(owr1, null, all_recep, pro1);
	pro1.sign_all_receptacles(owr1);
	pro1.make_all_receptacles(owr1, null, all_recep, pro1);
	pro1.sign_all_receptacles(owr1);
	pro1.check_all_mine(owr1, null);

	//pro1.sign_demand_all(owr1);
	pro1.verif_all_passets(null, owr1);

	System.out.println("read_ok");
	
	//System.out.println(sgn1);
}*/

public static void
test_str_funcs(String[] args){
	byte[] DEFAULT_KEY = "empty_key".getBytes(config.UTF_8);
	String str1 = convert.bytes_to_str(DEFAULT_KEY);
	System.out.println("str1=" + str1);
}

public static void
test_decimal(String[] args){
	BigDecimal dd = new BigDecimal(new BigInteger("" + 5), -10);
	System.out.println("dd=" + dd.toString());
	System.out.println("dd=" + dd.toPlainString());

	BigDecimal dd2 = new BigDecimal(new BigInteger("" + 3), 7);
	System.out.println("dd2=" + dd2.toString());
	System.out.println("dd2=" + dd2.toPlainString());
}

public static void
test_trust_dom(String[] args){
	if(args.length < 1){
		System.out.println("Needs one arg");
		return;
	}
	//File dir = null;
	//System.out.println("THE_FILE=" + new File(dir, "un_nombre"));
	
		// String dom = args[0];
	
		// List<String> all_sub = passet.all_sub_dom_of(dom);
		// for(String dd: all_sub){
		// System.out.println(dd);
		// }
	
	System.out.println("NO TEST");
}

public static void
test_slice_name(String[] args){
	if(args.length < 1){
		System.out.println("Needs one arg");
		return;
	}
	//String dir1 = file_funcs.as_sliced_path(args[0], passet.DIR_SLICES_1);
	//System.out.println(dir1);
}

public static void
test_transf_descrip(String[] args){
	if(args.length < 1){
		System.out.println("faltan args");
		return;
	}
	//String descr = tag_transfer.as_std_description(args[0]);
	String descr = null;
	System.out.println(descr);
//	if(descr == tag_transfer.TRANSFER_IS_DEMAND){
//		System.out.println("IS_DEMAND");
//	}
	int idx_1 = tag_transfer.STD_TRANSFERS.indexOf(descr);
	if(idx_1 != -1){
		System.out.println("IS_STD_TRANS=" + tag_transfer.STD_TRANSFERS.get(idx_1));
	}
}

public static void
test_input_eq(String[] args){
	
	tag_input in_1 = new tag_input();
	tag_input in_2 = new tag_input();
	tag_input in_3 = new tag_input();
	tag_input in_4 = new tag_input();
	
	in_1.passet_id = new tag_filglid("in_1");
	in_2.passet_id = new tag_filglid("in_2");
	in_3.passet_id = new tag_filglid("in_3");

	in_1.passet_idx = 1;
	
	in_4.passet_id = new tag_filglid(new String("in_"));
	in_4.passet_idx = 1;

	System.out.println("in_1 eq in_4 " + in_1.equals(in_4));
	
	tag_input[] arr_1 = { in_1, in_2, in_3};
	tag_input[] arr_2 = { in_1, in_2, in_3};
	tag_input[] arr_3 = { in_2, in_1, in_3};
	
	List<tag_input> lst_1 = Arrays.asList(arr_1);
	List<tag_input> lst_2 = Arrays.asList(arr_2);
	List<tag_input> lst_3 = Arrays.asList(arr_3);
	
	System.out.println("lst_1 eq lst_2 " + lst_1.equals(lst_2));
	System.out.println("lst_1 eq lst_3 " + lst_1.equals(lst_3));
}

public static void
test_files_eq(String[] args){
	if(args.length < 2){
		System.out.println("faltan args");
		return;
	}

	File ff1 = new File(args[0]);
	File ff2 = new File(args[1]);
	
	String val = " != ";
	if(ff1.equals(ff2)){
		val = " == ";
	}
	System.out.println("" + ff1 + val + ff2);
}

public static void
test_give(String[] args){

	//dbg_check_bytes_funcs(args);

	//String home = System.getProperty("user.home");
	//System.out.println(home);
	
	System.out.println("START_of_test_give");
	
	tag_denomination deno = new tag_denomination();
	deno.ten_exponent = 4;
	deno.multiplier = 5;

	gamal_generator gam = TEST_netmix.read_gamal_sys();
	File root_dir = new File("/home/jose/tmp/test_give/");

	file_funcs.delete_dir(root_dir);
	
	byte[] key1 = "USU_01".getBytes(config.UTF_8);
	key_owner owr1 = new key_owner(key1);

	byte[] key2 = "USU_02".getBytes(config.UTF_8);
	key_owner owr2 = new key_owner(key2);
	
	paccount pcc1 = new paccount();
	pcc1.set_base_dir(root_dir, owr1, net_funcs.MUDP_NET, gam);
	pcc1.set_working_currency(deno.currency_idx);
	
	paccount pcc2 = new paccount();
	pcc2.set_base_dir(root_dir, owr2, net_funcs.MUDP_NET, gam);
	pcc2.set_working_currency(deno.currency_idx);
	
	// create glid 1

	tag_person iss_person1 = new tag_person();
	iss_person1.legal_name = "usuario1";
	iss_person1.legal_id = "id_usu1";
	
	boolean has_diff1 = pcc1.curr_user.update_with(iss_person1);
	if (has_diff1) {
		pcc1.write_current_user(owr1);
	}
	pcc1.create_glid_file(owr1);
	pcc1.read_current_user(owr1);
	
	// create glid 2
	
	tag_person iss_person2 = new tag_person();
	iss_person2.legal_name = "usuario2";
	iss_person2.legal_id = "id_usu2";

	boolean has_diff2 = pcc2.curr_user.update_with(iss_person2);
	if (has_diff2) {
		pcc2.write_current_user(owr2);
	}	
	pcc2.create_glid_file(owr2);
	pcc2.read_current_user(owr2);
	
	// issue passet
	
	List<tag_transfer> all_iss = new ArrayList<tag_transfer>();
	pcc1.issue_passets(1, owr1, deno, all_iss, null);
	
	long cnn_val = 123456;
	nx_conn_id the_coid = new nx_conn_id(cnn_val);
	paccount u1_u2 = pcc1.get_sub_paccount(the_coid);
	paccount u2_u1 = pcc2.get_sub_paccount(the_coid);
	
	pcc1.choose_passets(u1_u2, owr1, all_iss);

	file_funcs.copy_all_files(u1_u2.get_passet_dir(), u2_u1.get_passet_dir());
	
	List<File> all_unsig = Arrays.asList(u2_u1.get_passet_dir().listFiles());
	List<File> all_recep = new ArrayList<File>();
	u2_u1.make_receptacles_for(all_unsig, owr2, all_iss,
			all_recep, pcc2);
	
	file_funcs.copy_all_files(u2_u1.get_recep_dir(), u1_u2.get_recep_dir());

	List<File> all_to_sign = Arrays.asList(u1_u2.get_recep_dir().listFiles());
	u1_u2.try_sign_receptacles_for(all_to_sign, owr1, null);
	
	System.out.println("give_ok");	
}

public static void
test_split_passet(String[] args){
//	if(args.length < 1){
//		System.out.println("Bad arguments");
//		return;
//	}

	System.out.println("START_of_test_split");
	
	tag_denomination deno = new tag_denomination();
	deno.ten_exponent = 4;
	deno.multiplier = 5;

	gamal_generator gam = TEST_netmix.read_gamal_sys();
	File root_dir = new File("/home/jose/tmp/test_split/");
	
	file_funcs.delete_dir(root_dir);

	byte[] key1 = "USU_01".getBytes(config.UTF_8);
	key_owner owr1 = new key_owner(key1);

	paccount pcc1 = new paccount();
	pcc1.set_base_dir(root_dir, owr1, net_funcs.MUDP_NET, gam);
	pcc1.set_working_currency(deno.currency_idx);
	
	tag_person iss_person1 = new tag_person();
	iss_person1.legal_name = "usuario1";
	iss_person1.legal_id = "id_usu1";
	
	boolean has_diff1 = pcc1.curr_user.update_with(iss_person1);
	if (has_diff1) {
		pcc1.write_current_user(owr1);
	}
	pcc1.create_glid_file(owr1);
	pcc1.read_current_user(owr1);

	List<tag_transfer> all_iss = new ArrayList<tag_transfer>();
	pcc1.issue_passets(1, owr1, deno, all_iss, null);
	
	List<File> all_pss_ff = pcc1.get_all_passet_files(all_iss);
	List<tag_transfer> all_spl_tra = pcc1.split_passets(all_pss_ff, owr1, true);
	List<File> all_spl_iss = pcc1.get_all_passet_files(all_spl_tra);
	
	List<String> pths = file_funcs.files_to_path_list(all_spl_iss);
		
	System.out.println("SPLIT_SZ=" + all_spl_iss.size());
	System.out.println("\n\nALL_SPLIT=");
	file_funcs.print_lines(System.out, pths.toArray(new String[0]));
	
//	tag_transfer lst_tra = all_iss.get(0);
//	File unsig_ff = lst_tra.get_the_passet_file();
//	File recep_ff = pro1.make_local_receptacle(unsig_ff, owr1, all_iss);	
//	File signed_ff = pro1.try_sign_receptacle(unsig_ff, owr1);
	
	System.out.println("END_of_test_split");
}

public static void
test_join_passet(String[] args){
	System.out.println("START_of_test_join");
	
	tag_denomination deno = new tag_denomination();
	deno.ten_exponent = 4;
	deno.multiplier = 5;

	gamal_generator gam = TEST_netmix.read_gamal_sys();
	File root_dir = new File("/home/jose/tmp/test_join/");
	
	file_funcs.delete_dir(root_dir);

	byte[] key1 = "USU_01".getBytes(config.UTF_8);
	key_owner owr1 = new key_owner(key1);

	paccount pcc1 = new paccount();
	pcc1.set_base_dir(root_dir, owr1, net_funcs.MUDP_NET, gam);
	pcc1.set_working_currency(deno.currency_idx);
	
	tag_person iss_person1 = new tag_person();
	iss_person1.legal_name = "usuario1";
	iss_person1.legal_id = "id_usu1";
	
	boolean has_diff1 = pcc1.curr_user.update_with(iss_person1);
	if (has_diff1) {
		pcc1.write_current_user(owr1);
	}
	pcc1.create_glid_file(owr1);
	pcc1.read_current_user(owr1);

	List<tag_transfer> all_iss = new ArrayList<tag_transfer>();
	pcc1.issue_passets(1, owr1, deno, all_iss, null);
	
	List<File> all_pss_ff = pcc1.get_all_passet_files(all_iss);
	List<tag_transfer> all_spl_tra = pcc1.split_passets(all_pss_ff, owr1, true);
	List<File> all_spl_iss = pcc1.get_all_passet_files(all_spl_tra);
	List<tag_transfer> all_jned_tra = pcc1.join_passets(all_spl_iss, owr1, true);
	List<File> all_jned_iss = pcc1.get_all_passet_files(all_jned_tra);
	
	List<String> pths = file_funcs.files_to_path_list(all_jned_iss);
		
	System.out.println("JOIN_SZ=" + all_jned_iss.size());
	System.out.println("\n\nALL_JOIN=");
	file_funcs.print_lines(System.out, pths.toArray(new String[0]));
	
	System.out.println("END_of_test_join");
}

public static void
test_save_trissuers(String[] args){
	System.out.println("START_of_test_save_trissuers");
	
	if(args.length < 2){
		System.out.println("args: (-a <glid> | -ck <glid>)");
		return;
	}
	
	int ADD_TRSS = 10;
	int CK_TRSS = 20;
	
	int oper = CK_TRSS;
	if(args[0].equals("-a")){
		oper = ADD_TRSS;
	} else if(! args[0].equals("-ck")){
		System.out.println("args: (-a <glid> | -ck <glid>)");
		return;
	}
	
	tag_denomination deno = new tag_denomination();
	deno.ten_exponent = 4;
	deno.multiplier = 5;

	gamal_generator gam = TEST_netmix.read_gamal_sys();
	File root_dir = new File("/home/jose/tmp/test_passet/");
	
	//file_funcs.delete_dir(root_dir);
	boolean fst_tm = !root_dir.exists();

	byte[] key1 = "USU_01".getBytes(config.UTF_8);
	key_owner owr1 = new key_owner(key1);

	paccount pcc1 = new paccount();
	pcc1.set_base_dir(root_dir, owr1, net_funcs.MUDP_NET, gam);
	pcc1.set_working_currency(deno.currency_idx);
	
	if(fst_tm){
		tag_person iss_person1 = new tag_person();
		iss_person1.legal_name = "usuario1";
		iss_person1.legal_id = "id_usu1";
		
		boolean has_diff1 = pcc1.curr_user.update_with(iss_person1);
		if (has_diff1) {
			pcc1.write_current_user(owr1);
		}
		pcc1.create_glid_file(owr1);
	}
	pcc1.read_current_user(owr1);	
	
	String tg_tss = args[1];
	
	nx_std_coref iss_gld = new nx_std_coref("a_trissuer_glid");
	nx_std_coref trk_gld = new nx_std_coref(tg_tss);
	tag_accoglid tag_iss = new tag_accoglid(iss_gld);
	tag_accoglid tag_trk = new tag_accoglid(trk_gld);
	
	if(oper == ADD_TRSS){
		List<String> to_add = new ArrayList<String>();
		to_add.add(tg_tss);
		pcc1.save_trackers_list(iss_gld, to_add, owr1);
	} else {
		boolean is_trk = pcc1.is_tracker(tag_iss, tag_trk, owr1);
		if(is_trk){
			System.out.println(tg_tss + " IS_TRACKER");
		} else {
			System.out.println(tg_tss + " is_NOT_tracker");
		}
	}
}

public static void
test_chomarks(String[] args){
	
	String usu_pwd = "USU_01";
	
	System.out.println("test_chomarks_INIT");
	
	int curr_idx = config.DEFAULT_CURRENCY;
	
	gamal_generator gam = TEST_netmix.read_gamal_sys();
	File root_dir = new File("/home/jose/tmp/test_chomarks/");
	
	//file_funcs.delete_dir(root_dir);
	boolean fst_tm = !root_dir.exists();

	byte[] key1 = usu_pwd.getBytes(config.UTF_8);
	key_owner owr1 = new key_owner(key1);

	paccount pcc1 = new paccount();
	pcc1.set_base_dir(root_dir, owr1, net_funcs.MUDP_NET, gam);
	pcc1.set_working_currency(curr_idx);
	
	if(fst_tm){
		tag_person iss_person1 = new tag_person();
		iss_person1.legal_name = "usuario1";
		iss_person1.legal_id = "id_usu1";
		
		boolean has_diff1 = pcc1.curr_user.update_with(iss_person1);
		if (has_diff1) {
			pcc1.write_current_user(owr1);
		}
		pcc1.create_glid_file(owr1);
	}
	pcc1.read_current_user(owr1);	

	if(args.length > 0){
		//String all_args = Arrays.toString(args);
		System.out.println("test_chomarks_CHOOSE");
		
		choose_chomarks(pcc1, owr1, usu_pwd);
		return;
	}
	
	if(fst_tm){
		tag_denomination deno = new tag_denomination(curr_idx);
		deno.ten_exponent = 4;
		deno.multiplier = 5;
		
		List<tag_transfer> all_iss = new ArrayList<tag_transfer>();
		pcc1.issue_passets(1, owr1, deno, all_iss, null);
		
		List<File> all_spl_0 = pcc1.get_all_passet_files(all_iss);
		List<tag_transfer> all_spl_1_tra = pcc1.split_passets(all_spl_0, owr1, true);
		List<File> all_spl_1 = pcc1.get_all_passet_files(all_spl_1_tra);
		List<tag_transfer> all_spl_2_tra = pcc1.split_passets(all_spl_1, owr1, true);
		List<File> all_spl_2 = pcc1.get_all_passet_files(all_spl_2_tra);
		List<tag_transfer> all_spl_3_tra = pcc1.split_passets(all_spl_2, owr1, true);
		List<File> all_spl_3 = pcc1.get_all_passet_files(all_spl_3_tra);
		
		List<String> pths = file_funcs.files_to_path_list(all_spl_3);
			
		System.out.println("SPLIT_SZ=" + all_spl_3.size());
		System.out.println("\n\nALL_SPLIT=");
		file_funcs.print_lines(System.out, pths.toArray(new String[0]));
	}
}

public static void
choose_chomarks(paccount pcc1, key_owner owr, String usu_pwd){
	nx_std_coref gli1 = pcc1.get_glid(owr);
	String gli1_str = gli1.get_str();
	System.out.println("GLID_USU=" + gli1_str);
	
	String[] args_01 = {"-k", usu_pwd, "-tc", "gli_001"};
	command_line_choose.main(args_01);

	String[] args_02 = {"-k", usu_pwd, "-cd", "gli_001"};
	command_line_choose.main(args_02);

	String[] args_03 = {"-k", usu_pwd, "-tch", "-tat", gli1_str};
	command_line_issuer.main(args_03);

	// $SELECTOR -k "usu1" +3 1
	// $SELECTOR -k "usu1" +2 1z
	// $SELECTOR -k "usu1" +1 5z
}

}
