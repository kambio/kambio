package emetcode.economics.passet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class tag_person {

	public static final String legal_name_field = "Legal name.";
	public static final String legal_id_field = "Legal id.";
	public static final String country_code_field = "Country code.";
	public static final String domain_name_field = "Domain name.";
	public static final String i2p_address_field = "I2P address.";
	public static final String email_field = "Email.";
	public static final String phone_number_field = "Phone number.";
	public static final String SWIFT_code_field = "SWIFT code.";
	public static final String account_number_field = "Account number.";

	public String legal_name;
	public String legal_id;
	public String contry_code;
	public String network_domain_name;
	public String i2p_address;
	public String email;
	public String phone_number;
	public String SWIFT_code;
	public String account_number;

	private boolean has_diff;

	public tag_person() {
		init_tag_person();
	}

	public tag_person(tag_person orig) {
		init_tag_person();
		if (orig == null) {
			return;
		}
		legal_name = orig.legal_name;
		legal_id = orig.legal_id;
		contry_code = orig.contry_code;
		network_domain_name = orig.network_domain_name;
		i2p_address = orig.i2p_address;
		email = orig.email;
		phone_number = orig.phone_number;
		SWIFT_code = orig.SWIFT_code;
		account_number = orig.account_number;

		has_diff = false;
	}

	private String updated_field(String old_fld, String new_fld) {
		boolean old_unk = config.is_unk(old_fld);
		boolean new_unk = config.is_unk(new_fld);
		if (!new_unk && old_unk) {
			has_diff = true;
			return new_fld;
		}
		if (new_unk && old_unk) {
			return config.UNKNOWN_STR;
		}
		return old_fld;
	}

	public boolean update_with(tag_person diff) {
		if (diff == null) {
			return false;
		}
		has_diff = false;

		legal_name = updated_field(legal_name, diff.legal_name);
		legal_id = updated_field(legal_id, diff.legal_id);
		contry_code = updated_field(contry_code, diff.contry_code);
		network_domain_name = updated_field(network_domain_name, diff.network_domain_name);
		i2p_address = updated_field(i2p_address, diff.i2p_address);
		email = updated_field(email, diff.email);
		phone_number = updated_field(phone_number, diff.phone_number);
		SWIFT_code = updated_field(SWIFT_code, diff.SWIFT_code);
		account_number = updated_field(account_number, diff.account_number);

		return has_diff;
	}

	void init_tag_person() {
		legal_name = config.UNKNOWN_STR;
		legal_id = config.UNKNOWN_STR;
		contry_code = config.UNKNOWN_STR;
		network_domain_name = config.UNKNOWN_STR;
		i2p_address = config.UNKNOWN_STR;
		email = config.UNKNOWN_STR;
		phone_number = config.UNKNOWN_STR;
		SWIFT_code = config.UNKNOWN_STR;
		account_number = config.UNKNOWN_STR;

		has_diff = false;
	}

	void filter_person_lines() {
		legal_name = parse.filter_string(legal_name);
		legal_id = parse.filter_string(legal_id);
		contry_code = parse.filter_string(contry_code);
		network_domain_name = parse.filter_string(network_domain_name);
		i2p_address = parse.filter_string(i2p_address);
		email = parse.filter_string(email);
		phone_number = parse.filter_string(phone_number);
		SWIFT_code = parse.filter_string(SWIFT_code);
		account_number = parse.filter_string(account_number);

		if ((contry_code != config.UNKNOWN_STR)
				&& !iso.country_codes.containsKey(contry_code)) {
			contry_code = config.UNKNOWN_STR;
		}
	}

	public List<String> get_person_lines(String title) {
		List<String> txt = new ArrayList<String>();
		add_person_lines_to(txt, title);
		return txt;
	}

	void add_person_lines_to(List<String> txt, String title) {
		filter_person_lines();

//		parse.add_next_title_to(txt, title);
		parse.add_next_field_to(txt, title, legal_name_field, legal_name);
		parse.add_next_field_to(txt, title, legal_id_field, legal_id);
		parse.add_next_field_to(txt, title, country_code_field, contry_code);
		parse.add_next_field_to(txt, title, domain_name_field, network_domain_name);
		parse.add_next_field_to(txt, title, i2p_address_field, i2p_address);
		parse.add_next_field_to(txt, title, email_field, email);
		parse.add_next_field_to(txt, title, phone_number_field, phone_number);
		parse.add_next_field_to(txt, title, SWIFT_code_field, SWIFT_code);
		parse.add_next_field_to(txt, title, account_number_field, account_number);
//		txt.add(parse.end_of_title);

		parse.check_line_list(txt);
	}

	public void init_person_with(List<String> all_lines, String title) {
		parse.check_line_list(all_lines);

		ListIterator<String> it1 = all_lines.listIterator();
		init_person_with(it1, title);
	}

	public void init_person_with(ListIterator<String> it1, String title) {
//		parse.get_next_title_from(it1, title);
		legal_name = parse.get_next_field_from(it1, title, legal_name_field);
		legal_id = parse.get_next_field_from(it1, title, legal_id_field);
		contry_code = parse.get_next_field_from(it1, title, country_code_field);
		network_domain_name = parse.get_next_field_from(it1, title, domain_name_field);
		i2p_address = parse.get_next_field_from(it1, title, i2p_address_field);
		email = parse.get_next_field_from(it1, title, email_field);
		phone_number = parse.get_next_field_from(it1, title, phone_number_field);
		SWIFT_code = parse.get_next_field_from(it1, title, SWIFT_code_field);
		account_number = parse.get_next_field_from(it1, title, account_number_field);
//		parse.get_next_end_of_title(it1);

		has_diff = false;
	}

}
