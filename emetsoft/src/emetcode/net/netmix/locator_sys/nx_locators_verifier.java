package emetcode.net.netmix.locator_sys;

import java.io.File;
import java.util.List;

public interface nx_locators_verifier {

	public boolean verif_locator_files(File prev_verif_file,
			File nxt_verif_file, List<File> prev_files, List<File> nxt_files);

	public String get_verifier_file_name();

}
