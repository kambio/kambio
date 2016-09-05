package emetcode.net.netmix.locator_sys;

import java.nio.ByteBuffer;
import java.util.Arrays;

import emetcode.crypto.bitshake.utils.data_packer;
import emetcode.net.netmix.bad_netmix;

public class nx_location_request {
	static final String IS_LOCAL_GLID = "is_local_glid";
	
	static final int MAX_DATA_NUM_BY = 900;

	static final int MAX_STR_SZ = 100;

	static final byte ASK_LOCATE_OPER = 11;
	static final byte ASK_SERVICE_OPER = 12;
	static final byte ASK_COID_OPER = 13;
	static final byte ASK_SET_COID_OPER = 14;
	static final byte ASK_INVALID_OPER = 15;

	static final byte ANSW_ALL_OK = 31;
	static final byte ANSW_COID_WITHOUT_GLID = 32;
	static final byte ANSW_GLID_NOT_FOUND = 33;
	static final byte ANSW_BAD_GLID = 34;
	static final byte ANSW_BAD_CHECK_GLID = 35;
	static final byte ANSW_UNTRUSTED_REQUEST = 36;
	static final byte ANSW_BAD_REQ_GLID = 37;
	static final byte ANSW_BAD_REQ_COID = 38;
	static final byte ANSW_NO_BOSS = 39;
	static final byte ANSW_BAD_FWD_ADDR = 40;
	static final byte ANSW_BUSY_SERVER = 41;
	static final byte ANSW_NOT_LOCAL_COREF = 42;
	static final byte ANSW_INVALID_OPER = 43;
	
	
	byte msg_code;
	long msg_tm;

	String req_coref_str;
	String src_reported_nx_addr;

	String src_observed_nx_addr;
	String dest_reported_nx_addr;
	String dest_observed_nx_addr;

	String glid_check;
	String forward_nx_addr;
	
	String req_service;
	
	long coid_resp;
	
	public nx_location_request() {
		init_nx_location_request();
	}

	void init_nx_location_request() {
		msg_code = 0;
		
		req_coref_str = null;
		src_reported_nx_addr = null;

		msg_tm = 0;
		src_observed_nx_addr = null;
		dest_reported_nx_addr = null;
		dest_observed_nx_addr = null;
		
		glid_check = null;
		forward_nx_addr = null;
		
		req_service = null;
		
		coid_resp = 0;
	}
	
	private boolean is_ask_msg(){
		boolean aa_ok = (msg_code >= ASK_LOCATE_OPER) && (msg_code < ASK_INVALID_OPER);
		return aa_ok;
	}

	private boolean is_answ_msg(){
		boolean aa_ok = (msg_code >= ANSW_ALL_OK) && (msg_code < ANSW_INVALID_OPER);
		return aa_ok;
	}

	private static void put_str(ByteBuffer dat, String the_str) {
		data_packer.put_str(dat, the_str);
	}

	private static String get_str(ByteBuffer dat) {
		return data_packer.get_str(dat);
	}

	byte[] get_ask_msg() {
		byte[] buff = new byte[MAX_DATA_NUM_BY];
		ByteBuffer dat = ByteBuffer.wrap(buff);
		
		if(!is_ask_msg()){
			throw new bad_netmix(2);
		}

		dat.put(msg_code);
		msg_tm = System.currentTimeMillis();
		dat.putLong(msg_tm);
		switch(msg_code){
		case ASK_LOCATE_OPER:
			put_str(dat, req_coref_str);
			put_str(dat, src_reported_nx_addr);
			break;
		case ASK_SERVICE_OPER:
			put_str(dat, req_service);
			put_str(dat, req_coref_str);
			put_str(dat, src_reported_nx_addr);
			break;
		case ASK_COID_OPER:
			put_str(dat, req_coref_str);
			put_str(dat, forward_nx_addr);
			put_str(dat, glid_check);
			break;
		case ASK_SET_COID_OPER:
			put_str(dat, req_coref_str);
			dat.putLong(coid_resp);
			break;
		}

		byte[] msg = Arrays.copyOf(buff, dat.position());
		return msg;
	}

	void set_ask_msg(byte[] msg) {
		ByteBuffer dat = ByteBuffer.wrap(msg);
		
		msg_code = dat.get();
		msg_tm = dat.getLong();
		switch(msg_code){
		case ASK_LOCATE_OPER:
			req_coref_str = get_str(dat);
			src_reported_nx_addr = get_str(dat);
			break;
		case ASK_SERVICE_OPER:
			req_service = get_str(dat);
			req_coref_str = get_str(dat);
			src_reported_nx_addr = get_str(dat);
			break;
		case ASK_COID_OPER:
			req_coref_str = get_str(dat);
			forward_nx_addr = get_str(dat);
			glid_check = get_str(dat);
			break;
		case ASK_SET_COID_OPER:
			req_coref_str = get_str(dat);
			coid_resp = dat.getLong();
			break;
		}

		if(!is_ask_msg()){
			throw new bad_netmix(2);
		}
	}

	byte[] get_answ_msg() {
		byte[] buff = new byte[MAX_DATA_NUM_BY];
		ByteBuffer dat = ByteBuffer.wrap(buff);

		if(!is_answ_msg()){
			throw new bad_netmix(2);
		}
		
		dat.put(msg_code);
		msg_tm = System.currentTimeMillis();
		dat.putLong(msg_tm);
		put_str(dat, req_coref_str);
		put_str(dat, src_observed_nx_addr);
		put_str(dat, dest_reported_nx_addr);
		put_str(dat, dest_observed_nx_addr);
		dat.putLong(coid_resp);

		byte[] msg = Arrays.copyOf(buff, dat.position());
		return msg;
	}

	void set_answ_msg(byte[] msg) {
		ByteBuffer dat = ByteBuffer.wrap(msg);

		msg_code = dat.get();
		msg_tm = dat.getLong();
		req_coref_str = get_str(dat);
		src_observed_nx_addr = get_str(dat);
		dest_reported_nx_addr = get_str(dat);
		dest_observed_nx_addr = get_str(dat);
		coid_resp = dat.getLong();

		if(!is_answ_msg()){
			throw new bad_netmix(2);
		}
	}

	public String toString() {
		String str = "req_glid='" + req_coref_str + "'\n"
				+ "src_reported_nx_addr='" + src_reported_nx_addr + "'\n"
				+ "info_tm='" + msg_tm + "'\n" + "src_observed_nx_addr='"
				+ src_observed_nx_addr + "'\n" + "dest_reported_nx_addr='"
				+ dest_reported_nx_addr + "'\n" + "dest_observed_nx_addr='"
				+ dest_observed_nx_addr + "'\n";
		return str;
	}

	public boolean is_target() {
		if(dest_observed_nx_addr == null){
			return false;
		}
		return dest_observed_nx_addr.equals(IS_LOCAL_GLID);
	}

	public void set_as_target() {
		dest_observed_nx_addr = IS_LOCAL_GLID;
		dest_reported_nx_addr = IS_LOCAL_GLID;
	}
	
}
