package emetcode.economics.netpasser;

public interface trans_operator {
	public void queue_transaction(transaction working_trans);

	public transaction wait_for_transaction(transaction working_trans);
}
