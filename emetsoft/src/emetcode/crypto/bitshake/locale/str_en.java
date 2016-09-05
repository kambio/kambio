package emetcode.crypto.bitshake.locale;

public class str_en extends L {
	public static void set() {
		could_not_process_bytes = "Could not process null bytes.";
		cannot_calc_sha_of_output_data = "Cannot calc sha of output data.";
		cannot_calc_sha_of_processed_data = "Cannot calc sha of processed data.";
		could_not_encrypt_bytes = "Could not encrypt bytes";
		could_not_decrypt_bytes = "Could not decrypt bytes";
		data_verification_failed = "Data verification (sha info) failed." +
				"Wrong key or corrupted file.";
		bytes_seem_raw_bytes = "Processing as bytes with sha header " + 
				"but they seem raw bytes. Use proper processing option.";
		bytes_seem_with_sha_verif = "Processing as raw bytes " + 
				"but they seem bytes with sha header. Use proper processing option.";
		no_sha_header_found = "Processing as bytes with sha header " + 
				"but no sha header found. Corrupted bytes.";
		header_error_no_bytes_found = "Header error. No bytes to process found. Corrupted bytes.";
		internal_err_null_src = "Internal error. null src.";
		internal_err_invalid_idx = "Internal error. invalid idx.";
		internal_err_invalid_idx_2 = "Internal error. invalid idx 2.";
		cannot_parse_int = "Conversion error. Cannot parse integer string '%s'.";
		cannot_parse_long = "Conversion error. Cannot parse long string '%s'.";
		cannot_set_signer_data = "Cannot set_signer_data.";
		cannot_read_signer_data = "Cannot read signer data.";
		cannot_write_signer_data_1 = "Cannot write signer data 1.";
		given_signer_is_not_equal = "Given signer is not equal.";
		internal_error_uncomplete_signer_arguments = 
			"Cannot sign. Uncomplete signer arguments. Internal error.";
		invalid_signature_size = "Invalid signature size.";
		internal_error_cannot_check = "Cannot check. Internal error.";
		invalid_length = "invalid length";
		invalid_format_no_dots = "Invalid format. No dots found.";
		invalid_format_no_prefix = "Invalid format. No prefix found.";
		invalid_format_no_sufix = "Invalid format. No sufix found.";
		cannot_write_null_data = "Cannot write null data.";
		cannot_write_empty_data = "Cannot write empty data.";
		cannot_encrypt_data = "Cannot encrypt data.";
		cannot_write_data = "Cannot write data.";
		cannot_decrypt_data = "Cannot decrypt data.";
		null_dir = "null dir";
		not_a_dir = "file %s is not a dir";
		cannot_get_canonical_form_of = "Cannot get cannonical form of file %s";
		cannot_delete_dir = "Cannot delete dir %s";
		cannot_create_zip_file = "Cannot created zip file %s";		
		cannot_read_file = "Cannot read file '%s'";
		cannot_write_spend_file = "Cannot write spend file '%s'";
		invalid_sha_while_decryptig_transfer = "Invalid sha while decrypting transfer";
		cannot_save_null_transfer_pvks = "Cannot save null private keys of transfer in file '%s'";
		bad_header_found = "Bad header found. Not Use / Use sha option ?";
		cannot_read_list_file = "File error. Cannot read list file '%s'";
	}
}
