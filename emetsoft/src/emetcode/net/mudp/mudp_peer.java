package emetcode.net.mudp;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class mudp_peer implements Comparable<mudp_peer> {
	InetSocketAddress sok_addr;
	BigInteger id;

	public mudp_peer(InetAddress addr_val, int port_val) {
		sok_addr = new InetSocketAddress(addr_val, port_val);
		id = calc_id();
	}

	public mudp_peer(InetSocketAddress sok) {
		sok_addr = sok;
		id = calc_id();
	}

	private BigInteger calc_id() {
		int port = sok_addr.getPort();
		//byte[] addr_by = addr.getAddress();
		byte[] addr_by = sok_addr.getAddress().getAddress();
		int int_num_by = Integer.SIZE / Byte.SIZE;
		byte[] full_id = new byte[addr_by.length + int_num_by];
		ByteBuffer buff_id = ByteBuffer.wrap(full_id);
		buff_id.put(addr_by);
		buff_id.putInt(port);
		BigInteger id = new BigInteger(full_id);
		return id;
	}

	public BigInteger get_id() {
		return id;
	}

	public String toString() {
		return get_ip_descr();
	}

	public String get_ip_descr() {
		InetAddress addr = sok_addr.getAddress();
		return addr.getHostAddress() + config.FLD_SEP + sok_addr.getPort();
	}

	public InetSocketAddress get_socket_address() {
		return sok_addr;
	}
	
	@Override
	public int compareTo(mudp_peer pp2) {
		BigInteger id1 = get_id();
		BigInteger id2 = pp2.get_id();
		return id1.compareTo(id2);
	}

	public boolean equals(Object opp2) {
		mudp_peer pp2 = (mudp_peer) opp2;
		return (compareTo(pp2) == 0);
	}
}
