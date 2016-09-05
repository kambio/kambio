package emetcode.net.netmix.locale;

public class L {
	
	public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

	public static String i2p_local_peer_is_null = UNKNOWN_ERROR;
	public static String i2p_remote_peer_is_null = UNKNOWN_ERROR;
	public static String cannot_get_i2p_input_stream = UNKNOWN_ERROR;
	public static String cannot_get_i2p_output_stream = UNKNOWN_ERROR;
	public static String invalid_i2p_id_file_or_descrip_file = UNKNOWN_ERROR;
	public static String invalid_i2p_local_peer = UNKNOWN_ERROR;
	public static String invalid_i2p_remote_peer = UNKNOWN_ERROR;
	public static String cannot_get_i2p_server_socket = UNKNOWN_ERROR;
	public static String cannot_get_i2p_session = UNKNOWN_ERROR;
	public static String cannot_get_i2p_destination = UNKNOWN_ERROR;
	public static String cannot_get_i2p_description = UNKNOWN_ERROR;
	public static String cannot_read_i2p_null_id_file = UNKNOWN_ERROR;
	public static String cannot_read_i2p_peer_id_file = UNKNOWN_ERROR;
	public static String cannot_decrypt_i2p_peer_id_file = UNKNOWN_ERROR;
	public static String cannot_decrypt_i2p_peer_id_file_bad_key = UNKNOWN_ERROR;
	public static String cannot_write_i2p_id_file_no_session = UNKNOWN_ERROR;
	public static String cannot_write_i2p_id_file_no_descr = UNKNOWN_ERROR;
	public static String cannot_write_i2p_null_id_file = UNKNOWN_ERROR;
	public static String cannot_write_i2p_temp_id = UNKNOWN_ERROR;
	public static String cannot_read_i2p_temp_id = UNKNOWN_ERROR;
	public static String cannot_delete_i2p_temp_id = UNKNOWN_ERROR;
	public static String cannot_encrypt_i2p_peer_id_file = UNKNOWN_ERROR;
	public static String cannot_write_i2p_null_descr_file = UNKNOWN_ERROR;
	public static String cannot_write_i2p_descr_file = UNKNOWN_ERROR;
	public static String cannot_set_i2p_dest = UNKNOWN_ERROR;
	public static String cannot_connect_to_same_i2p_peer = UNKNOWN_ERROR;
	public static String null_i2p_server_socket = UNKNOWN_ERROR;
	public static String cannot_close_i2p_server_socket = UNKNOWN_ERROR;
	public static String invalid_exact_bytes_bad_len = UNKNOWN_ERROR;
	public static String invalid_exact_bytes = UNKNOWN_ERROR;
	public static String cannot_recv_int_str = UNKNOWN_ERROR;
	public static String cannot_recv_bytes = UNKNOWN_ERROR;
	public static String bad_msg_szs = UNKNOWN_ERROR;
	public static String cannot_send_bytes = UNKNOWN_ERROR;
	public static String cannot_secure_conn = UNKNOWN_ERROR;
	public static String cannot_get_new_cokey = UNKNOWN_ERROR;
	public static String cannot_secure_conn_bad_oper = UNKNOWN_ERROR;
	public static String cannot_secure_conn_bad_cokk = UNKNOWN_ERROR;
	public static String cannot_secure_conn_bad_coid = UNKNOWN_ERROR;
	public static String cannot_read_coid_file = UNKNOWN_ERROR;
	public static String corrupt_coid_file = UNKNOWN_ERROR;
	public static String no_such_old_coid_file = UNKNOWN_ERROR;
	public static String cannot_decrypt_msg = UNKNOWN_ERROR;
	public static String already_existing_coid_file = UNKNOWN_ERROR;
	public static String cannot_recv_mem_files = UNKNOWN_ERROR;
	public static String no_owner = UNKNOWN_ERROR;
	public static String invalid_tcp_local_peer = UNKNOWN_ERROR;
	public static String invalid_tcp_remote_peer = UNKNOWN_ERROR;
	public static String cannot_connect_to_same_tcp_peer = UNKNOWN_ERROR;
	public static String null_tcp_server_socket = UNKNOWN_ERROR;
	public static String cannot_close_tcp_server_socket = UNKNOWN_ERROR;
	public static String tcp_local_peer_is_null = UNKNOWN_ERROR;
	public static String tcp_remote_peer_is_null = UNKNOWN_ERROR;
	public static String cannot_get_tcp_input_stream = UNKNOWN_ERROR;
	public static String cannot_get_tcp_output_stream = UNKNOWN_ERROR;
	public static String mudp_local_peer_is_null = UNKNOWN_ERROR;
	public static String mudp_remote_peer_is_null = UNKNOWN_ERROR;
	public static String invalid_mudp_local_peer = UNKNOWN_ERROR;
	public static String invalid_mudp_remote_peer = UNKNOWN_ERROR;
	public static String cannot_connect_to_same_mudp_peer = UNKNOWN_ERROR;
	public static String null_mudp_server_socket = UNKNOWN_ERROR;
	public static String could_secure_connection_with = UNKNOWN_ERROR;
	public static String coname_reported_and_original_differ = UNKNOWN_ERROR;
	public static String no_locators_found = UNKNOWN_ERROR;
	public static String bad_message_name = UNKNOWN_ERROR;
	public static String cannot_find_gammal_sys = UNKNOWN_ERROR;
	public static String add_supra_locator_first = UNKNOWN_ERROR;
	
	static{
		str_en.set();
	}
	
	public static void set_lang(String two_letter_code){
		if(two_letter_code == null){ return; }
		String cod = two_letter_code.toLowerCase();
		if(cod.equals("en")){
			str_en.set();
			return;
		}
		else if(cod.equals("es")){
			str_es.set();
			return;
		}
	}
	
}
