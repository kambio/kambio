package emetcode.economics.netpasser.locale;

public class L {

	public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

	public static String invalid_file_oper = UNKNOWN_ERROR;
	public static String not_all_diff_files = UNKNOWN_ERROR;
	public static String not_all_files_demanded = UNKNOWN_ERROR;
	public static String cannot_connect_to_unkown_remote_peer = UNKNOWN_ERROR;
	public static String cannot_find_remote_host = UNKNOWN_ERROR;
	public static String could_not_connect_to = UNKNOWN_ERROR;
	public static String cannot_connect_to_itself = UNKNOWN_ERROR;
	public static String could_secure_connection_with = UNKNOWN_ERROR;
	public static String expecting_other_coid = UNKNOWN_ERROR;
	public static String expecting_other_msg = UNKNOWN_ERROR;
	public static String no_connection_found = UNKNOWN_ERROR;
	public static String could_not_create_channel = UNKNOWN_ERROR;
	public static String did_not_create_channel = UNKNOWN_ERROR;
	public static String asking_for_zero_passids = UNKNOWN_ERROR;
	public static String timeout_waiting_for_thread = UNKNOWN_ERROR;
	public static String null_rechange_key = UNKNOWN_ERROR;
	public static String channel_does_not_accept_delegations = UNKNOWN_ERROR;
	public static String invalid_transaction = UNKNOWN_ERROR;
	public static String bad_inverse_oper = UNKNOWN_ERROR;
	public static String bad_net_oper = UNKNOWN_ERROR;
	public static String bad_cancel_oper = UNKNOWN_ERROR;
	public static String null_deno_counter = UNKNOWN_ERROR;
	public static String recv_invalid_oper = UNKNOWN_ERROR;
	public static String null_forward_key = UNKNOWN_ERROR;
	public static String channel_does_not_accept_forward_delegations = UNKNOWN_ERROR;
	public static String verification_failed = UNKNOWN_ERROR;
	public static String needs_mudp = UNKNOWN_ERROR;
	public static String invalid_g_option = UNKNOWN_ERROR;
	public static String invalid_local_address_for_transfers = UNKNOWN_ERROR;

	static {
		str_en.set();
	}

	public static void set_lang(String two_letter_code) {
		if (two_letter_code == null) {
			return;
		}
		String cod = two_letter_code.toLowerCase();
		if (cod.equals("en")) {
			str_en.set();
			return;
		} else if (cod.equals("es")) {
			str_es.set();
			return;
		}
	}

}
