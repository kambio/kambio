package emetcode.net.netmix;

import emetcode.crypto.bitshake.utils.convert;

public class nx_conn_id {
		
	private long the_id;
	
	public nx_conn_id(){
		the_id = System.currentTimeMillis();
	}
	
	public nx_conn_id(long val){
		the_id = val;
	}
	
	public nx_conn_id(byte[] val){
		the_id = convert.to_long(val);
	}
	
	public long as_long(){
		return the_id;
	}
	
	public String toString(){
		return "" + the_id;
	}
	
	String get_str(){
		return "" + the_id;
	}
	
	public byte[] to_bytes(){
		return convert.to_byte_array(the_id);
	}
	
	public boolean equals(Object oo){
		return (((nx_conn_id)oo).the_id == the_id);
	}
}
