package emetcode.economics.passet;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.key_owner;

public class trissuers {

	public List<String> trusted;
	public List<String> not_trusted;

	private Set<String> set_of_trusted;
	private Set<String> set_of_not_trusted;

	private Map<String, Integer> map_of_trust_vals;
	private Comparator<String> comparator;
	
	public trissuers() {
		init_trusted_issuers();
	}

	private void init_trusted_issuers() {
		trusted = null;
		not_trusted = null;
		set_of_trusted = null;
		set_of_not_trusted = null;
	}

	public void start_trissuers() {
		if (trusted == null) {
			trusted = new ArrayList<String>();
		}
		set_of_trusted = new TreeSet<String>();
		set_of_trusted.addAll(trusted);

		if (not_trusted == null) {
			not_trusted = new ArrayList<String>();
		}
		set_of_not_trusted = new TreeSet<String>();
		set_of_not_trusted.addAll(not_trusted);

		trusted.removeAll(not_trusted);
		set_of_trusted.removeAll(set_of_not_trusted);
	}
	
	public void init_trissuers(paccount pcc, key_owner owr) {
		File ff1 = null;

		if (trusted != null) {
			return;
		}
		if (not_trusted != null) {
			return;
		}

		ff1 = pcc.get_trusted_file();
		trusted = parse.read_encrypted_lines(ff1, owr);

		ff1 = pcc.get_not_trusted_file();
		not_trusted = parse.read_encrypted_lines(ff1, owr);

		start_trissuers();
	}

	private static void update_trissuer_file(File ff1, key_owner owr,
			List<String> all_doms) {
		SortedSet<String> all_lines = new TreeSet<String>();
		all_lines.addAll(all_doms);
		parse.write_encrypted_set(ff1, owr, all_lines);
	}

	public void update_trissuer_files(paccount pcc, key_owner owr) {
		update_trissuer_file(pcc.get_trusted_file(), owr, trusted);
		update_trissuer_file(pcc.get_not_trusted_file(), owr, not_trusted);
		init_trissuers(pcc, owr);
		pcc.delete_not_trusted_trackers(this);
	}

	private int cmp_trissuers(String d1, String d2) {
		Integer tt1 = map_of_trust_vals.get(d1);
		Integer tt2 = map_of_trust_vals.get(d2);
		if ((tt1 != null) && (tt2 != null)) {
			return convert.cmp_int(tt1, tt2);
		}
		return 0;
	}

	public void init_trissuer_comparator(paccount pcc, key_owner owr) {

		if (comparator != null) {
			return;
		}

		init_trissuers(pcc, owr);

		map_of_trust_vals = new TreeMap<String, Integer>();
		int pos = 0;
		for (String iss : trusted) {
			pos++;
			map_of_trust_vals.put(iss, pos);
		}
		for (String iss : not_trusted) {
			pos++;
			map_of_trust_vals.put(iss, pos);
		}

		Comparator<String> cmp_iss = new Comparator<String>() {
			public int compare(String d1, String d2) {
				return cmp_trissuers(d1, d2);
			}
		};

		comparator = cmp_iss;
	}

	public Comparator<String> get_trissuer_comparator(){
		return comparator;
	}
	
	public boolean can_trust(String iss) {
		return set_of_trusted.contains(iss);		
	}
	
	public boolean can_trust(tag_accoglid iss) {
		return set_of_trusted.contains(iss.get_str());		
	}
}
