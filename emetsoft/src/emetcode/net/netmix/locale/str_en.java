package emetcode.net.netmix.locale;

public class str_en extends L {
	public static void set() {
		i2p_local_peer_is_null = "Network error. I2P local_peer is null.";
		i2p_remote_peer_is_null = "Network error. I2P remote_peer is null.";
		cannot_get_i2p_input_stream = "Network error. cannot get input stream from I2P connection.";
		cannot_get_i2p_output_stream = "Network error. cannot get output stream from I2P connection.";
		invalid_i2p_id_file_or_descrip_file = "Network error. Invalid I2P id file or descrip file '%s'";
		invalid_i2p_local_peer = "Network error. Invalid I2P local peer.";
		invalid_i2p_remote_peer = "Network error. Invalid I2P remote peer.";
		cannot_get_i2p_server_socket = "Network error. Cannot get I2P server_socket from manager.";
		cannot_get_i2p_session = "Network error. Cannot get session from I2P router. Is it running?.";
		cannot_get_i2p_destination = "Network error. Cannot get destination from I2P session.";
		cannot_get_i2p_description = "Network error. Cannot get description from I2P destination.";
		cannot_read_i2p_null_id_file = "Network error. Cannot read I2P peer id file. Null file.";
		cannot_read_i2p_peer_id_file = "Network error. Cannot read I2P peer id file '%s'";
		cannot_decrypt_i2p_peer_id_file = "Network error. Cannot decrypt I2P peer id file '%s'. Null or empty key.";
		cannot_decrypt_i2p_peer_id_file_bad_key = 
				"Network error. Cannot decrypt I2P peer id file '%s'. Bad key?. Try again.";
		cannot_write_i2p_id_file_no_session = "Network error. Cannot write I2P id_file '%s'. No session started.";
		cannot_write_i2p_id_file_no_descr = "Network error. Cannot write I2P id_file '%s'. No description inited.";
		cannot_write_i2p_null_id_file = "Network error. Cannot write I2P null id_file";
		cannot_write_i2p_temp_id = "Network error. Cannot write I2P temp id to '%s'.";
		cannot_read_i2p_temp_id = "Network error. Cannot read I2P temp id to '%s'.";
		cannot_delete_i2p_temp_id = "Network error. Cannot delete I2P temp id to '%s'.";
		cannot_encrypt_i2p_peer_id_file = "Network error. Cannot encrypt I2P peer id file '%s'. Internal error.";
		cannot_write_i2p_null_descr_file = "Network error. Cannot write I2P description file. Null description.";
		cannot_write_i2p_descr_file = "Network error. Cannot write I2P description file '%s'.";
		cannot_set_i2p_dest = "Network error. Cannot set I2P destination '%s'. Creation failed.";
		cannot_connect_to_same_i2p_peer = "Network error. Cannot connect to same I2P peer '%s'.";
		null_i2p_server_socket = "Network error. I2P srv_socket is null";
		cannot_close_i2p_server_socket = "Network error. cannot close I2P srv_socket";
		invalid_exact_bytes_bad_len = "Network error. Invalid exact bytes (bad length). d_sz=%s rcv_arr.length=%s";
		invalid_exact_bytes = "Network error. Invalid exact bytes (not the same).";
		cannot_recv_int_str = "Network error. Cannot recv integer string.";
		cannot_recv_bytes = "Network error. Cannot recv bytes.";
		bad_msg_szs = "Network error. Invalid message. Bad msg sizes (%s >= %s)";
		cannot_send_bytes = "Network error. Cannot send bytes.";
		cannot_secure_conn = "Network error. Cannot secure connection. Invalid messenger.";
		cannot_get_new_cokey = "Network error. CANNONT GET NEW cokey";
		cannot_secure_conn_bad_oper = "Network error. Cannot secure connection. Bad oper '%s' while %s";
		cannot_secure_conn_bad_cokk = "Network error. Cannot secure connection. Bad cokk " +
				"ssc_cokk(%s) != peer_cokk(%s)";
		cannot_secure_conn_bad_coid = "Network error. Cannot secure connection. Bad coid " +
				" \n ssc_coid(%s) != coid(%s)";
		cannot_read_coid_file = "Network error. Cannot secure connection. Cannot read coid file '%s'";
		corrupt_coid_file = "Network error. Cannot secure connection. Corrupt coid file '%s'";
		no_such_old_coid_file = "Network error. NO SUCH OLD COID FILE '%s' !!!!";
		cannot_decrypt_msg = "Network error. Cannot secure connection. Cannot decrypt received %s.";
		already_existing_coid_file = "Network error. Cannot secure connection. Already existing coid file '%s'";
		cannot_recv_mem_files = "Network error. Cannot receive mem files. Invalid msg '%s'.";
		no_owner = "Network error. No connection owner found.";
		invalid_tcp_local_peer = "Network error. Invalid TCP local peer.";
		invalid_tcp_remote_peer = "Network error. Invalid TCP remote peer.";
		cannot_connect_to_same_tcp_peer = "Network error. Cannot connect to same TCP peer '%s'.";
		null_tcp_server_socket = "Network error. TCP srv_socket is null";
		cannot_close_tcp_server_socket = "Network error. cannot close TCP srv_socket";
		tcp_local_peer_is_null = "Network error. TCP local_peer is null.";
		tcp_remote_peer_is_null = "Network error. TCP remote_peer is null.";
		cannot_get_tcp_input_stream = "Network error. cannot get input stream from TCP connection.";
		cannot_get_tcp_output_stream = "Network error. cannot get output stream from TCP connection.";
		mudp_local_peer_is_null = "Network error. MUDP local_peer is null.";
		mudp_remote_peer_is_null = "Network error. MUDP remote_peer is null.";
		invalid_mudp_local_peer = "Network error. Invalid MUDP local peer description '%s'.";
		invalid_mudp_remote_peer = "Network error. Invalid MUDP remote peer description '%s'.";
		cannot_connect_to_same_mudp_peer = "Network error. Cannot connect to same MUDP peer '%s'.";
		null_mudp_server_socket = "Network error. MUDP srv_socket is null";
		could_secure_connection_with = "Network error. Could not secure connection with '%s'";
		coname_reported_and_original_differ = "Network error. Reported glid '%s' and original glid '%s' differ.";
		no_locators_found = "Network error. Cannot find any locators in '%s'";
		bad_message_name = "Network error. Bad message. Expecting '%s'";
		cannot_find_gammal_sys = "Network error. Cannot find gamal system. Reading '%s'";
		add_supra_locator_first = "Network error. Add a supralocator to the supralocators list. Then run again.";
		
	}
}
