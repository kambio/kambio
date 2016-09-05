package emetcode.economics.passet.locale;

public class L {

	public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

	public static String invalid_multiple_of_deno = UNKNOWN_ERROR;
	public static String invalid_exponent_of_deno = UNKNOWN_ERROR;
	public static String no_such_file = UNKNOWN_ERROR;
	public static String internal_error_different_currencies = UNKNOWN_ERROR;
	public static String internal_error_bad_ref_file = UNKNOWN_ERROR;
	public static String internal_error_null_file = UNKNOWN_ERROR;
	public static String invalid_passet_dir = UNKNOWN_ERROR;
	public static String cannot_delete_file = UNKNOWN_ERROR;
	public static String internal_error_inexistant_file = UNKNOWN_ERROR;
	public static String no_encrypted_receptacle_found = UNKNOWN_ERROR;
	public static String null_receiver_lines = UNKNOWN_ERROR;
	public static String different_read_and_calc_section_consec = UNKNOWN_ERROR;
	public static String different_read_prev_sha_and_calc_prev_sha = UNKNOWN_ERROR;
	public static String cannot_make_receptacle = UNKNOWN_ERROR;
	public static String no_such_receptacle_file = UNKNOWN_ERROR;
	public static String bad_receptacle_file = UNKNOWN_ERROR;
	public static String recepid = UNKNOWN_ERROR;
	public static String local_creation_date = UNKNOWN_ERROR;
	public static String remote_creation_date = UNKNOWN_ERROR;
	public static String sync_date = UNKNOWN_ERROR;
	public static String flush_date = UNKNOWN_ERROR;
	public static String cannot_transfer_before_time = UNKNOWN_ERROR;
	public static String inconsistent_transfer_times = UNKNOWN_ERROR;
	public static String cannot_transfer_before_last_giving = UNKNOWN_ERROR;
	public static String cannot_transfer_before_previous_one = UNKNOWN_ERROR;
	public static String cannot_acknowledge_before_last_giving = UNKNOWN_ERROR;
	public static String cannot_acknowledge_before_previous_one = UNKNOWN_ERROR;
	public static String cannot_demand_before_last_giving = UNKNOWN_ERROR;
	public static String cannot_demand_before_last_acknowledgment = UNKNOWN_ERROR;
	public static String asking_for_0_iss_tails = UNKNOWN_ERROR;
	public static String not_issuer = UNKNOWN_ERROR;
	public static String verifying_files_requires_private_key = UNKNOWN_ERROR;
	public static String only_issuer_can_verify_diff_files = UNKNOWN_ERROR;
	public static String cannot_receive_diff_files = UNKNOWN_ERROR;
	public static String cannot_read_file = UNKNOWN_ERROR;
	public static String already_demanded_passet = UNKNOWN_ERROR;
	public static String not_trusted_issuer = UNKNOWN_ERROR;
	public static String not_accepted_tracker = UNKNOWN_ERROR;
	public static String corrupt_diff_passet_file = UNKNOWN_ERROR;
	public static String corrupt_passet_file = UNKNOWN_ERROR;
	public static String cannot_export_remote = UNKNOWN_ERROR;
	public static String cannot_create_glid = UNKNOWN_ERROR;
	public static String parse_error = UNKNOWN_ERROR;
	public static String cannot_trust_file = UNKNOWN_ERROR;

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
