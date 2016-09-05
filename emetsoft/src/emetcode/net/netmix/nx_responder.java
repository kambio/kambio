package emetcode.net.netmix;

public interface nx_responder {

	public byte[] get_response(nx_peer rem_peer, nx_conn_id conn, byte[] msg);
	
}
