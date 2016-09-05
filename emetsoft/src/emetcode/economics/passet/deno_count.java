package emetcode.economics.passet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import emetcode.util.devel.logger;

public class deno_count {
	static final boolean IN_DEBUG_1 = true; // equals

	public static final char FLD_SEP = ':';
	public static final Pattern FLD_SEP_PATT = Pattern.compile("" + FLD_SEP);

	public static final int MIN_EXPO = tag_denomination.MIN_EXPO;
	public static final int MAX_EXPO = tag_denomination.MAX_EXPO;

	public tag_denomination deno;
	public int num_have;
	public int num_can_give;
	public int num_chosen;
	public List<Object> joined_denos;  // used for joining
	public Object cnt_data;		// used in GUI

	public deno_count() {
		init_deno_count();
	}

	public deno_count(deno_count orig) {
		init_deno_count(orig);
	}

	void init_deno_count(deno_count orig) {
		init_deno_count();
		deno = new tag_denomination(orig.deno);
		num_have = orig.num_have;
		num_can_give = orig.num_can_give;
		num_chosen = orig.num_chosen;
	}

	private void init_deno_count() {
		deno = new tag_denomination();
		num_have = 0;
		num_can_give = 0;
		num_chosen = 0;
		joined_denos = new ArrayList<Object>();
		cnt_data = null;
	}

	public boolean is_zero() {
		if ((num_have == 0) && (num_can_give == 0) && (num_chosen == 0)) {
			return true;
		}
		return false;
	}

	public void add_count(deno_count cc_2) {
		num_have += cc_2.num_have;
		num_can_give += cc_2.num_can_give;
		num_chosen += cc_2.num_chosen;
	}

	public String as_text_deno_count() {
		String the_ln = deno.get_short_text_denomination(false) + FLD_SEP
				+ num_have + FLD_SEP + num_can_give + FLD_SEP + num_chosen;
		return the_ln;
	}

	public static deno_count parse_text_deno_count(String txt, int currcy_idx) {
		deno_count cnt = new deno_count();
		Scanner s1 = new Scanner(txt);
		s1.useDelimiter(FLD_SEP_PATT);

		String val = s1.next();
		String hav = s1.next();
		String giv = s1.next();
		String cho = s1.next();

		cnt.deno = tag_denomination.parse_short_text_denomination(val,
				currcy_idx);
		cnt.num_have = Integer.parseInt(hav);
		cnt.num_can_give = Integer.parseInt(giv);
		cnt.num_chosen = Integer.parseInt(cho);

		return cnt;
	}

	public String get_short_str() {
		String val = deno.get_short_text_denomination(true) + ".h." + num_have + 
				".g." + num_can_give + ".c." + num_chosen;
		return val;
	}
	
	public String get_str() {
		String val = deno.get_number_denomination();
		String hav = "" + num_have;
		String giv = "" + num_can_give;
		String cho = "" + num_chosen;
		String dis = String.format(
				"for %35s %10s selected %10s can give %10s have", val, cho,
				giv, hav);
		return dis;
	}
	
	public String toString() {
		return get_str();
	}

	public BigDecimal get_chosen_decimal() {
		BigDecimal num_cho = new BigDecimal(new BigInteger("" + num_chosen), 0);
		BigDecimal deno_num = deno.get_decimal();
		BigDecimal val_cho = deno_num.multiply(num_cho);
		return val_cho;
	}
	
	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}
		if(! (obj instanceof deno_count)){
			throw new bad_passet(2);
		}
		deno_count dd2 = (deno_count)obj;
		boolean c1 = deno.equals(dd2.deno);
		boolean c2 = (num_have == dd2.num_have);
		boolean c3 = (num_can_give == dd2.num_can_give);
		boolean c4 = (num_chosen == dd2.num_chosen);
		
		boolean eq_cc = (c1 && c2 && c3 && c4);

		if(IN_DEBUG_1){
			if(eq_cc){
				//String stk = logger.get_stack_str();
				logger.info("COUNTS." + get_short_str() + " == " + dd2.get_short_str());
			}
		}

		return eq_cc;
	}
}
