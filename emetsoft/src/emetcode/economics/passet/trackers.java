package emetcode.economics.passet;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.net.netmix.nx_std_coref;

public class trackers {

	public List<String> all_trackers;
	public String next_tracker;

	public trackers() {
		init_trackers();
	}

	private void init_trackers() {
		all_trackers = null;
		next_tracker = null;
	}

	public void init_trackers(paccount pcc, key_owner owr) {
		File ff1 = null;

		if (all_trackers != null) {
			return;
		}
		if (next_tracker != null) {
			return;
		}

		ff1 = pcc.get_all_trackers_file();
		all_trackers = file_funcs.read_list_file(ff1, owr);
		if (all_trackers == null) {
			all_trackers = new ArrayList<String>();
		}

		ff1 = pcc.get_next_tracker_file();
		next_tracker = mem_file.read_encrypted_string(ff1, owr);
	}

	public void update_trackers(paccount pcc, key_owner owr) {
		File ff1 = null;
		
		ff1 = pcc.get_next_tracker_file();
		if(next_tracker != null){
			mem_file.write_encrypted_string(ff1, owr, next_tracker);
			if(! all_trackers.contains(next_tracker)){
				all_trackers.add(next_tracker);
			}
		}
		
		ff1 = pcc.get_all_trackers_file();
		file_funcs.write_list_file(ff1, owr, all_trackers);
		
		nx_std_coref loc_iss = pcc.get_glid(owr);
		pcc.save_trackers_list(loc_iss, all_trackers, owr);
		
		init_trackers(pcc, owr);
	}
	
	public void print(PrintStream os){
		os.println("ALL_TRACKERS=");
		for(String trk: all_trackers){
			os.println(trk);
		}
		os.println("NEXT_TRACKER=" + next_tracker);
	}
}
