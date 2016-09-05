package emetcode.economics.passet;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.net.netmix.nx_std_coref;

@SuppressWarnings("serial")
public class transfers_map extends TreeMap<tag_accoglid, List<tag_transfer>> {
	public transfers_map() {
	}

	public static transfers_map create_by_trackers(List<tag_transfer> all_iss,
			nx_std_coref loc_gli, transfers_map to_update, paccount local_pcc,
			key_owner owr) {

		if (loc_gli == null) {
			throw new bad_passet(2);
		}
		if (to_update != null) {
			to_update.clear();
		}

		transfers_map mm = new transfers_map();
		for (tag_transfer tiss : all_iss) {
			tag_accoglid trk_gld = tiss.get_tracker_accoglid();
			if (to_update != null) {
				if (local_pcc == null) {
					throw new bad_passet(2);
				}
				if (owr == null) {
					throw new bad_passet(2);
				}
				tag_accoglid iss = tiss.get_root_issuer();
				if (!local_pcc.is_tracker(iss, trk_gld, owr)) {
					//String iss_gld_str = iss.get_str();
					if (!to_update.containsKey(iss)) {
						to_update.put(iss, null);
					}
				}
			}

			//String trk_glid_str = trk_gld.get_str();

			if (loc_gli != null) {
				nx_std_coref tkr_gli = trk_gld.the_glid;
				if (tkr_gli.equals(loc_gli)) {
					continue;
				}
			}

			mm.add_relation(trk_gld, tiss);
		}
		return mm;
	}

	void add_relation(tag_accoglid dst_gld, tag_transfer the_tra) {
		List<tag_transfer> tra_grp = null;
		if (containsKey(dst_gld)) {
			tra_grp = get(dst_gld);
		} else {
			tra_grp = new ArrayList<tag_transfer>();
			put(dst_gld, tra_grp);
		}
		tra_grp.add(the_tra);
	}
}
