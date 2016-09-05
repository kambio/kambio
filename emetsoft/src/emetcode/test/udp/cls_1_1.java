package emetcode.test.udp;

public class cls_1_1 extends cls_1 {
	
	
	void mth_1(){
		System.out.println("cls_1_1.imple_mth_1");
	}

	void mth_2(){
		System.out.println("cls_1_1.mth_2");
	}

	public static void main(String args[]) throws Exception {
		cls_1_1 oo1 = new cls_1_1();
		oo1.mth_1();
		oo1.mth_2();

		cls_1_2 oo2 = new cls_1_2();
		oo2.mth_1();
		oo2.mth_2();
	}	
}
