package emetcode.net.netmix;

import java.util.concurrent.atomic.AtomicBoolean;

import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.net.netmix.locale.L;

public abstract class nx_peer {
	public static final boolean IN_DEBUG_1 = true;
	public static final boolean IN_DEBUG_2 = false;

	public static final int MIN_GAMAL_BITS = 1000;
	public static final int MIN_GAMAL_CERTAINTY = 1000;

	public static final byte[] DEFAULT_KEY = "empty_key".getBytes(config.UTF_8);

	public static final char PORT_SEP = ':';
	public static final String PORT_SEP_STR = ":";
	
	protected nx_context context;
	protected boolean is_remote;
	protected boolean is_server;
	protected String local_name;
	protected String str_description;

	private key_owner owner;

	private boolean is_started_set;
	private AtomicBoolean is_srv_started;
	
	// use context.make_peer insted
	protected nx_peer() {
		init_nx_peer();
	}

	protected void init_nx_peer() {
		context = null;
		is_remote = false;
		is_server = false;
		local_name = "no_name";
		str_description = null;
		owner = null;

		is_started_set = false;
		is_srv_started = new AtomicBoolean(false);
	}
	
	protected void set_server_started(){
		if(! is_started_set){
			is_started_set = true;
			is_srv_started.set(true);
		}
	}
	
	public boolean is_server_started(){
		return is_srv_started.get();
	}
	
	public static nx_peer get_copy(nx_peer orig){
		if(orig == null){
			return null;
		}
		nx_peer cp = orig.get_context().make_peer();
		if(orig.is_remote_peer()){
			cp.init_remote_peer(orig);
		} else {
			cp.init_local_peer(orig);
		}
		return cp;
	}

	public int get_net_type() {
		return context.get_net_type();
	}

	public abstract void init_local_peer(String local_name, key_owner owr,
			boolean is_srv);

	public abstract void init_remote_peer(String remote_name, String descrip);

	public void init_local_peer(nx_peer orig) {
		if (orig.is_remote_peer()) {
			throw new bad_netmix(2);
		}
		key_owner c_owr = key_owner.get_copy(orig.get_owner());
		init_local_peer(orig.get_description(), c_owr, orig.is_server);
	}

	public void init_remote_peer(nx_peer orig) {
		if (!orig.is_remote_peer()) {
			throw new bad_netmix(2);
		}
		init_remote_peer(orig.get_local_name(), orig.get_description());
	}

	public abstract nx_connection connect_to(nx_peer pp);

	public abstract nx_connection connect_to(String dest_descrip);

	public abstract nx_connection accept();

	public abstract boolean can_accept();

	public abstract void kill_accept();

	public boolean is_remote_peer() {
		return is_remote;
	}

	public void set_context(nx_context ctx) {
		context = ctx;
	}

	public nx_context get_context() {
		return context;
	}

	public String get_local_name() {
		return local_name;
	}

	public String get_description() {
		return str_description;
	}

	public void set_owner(key_owner owr) {
		if (owr == null) {
			return;
		}
		owner = owr;
	}

	public boolean has_owner() {
		return (owner != null);
	}

	public key_owner get_owner() {
		if (owner == null) {
			throw new bad_netmix(2, L.no_owner);
		}
		return owner;
	}

	public void init_responder(nx_responder rr) {
		throw new bad_netmix(2);
	}

	public boolean can_respond() {
		throw new bad_netmix(2);
	}

	public void kill_responder() {
		throw new bad_netmix(2);
	}

	public void respond() {
		throw new bad_netmix(2);
	}

	public nx_connection get_requester(nx_peer dest, nx_conn_id req_conn) {
		throw new bad_netmix(2);
	}

	public String get_description_port(){
		return get_description_port(get_description());
	}
	
	public static String get_description_port(String descr){
		if(descr == null){
			return null;
		}
		int idx1 = descr.lastIndexOf(nx_peer.PORT_SEP);
		if(idx1 < 0){
			return descr;
		}
		int idx2 = idx1 + 1;
		if(idx2 > descr.length()){
			return descr;
		}
		return descr.substring(idx2);
	}
	
	public static String strip_description_port(String descr){
		if(descr == null){
			return null;
		}
		int idx_descr = descr.lastIndexOf(nx_peer.PORT_SEP);
		if(idx_descr == -1){
			idx_descr = descr.length();
		}		
		String out = descr.substring(0, idx_descr);
		return out;
	}
	
	public static String set_description_port(String src, String dest){
		if(src == null){
			return dest;
		}
		if(dest == null){
			return null;
		}
		int idx_src = src.lastIndexOf(nx_peer.PORT_SEP);
		if(idx_src == -1){
			return dest;
		}
		String out = strip_description_port(dest);
		if(idx_src + 1 <= src.length()){
			out += nx_peer.PORT_SEP + src.substring(idx_src + 1);
		}
		return out;
	}
	
	public boolean same_description(nx_peer pp2){
		if(pp2 == null){
			return false;
		}
		if(! (pp2 instanceof nx_peer)){
			return false;
		}
		return get_description().equals(pp2.get_description());
	}
	
	public String toString(){
		return get_description();
	}
}
