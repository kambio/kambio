package emetcode.economics.passet.locale;

public class str_en extends L {
	public static void set() {
		invalid_multiple_of_deno = "Invalid multiplier in denomination parameter. Must be 1, 2, or 5.";
		invalid_exponent_of_deno = "Invalid exponent in denomination parameter. Must be (%s <= expo <= %s)";
		no_such_file = "No such file '%s'";
		internal_error_different_currencies = "Internal error. "
				+ "Diferent file currency and working (dir) currency\n"
				+ "file_curr=%s\n" + "dir_curr=%s\n" + "curr_dir=%s";
		internal_error_bad_ref_file = "Internal error. Bad ref file '%s'";
		internal_error_null_file = "Internal error. null file";
		invalid_passet_dir = "Invalid passet directory \n\t full_pro= \n\t %s \n\t passet_pth= \n\t %s";
		cannot_delete_file = "Cannot delete file '%s'";
		internal_error_inexistant_file = "Internal error. Inexistant file '%s'";
		no_encrypted_receptacle_found = "No encrypted receptacle found !!!";
		null_receiver_lines = "Null receiver lines";
		different_read_and_calc_section_consec = "Different read consec '%s' and section consec '%s' with file '%s'.";
		different_read_prev_sha_and_calc_prev_sha = "Different read prev sha and calc prev sha with file '%s'.";
		cannot_make_receptacle = "Cannot make receptacle";
		no_such_receptacle_file = "No such receptacle file '%s'";
		bad_receptacle_file = "Bad receptacle file '%s'";
		recepid = "recepid";
		local_creation_date = "local creation date";
		remote_creation_date = "remote creation date";
		sync_date = "sync date";
		flush_date = "flush date";
		cannot_transfer_before_time = "cannot transfer before time %s. Now is %s.";
		inconsistent_transfer_times = "inconsistent transfer times." +
				" (%s < %s) is false. transfer verification failed.";
		cannot_transfer_before_last_giving = "cannot transfer before last giving."
				+ " inconsistent transaction sequence times.\n"
				+ "(%s < %s) is false";
		cannot_transfer_before_previous_one = "cannot transfer before previous one.\n"
				+ "inconsistent transaction sequence times.\n(%s < %s) is false";
		cannot_acknowledge_before_last_giving = "cannot acknowledge before last giving."
				+ " inconsistent acknowledgment sequence times.\n"
				+ "(%s < %s) is false";
		cannot_acknowledge_before_previous_one = "cannot acknowledge before previous one.\n"
				+ "inconsistent acknowledgment sequence times.\n(%s < %s) is false";
		cannot_demand_before_last_giving = "cannot demand before last giving."
				+ " inconsistent demand sequence times.\n"
				+ "(%s < %s) is false";
		cannot_demand_before_last_acknowledgment = "cannot demand before last acknowledgment."
				+ " inconsistent demand sequence times.\n"
				+ "(%s < %s) is false";
		asking_for_0_iss_tails = "Asking for 0 iss tails";
		not_issuer = "User is NOT issuer. Must be issuer for this operation.";
		verifying_files_requires_private_key = "verifying files requires private key.\n%s";
		only_issuer_can_verify_diff_files = "Only issuer can varify diff files.\n%s";
		cannot_receive_diff_files = "Cannot receive diff files.\n%s";
		cannot_read_file = "Cannot read file '%s'";
		already_demanded_passet = "Already demanded passet. Cannot verify \n%s"
				+ "\n\t dem_file\n%s";
		not_trusted_issuer = "Not a trusted issuer '%s'";
		not_accepted_tracker = "Not an accepted tracker '%s'";
		corrupt_diff_passet_file = "Corrupt diff passet file \n'%s'";
		corrupt_passet_file = "Corrupt passet file \n'%s'";
		cannot_export_remote = "Cannot export remote '%s'";
		cannot_create_glid = "Cannot create glid. Create a user info file or a user image file.";
		parse_error = "Parse error with '%s'.";
		cannot_trust_file = "Cannot trust file '%s'";

	}
}
