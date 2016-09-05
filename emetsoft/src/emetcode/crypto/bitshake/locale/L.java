package emetcode.crypto.bitshake.locale;

public class L {
	
	public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

	public static String could_not_process_bytes = UNKNOWN_ERROR;
	public static String cannot_calc_sha_of_output_data = UNKNOWN_ERROR;
	public static String cannot_calc_sha_of_processed_data = UNKNOWN_ERROR;
	public static String could_not_encrypt_bytes = UNKNOWN_ERROR;
	public static String could_not_decrypt_bytes = UNKNOWN_ERROR;
	public static String data_verification_failed = UNKNOWN_ERROR;
	public static String bytes_seem_raw_bytes = UNKNOWN_ERROR;
	public static String bytes_seem_with_sha_verif = UNKNOWN_ERROR;
	public static String no_sha_header_found = UNKNOWN_ERROR;				
	public static String header_error_no_bytes_found = UNKNOWN_ERROR;
	public static String internal_err_null_src = UNKNOWN_ERROR;
	public static String internal_err_invalid_idx = UNKNOWN_ERROR;
	public static String internal_err_invalid_idx_2 = UNKNOWN_ERROR;
	public static String cannot_parse_int = UNKNOWN_ERROR;
	public static String cannot_parse_long = UNKNOWN_ERROR;
	public static String cannot_set_signer_data = UNKNOWN_ERROR;
	public static String cannot_read_signer_data = UNKNOWN_ERROR;
	public static String cannot_write_signer_data_1 = UNKNOWN_ERROR;
	public static String given_signer_is_not_equal = UNKNOWN_ERROR;
	public static String internal_error_uncomplete_signer_arguments = UNKNOWN_ERROR;
	public static String invalid_signature_size = UNKNOWN_ERROR;
	public static String internal_error_cannot_check = UNKNOWN_ERROR;
	public static String invalid_length = UNKNOWN_ERROR;
	public static String invalid_format_no_dots = UNKNOWN_ERROR;
	public static String invalid_format_no_prefix = UNKNOWN_ERROR;
	public static String invalid_format_no_sufix = UNKNOWN_ERROR;
	public static String cannot_write_null_data = UNKNOWN_ERROR;
	public static String cannot_write_empty_data = UNKNOWN_ERROR;
	public static String cannot_encrypt_data = UNKNOWN_ERROR;
	public static String cannot_write_data = UNKNOWN_ERROR;
	public static String cannot_decrypt_data = UNKNOWN_ERROR;
	public static String null_dir = UNKNOWN_ERROR;
	public static String not_a_dir = UNKNOWN_ERROR;
	public static String cannot_get_canonical_form_of = UNKNOWN_ERROR;
	public static String cannot_delete_dir = UNKNOWN_ERROR;
	public static String cannot_create_zip_file = UNKNOWN_ERROR;
	public static String cannot_read_file = UNKNOWN_ERROR;
	public static String cannot_write_spend_file = UNKNOWN_ERROR;
	public static String invalid_sha_while_decryptig_transfer = UNKNOWN_ERROR;
	public static String cannot_save_null_transfer_pvks = UNKNOWN_ERROR;
	public static String bad_header_found = UNKNOWN_ERROR;
	public static String cannot_read_list_file = UNKNOWN_ERROR;
	
	static{
		str_en.set();
	}
	
	public static void set_lang(String two_letter_code){
		if(two_letter_code == null){ return; }
		String cod = two_letter_code.toLowerCase();
		if(cod.equals("en")){
			str_en.set();
			return;
		}
		else if(cod.equals("es")){
			str_es.set();
			return;
		}
	}
	
}
