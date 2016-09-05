package emetcode.economics.passet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.economics.passet.locale.L;

public class tag_denomination implements Comparator<tag_denomination>,
		Comparable<tag_denomination> {

	// denominations with multiplier equal one
	public static final int MIN_EXPO = -15;
	public static final int MAX_EXPO = 15;
	public static final int INVALID_EXPO = 100;

	public static final String[] pos_expo_amounts = { "0 ", "00 ", ",000 ",
			"0,000 ", "00,000 ", ",000,000 ", "0,000,000 ", "00,000,000 ",
			",000,000,000 ", "0,000,000,000 ", "00,000,000,000 ",
			",000,000,000,000 ", "0,000,000,000,000 ", "00,000,000,000,000 ",
			",000,000,000,000,000 ", };

	public static final String[] neg_expo_amounts = { " / 10 of ",
			" / 100 of ", " / 1,000 of ", " / 10,000 of ", " / 100,000 of ",
			" / 1,000,000 of ", " / 10,000,000 of ", " / 100,000,000 of ",
			" / 1,000,000,000 of ", " / 10,000,000,000 of ",
			" / 100,000,000,000 of ", " / 1,000,000,000,000 of ",
			" / 10,000,000,000,000 of ", " / 100,000,000,000,000 of ",
			" / 1,000,000,000,000,000 of ", };

	public static final String[] pos_expo_txt_amounts = { "ten ",
			"one hundred ", "one kilo ", "ten kilo ", "one hundred kilo ",
			"one mega ", "ten mega ", "one hundred mega ", "one giga ",
			"ten giga ", "one hundred giga ", "one tera ", "ten tera ",
			"one hundred tera ", "one peta ", };

	public static final String zero_expo_txt_amount = "one ";

	public static final String[] neg_expo_txt_amounts = { "one hundred mili ",
			"ten mili ", "one mili ", "one hundred micro ", "ten micro ",
			"one micro ", "one hundred nano ", "ten nano ", "one nano ",
			"one hundred pico ", "ten pico ", "one pico ",
			"one hundred femto ", "ten femto ", "one femto ", };

	// denominations with multiplier equal two

	public static final String[] pos_expo_txt_amounts_mult_two = { "twenty ",
			"two hundred ", "two kilo ", "twenty kilo ", "two hundred kilo ",
			"two mega ", "twenty mega ", "two hundred mega ", "two giga ",
			"twenty giga ", "two hundred giga ", "two tera ", "twenty tera ",
			"two hundred tera ", "two peta ", };

	public static final String zero_expo_txt_amount_mult_two = "two ";

	public static final String[] neg_expo_txt_amounts_mult_two = {
			"two hundred mili ", "twenty mili ", "two mili ",
			"two hundred micro ", "twenty micro ", "two micro ",
			"two hundred nano ", "twenty nano ", "two nano ",
			"two hundred pico ", "twenty pico ", "two pico ",
			"two hundred femto ", "twenty femto ", "two femto ", };

	// denominations with multiplier equal five

	public static final String[] pos_expo_txt_amounts_mult_five = { "fifty ",
			"five hundred ", "five kilo ", "fifty kilo ", "five hundred kilo ",
			"five mega ", "fifty mega ", "five hundred mega ", "five giga ",
			"fifty giga ", "five hundred giga ", "five tera ", "fifty tera ",
			"five hundred tera ", "five peta ", };

	public static final String zero_expo_txt_amount_mult_five = "five ";

	public static final String[] neg_expo_txt_amounts_mult_five = {
			"five hundred mili ", "fifty mili ", "five mili ",
			"five hundred micro ", "fifty micro ", "five micro ",
			"five hundred nano ", "fifty nano ", "five nano ",
			"five hundred pico ", "fifty pico ", "five pico ",
			"five hundred femto ", "fifty femto ", "five femto ", };

	public static final String amount_number_denomination_field = "Amount (number).";
	public static final String amount_text_denomination_field = "Amount (text).";

	public static final String amount_denomination_multiplier_field = "Amount's multiplier (first digit).";
	public static final String amount_denomination_exponent_field = "Amount's exponent (num zeros).";
	public static final String amount_denomination_idx_field = "Amount's currency code.";

	public int currency_idx;
	public int ten_exponent;
	public int multiplier;

	public tag_denomination() {
		init_tag_denomination(config.DEFAULT_CURRENCY, 0, 1);
	}

	public tag_denomination(int curr_idx) {
		init_tag_denomination(curr_idx, 0, 1);
	}

	public tag_denomination(tag_denomination deno) {
		if (deno != null) {
			init_tag_denomination(deno.currency_idx, deno.ten_exponent,
					deno.multiplier);
		} else {
			init_tag_denomination(config.DEFAULT_CURRENCY, 0, 1);
		}
	}

	public boolean equals(Object oo) {
		if (oo == null) {
			return false;
		}
		if (!(oo instanceof tag_denomination)) {
			throw new bad_passet(2);
			// return false;
		}
		tag_denomination deno = (tag_denomination) oo;
		boolean c1 = (currency_idx == deno.currency_idx);
		boolean c2 = (ten_exponent == deno.ten_exponent);
		boolean c3 = (multiplier == deno.multiplier);
		boolean eq = (c1 && c2 && c3);
		return eq;
	}

	public int hashCode() {
		// Hopefully never used only implemented for recommendation of equals
		// doc.
		int tot = currency_idx * ten_exponent * multiplier;
		return tot;
	}

	public tag_denomination(int curr_idx, int expo, int multi) {
		init_tag_denomination(curr_idx, expo, multi);
	}

	public void init_tag_denomination(int curr_idx, int expo, int multi) {
		currency_idx = curr_idx;
		ten_exponent = expo;
		multiplier = multi;

		if (!is_valid_deno()) {
			throw new bad_passet(2);
		}
	}

	public boolean is_valid_deno() {
		if (!is_valid_currency(currency_idx)) {
			return false;
		}
		if (!is_valid_expo(ten_exponent)) {
			return false;
		}
		if (!is_valid_multi(multiplier)) {
			return false;
		}
		return true;
	}

	public String get_currency_name() {
		return iso.get_currency_name(currency_idx);
	}

	public String get_currency_code() {
		return iso.get_currency_code(currency_idx);
	}

	public static String get_text_denomination(int curr_idx, int expo, int multi) {
		return get_text_denomination(curr_idx, expo, multi, true);
	}

	public static String get_text_denomination(int curr_idx, int expo,
			int multi, boolean with_code) {
		if (!is_valid_currency(curr_idx)) {
			throw new bad_passet(2);
		}
		if (!is_valid_expo(expo)) {
			throw new bad_passet(2);
		}
		if (!is_valid_multi(multi)) {
			throw new bad_passet(2);
		}

		String nm = "";
		if (with_code) {
			nm = iso.get_currency_name(curr_idx);
		}

		if (multi == 5) {
			if (expo > 0) {
				return pos_expo_txt_amounts_mult_five[expo - 1] + nm;
			}
			if (expo < 0) {
				return neg_expo_txt_amounts_mult_five[(-expo) - 1] + nm;
			}
			return zero_expo_txt_amount_mult_five + nm;
		}

		if (multi == 2) {
			if (expo > 0) {
				return pos_expo_txt_amounts_mult_two[expo - 1] + nm;
			}
			if (expo < 0) {
				return neg_expo_txt_amounts_mult_two[(-expo) - 1] + nm;
			}
			return zero_expo_txt_amount_mult_two + nm;
		}

		if (expo > 0) {
			return pos_expo_txt_amounts[expo - 1] + nm;
		}
		if (expo < 0) {
			return neg_expo_txt_amounts[(-expo) - 1] + nm;
		}
		return zero_expo_txt_amount + nm;
	}

	public String get_text_denomination(boolean with_code) {
		return get_text_denomination(currency_idx, ten_exponent, multiplier,
				with_code);
	}

	public String get_text_denomination() {
		return get_text_denomination(currency_idx, ten_exponent, multiplier);
	}

	public static String get_number_denomination(int curr_idx, int expo,
			int multi) {
		return get_number_denomination(curr_idx, expo, multi, true);
	}

	public static String get_number_denomination(int curr_idx, int expo,
			int multi, boolean with_code) {
		if (!is_valid_currency(curr_idx)) {
			throw new bad_passet(2);
		}
		if (!is_valid_expo(expo)) {
			throw new bad_passet(2);
		}
		if (!is_valid_multi(multi)) {
			throw new bad_passet(2);
		}

		String num = "" + multi;
		String deno = "";
		if (with_code) {
			deno = iso.get_currency_code(curr_idx);
		}

		if (expo > 0) {
			return num + pos_expo_amounts[expo - 1] + deno;
		}
		if (expo < 0) {
			return num + neg_expo_amounts[(-expo) - 1] + deno;
		}
		return num + " " + deno;
	}

	public String get_number_denomination(boolean with_code) {
		return get_number_denomination(currency_idx, ten_exponent, multiplier,
				with_code);
	}

	public String get_number_denomination() {
		return get_number_denomination(currency_idx, ten_exponent, multiplier,
				true);
	}

	public static boolean is_valid_currency(int curr_idx) {
		if (curr_idx < 0) {
			return false;
		}
		if (curr_idx >= iso.currencies_array.length) {
			return false;
		}
		return true;
	}

	public static boolean is_valid_expo(int expo) {
		if (expo > MAX_EXPO) {
			return false;
		}
		if (expo < MIN_EXPO) {
			return false;
		}
		return true;
	}

	public static boolean is_valid_multi(int multi) {
		if (multi == 1) {
			return true;
		}
		if (multi == 2) {
			return true;
		}
		if (multi == 5) {
			return true;
		}
		return false;
	}

	List<String> get_denomination_lines(String title) {
		List<String> txt = new ArrayList<String>();
		add_denomination_lines_to(txt, title);
		return txt;
	}

	void add_denomination_lines_to(List<String> txt, String title) {

		String amount_txt = get_text_denomination(currency_idx, ten_exponent,
				multiplier);
		String amount_str = get_number_denomination(currency_idx, ten_exponent,
				multiplier);

		// parse.add_next_title_to(txt, title);
		parse.add_next_field_to(txt, title, amount_number_denomination_field,
				amount_str);
		parse.add_next_field_to(txt, title, amount_text_denomination_field,
				amount_txt);

		parse.add_next_field_to(txt, title,
				amount_denomination_multiplier_field, "" + multiplier);
		parse.add_next_field_to(txt, title, amount_denomination_exponent_field,
				"" + ten_exponent);
		parse.add_next_field_to(txt, title, amount_denomination_idx_field, ""
				+ currency_idx);

		parse.check_line_list(txt);
	}

	public void init_denomination_with(List<String> all_lines, String title) {
		parse.check_line_list(all_lines);

		ListIterator<String> it1 = all_lines.listIterator();
		init_denomination_with(it1, title);
	}

	public void init_denomination_with(ListIterator<String> it1, String title) {
		// parse.get_next_title_from(it1, title);
		String amount_str = parse.get_next_field_from(it1, title,
				amount_number_denomination_field);
		String amount_txt = parse.get_next_field_from(it1, title,
				amount_text_denomination_field);

		multiplier = convert.parse_int(parse.get_next_field_from(it1, title,
				amount_denomination_multiplier_field));
		ten_exponent = convert.parse_int(parse.get_next_field_from(it1, title,
				amount_denomination_exponent_field));
		currency_idx = convert.parse_int(parse.get_next_field_from(it1, title,
				amount_denomination_idx_field));

		if (!is_valid_deno()) {
			throw new bad_passet(2);
		}

		String calc_amount_txt = get_text_denomination(currency_idx,
				ten_exponent, multiplier);
		String calc_amount_str = get_number_denomination(currency_idx,
				ten_exponent, multiplier);

		if (!amount_txt.equals(calc_amount_txt)) {
			throw new bad_passet(2);
		}
		if (!amount_str.equals(calc_amount_str)) {
			throw new bad_passet(2);
		}
	}

	public int compare(tag_denomination t1, tag_denomination t2) {
		if (t1 == null) {
			throw new bad_passet(2);
		}
		if (t2 == null) {
			throw new bad_passet(2);
		}

		int cc = convert.cmp_int(t1.currency_idx, t2.currency_idx);
		if (cc != 0) {
			return cc;
		}
		cc = convert.cmp_int(t1.ten_exponent, t2.ten_exponent);
		if (cc != 0) {
			return cc;
		}
		cc = convert.cmp_int(t1.multiplier, t2.multiplier);
		return cc;
	}

	public int compareTo(tag_denomination t2) {
		return compare(this, t2);
	}

	public static String get_short_text_denomination(int curr_idx, int expo,
			int multi) {
		if (!is_valid_expo(expo)) {
			throw new bad_passet(2);
		}
		if (!is_valid_multi(multi)) {
			throw new bad_passet(2);
		}

		String num = "" + multi;
		if (expo != 0) {
			if (expo > 0) {
				num = num + 'z' + expo;
			} else {
				num = num + 'd' + (-expo);
			}
		}
		if (is_valid_currency(curr_idx)) {
			String deno = iso.get_currency_code(curr_idx);
			num = num + " " + deno;
		}

		return num;
	}

	public String get_short_text_denomination(boolean with_code) {
		int idx = -1;
		if (with_code) {
			idx = currency_idx;
		}
		return get_short_text_denomination(idx, ten_exponent, multiplier);
	}

	public static tag_denomination parse_short_text_denomination(String txt) {
		return parse_short_text_denomination(txt, -1);
	}

	public static tag_denomination parse_short_text_denomination(String txt,
			int currcy_idx) {
		tag_denomination deno = new tag_denomination();
		if (is_valid_currency(currcy_idx)) {
			deno.currency_idx = currcy_idx;
		}
		boolean is_neg = false;
		int mult = 1;
		int expo = 0;
		int sep_idx = txt.indexOf('z');
		if (sep_idx == -1) {
			sep_idx = txt.indexOf('d');
			if (sep_idx != -1) {
				is_neg = true;
			}
		}

		if (sep_idx == -1) {
			sep_idx = txt.length();
		} else {
			expo = 1;
		}
		if (sep_idx > 0) {
			mult = Integer.parseInt(txt.substring(0, sep_idx));
			if (!is_valid_multi(mult)) {
				throw new bad_passet(2, L.invalid_multiple_of_deno);
			}
		}
		int e_idx = sep_idx + 1;
		if (e_idx < txt.length()) {
			expo = Integer.parseInt(txt.substring(e_idx));
			if (!is_valid_expo(expo)) {
				throw new bad_passet(2, String.format(
						L.invalid_exponent_of_deno, MIN_EXPO, MAX_EXPO));
			}
		}
		if (is_neg) {
			expo = -expo;
		}

		deno.multiplier = mult;
		deno.ten_exponent = expo;

		return deno;
	}

	private static boolean split_deno(Stack<tag_denomination> s_denos,
			int min_expo) {
		if (s_denos.isEmpty()) {
			return false;
		}
		tag_denomination deno = s_denos.pop();
		if (deno.ten_exponent <= min_expo) {
			s_denos.push(new tag_denomination(deno));
			return false;
		}

		int n_mult = -1;
		int cu_idx = deno.currency_idx;
		int n_expo = deno.ten_exponent;
		int mult = deno.multiplier;
		switch (mult) {
		case 1:
			n_expo--;
			n_mult = 5;
			s_denos.push(new tag_denomination(cu_idx, n_expo, n_mult));
			s_denos.push(new tag_denomination(cu_idx, n_expo, n_mult));
			break;
		case 2:
			n_mult = 1;
			s_denos.push(new tag_denomination(cu_idx, n_expo, n_mult));
			s_denos.push(new tag_denomination(cu_idx, n_expo, n_mult));
			break;
		case 5:
			n_mult = 2;
			s_denos.push(new tag_denomination(cu_idx, n_expo, n_mult));
			s_denos.push(new tag_denomination(cu_idx, n_expo, n_mult));
			n_mult = 1;
			s_denos.push(new tag_denomination(cu_idx, n_expo, n_mult));
			break;
		}
		return true;
	}

	public static void rec_split_deno(Stack<tag_denomination> s_denos,
			int min_expo) {
		while (split_deno(s_denos, min_expo)) {
		}
	}

	private static deno_counter join_denos(deno_counter ord_deno,
			boolean mv_elems) {
		int currcy_idx = ord_deno.currency_idx;

		tag_denomination curr_dd = tag_denomination.first_deno(currcy_idx);
		while (true) {
			deno_count cc = ord_deno.get_deno_count(curr_dd);

			join_deno_list(cc, ord_deno, mv_elems);

			if ((curr_dd.multiplier == 1) && (curr_dd.ten_exponent == MAX_EXPO)) {
				break;
			}
			curr_dd.inc_deno();
		}

		ord_deno.clear_zeros();
		return ord_deno;
	}

	static deno_counter calc_join_transfer_denos(
			Collection<tag_transfer> all_dat) {
		deno_counter ord_deno = new deno_counter();
		ord_deno.init_with_transfers(all_dat);
		return join_denos(ord_deno, true);
	}

	static deno_counter calc_join_denos(Collection<tag_denomination> all_den) {
		deno_counter ord_deno = new deno_counter();
		ord_deno.init_with_denos(all_den);
		return join_denos(ord_deno, false);
	}

	public static tag_denomination first_deno(int currcy_idx) {
		tag_denomination dd = new tag_denomination(currcy_idx, MIN_EXPO, 1);
		return dd;
	}

	public void inc_deno() {
		int multi = multiplier;
		int expo = ten_exponent;
		int n_multi = 0;
		if (multi == 1) {
			n_multi = 2;
		}
		if (multi == 2) {
			n_multi = 5;
		}
		if (multi == 5) {
			n_multi = 1;
			expo++;
		}
		multiplier = n_multi;
		ten_exponent = expo;
	}

	public boolean is_last_deno() {
		return ((multiplier == 5) && (ten_exponent == MAX_EXPO));
	}

	private static void add_denos_to_num_have(deno_counter cntr,
			Collection<tag_denomination> all_dat) {
		for (tag_denomination deno : all_dat) {
			if (deno.currency_idx != cntr.currency_idx) {
				throw new bad_passet(2);
			}
			deno_count deco1 = cntr.get_deno_count(deno, true);
			deco1.num_have++;
		}
	}

	static deno_counter count_num_have_denos(
			Collection<tag_denomination> from_iss, int currcy_idx) {
		deno_counter cnt1 = new deno_counter();
		cnt1.currency_idx = currcy_idx;
		add_denos_to_num_have(cnt1, from_iss);
		return cnt1;
	}

	static deno_counter split_transfer(tag_transfer iss_dat, int min_expo) {
		if (iss_dat == null) {
			throw new bad_passet(2);
		}
		return split_deno(iss_dat.get_out_amount(), min_expo);
	}

	private static deno_counter split_deno(tag_denomination den, int min_expo) {
		if (den == null) {
			throw new bad_passet(2);
		}
		boolean is_rec = false;
		if (!is_valid_expo(min_expo)) {
			is_rec = true;
		}

		int currcy_idx = den.currency_idx;
		if (!iso.is_valid_currency_idx(currcy_idx)) {
			throw new bad_passet(2);

		}

		Stack<tag_denomination> from_iss = new Stack<tag_denomination>();
		from_iss.push(den);

		if (is_rec) {
			tag_denomination.rec_split_deno(from_iss, min_expo);
		} else {
			tag_denomination.split_deno(from_iss, min_expo);
		}

		deno_counter cnt1 = count_num_have_denos(from_iss, currcy_idx);
		return cnt1;
	}

	static deno_counter calc_split_transfer_denos(
			Collection<tag_transfer> all_dat, int min_expo) {
		List<tag_denomination> all_den = tag_transfer.get_denos(all_dat);
		return split_denos(all_den, min_expo);
	}

	static deno_counter split_denos(Collection<tag_denomination> all_den,
			int min_expo) {
		deno_counter cnt_tot = null;

		if (all_den.isEmpty()) {
			return cnt_tot;
		}

		for (tag_denomination den1 : all_den) {
			deno_counter cnt1 = split_deno(den1, min_expo);
			if (cnt_tot == null) {
				cnt_tot = new deno_counter();
				cnt_tot.reset_deno_counts(cnt1.currency_idx);
			}
			cnt_tot.add_counter(cnt1);
		}
		return cnt_tot;
	}

	public BigDecimal get_decimal() {
		return new BigDecimal(BigInteger.valueOf(multiplier), -ten_exponent);
	}

	private static void move_elems(List<Object> src1, int src1_num,
			List<Object> src2, int src2_num, List<Object> dst_ll, int dst_num) {

		for (int aa = 0; aa < dst_num; aa++) {
			List<Object> elem = new ArrayList<Object>();
			if (src1.size() < src1_num) {
				throw new bad_passet(2);
			}
			for (int bb = 0; bb < src1_num; bb++) {
				elem.add(src1.remove(0));
			}
			if (src2 != null) {
				if (src2.size() < src2_num) {
					throw new bad_passet(2);
				}
				for (int cc = 0; cc < src2_num; cc++) {
					elem.add(src2.remove(0));
				}
			}
			dst_ll.add(elem);
		}
	}

	private static void rec_get_all_transfer(List<Object> src1, List<tag_transfer> all_tra){
		for(Object oo : src1){
			if(oo instanceof tag_transfer){
				tag_transfer dd = (tag_transfer)oo;
				all_tra.add(dd);
				continue;
			}
			@SuppressWarnings("unchecked")
			List<Object> nn = (List<Object>)oo;
			rec_get_all_transfer(nn, all_tra);
		}
	}
	
	static List<tag_transfer> get_all_transfer(List<Object> src1){
		List<tag_transfer> all_den = new ArrayList<tag_transfer>();
		rec_get_all_transfer(src1, all_den);
		return all_den;
	}
			
	private static void join_deno_list(deno_count curr_cnt,
			deno_counter ord_deno, boolean mv_elems) {
		if (curr_cnt == null) {
			return;
		}

		int cu_idx = curr_cnt.deno.currency_idx;
		int expo = curr_cnt.deno.ten_exponent;
		int mult = curr_cnt.deno.multiplier;

		int div_num = 0;
		int mod_num = 0;

		int num1 = 0;
		int num2 = 0;
		int num5 = 0;
		deno_count cc_aux1 = null;
		deno_count cc_aux2 = null;
		deno_count cc_aux5 = null;

		switch (mult) {
		case 1:
			cc_aux1 = curr_cnt;
			num1 = cc_aux1.num_have;
			div_num = num1 / 2;
			mod_num = num1 % 2;

			cc_aux2 = ord_deno.get_deno_count(new tag_denomination(cu_idx,
					expo, 2));
			cc_aux2.num_have += div_num;
			cc_aux1.num_have = mod_num;
			if (mv_elems) {
				move_elems(cc_aux1.joined_denos, 2, null, 0, cc_aux2.joined_denos, div_num);
			}

			num2 = cc_aux2.num_have;
			num1 = mod_num;
			num5 = num2 / 2;
			if (num1 < num5) {
				num5 = num1;
			}

			if (num5 > 0) {
				cc_aux5 = ord_deno.get_deno_count(new tag_denomination(cu_idx,
						expo, 5));

				cc_aux5.num_have += num5;
				cc_aux2.num_have = num2 - (num5 * 2);
				cc_aux1.num_have = num1 - num5;
				if (mv_elems) {
					move_elems(cc_aux2.joined_denos, 2, cc_aux1.joined_denos, 1, cc_aux5.joined_denos, num5);
				}
			}
			break;
		case 2:
			cc_aux2 = curr_cnt;
			num2 = cc_aux2.num_have;
			div_num = num2 / 5;
			mod_num = num2 % 5;
			cc_aux1 = ord_deno.get_deno_count(new tag_denomination(cu_idx,
					expo + 1, 1));
			cc_aux1.num_have += div_num;
			cc_aux2.num_have = mod_num;
			if (mv_elems) {
				move_elems(cc_aux2.joined_denos, 5, null, 0, cc_aux1.joined_denos, div_num);
			}

			num2 = cc_aux2.num_have;
			num5 = (num2 * 2) / 5;
			int res5 = ((num2 * 2) % 5);
			num1 = res5 % 2;
			num2 = res5 / 2;

			if (num5 > 0) {
				cc_aux1 = ord_deno.get_deno_count(new tag_denomination(cu_idx,
						expo, 1));
				cc_aux5 = ord_deno.get_deno_count(new tag_denomination(cu_idx,
						expo, 5));

				cc_aux5.num_have += num5;
				cc_aux2.num_have = num2;
				cc_aux1.num_have += num1;
				if (mv_elems) {
					move_elems(cc_aux2.joined_denos, 2, cc_aux1.joined_denos, 1, cc_aux5.joined_denos, num5);
				}
			}
			break;
		case 5:
			cc_aux5 = curr_cnt;
			num5 = cc_aux5.num_have;
			div_num = num5 / 2;
			mod_num = num5 % 2;
			cc_aux1 = ord_deno.get_deno_count(new tag_denomination(cu_idx,
					expo + 1, 1));
			cc_aux1.num_have += div_num;
			cc_aux5.num_have = mod_num;
			if (mv_elems) {
				move_elems(cc_aux5.joined_denos, 2, null, 0, cc_aux1.joined_denos, div_num);
			}
			break;
		}
	}

}
