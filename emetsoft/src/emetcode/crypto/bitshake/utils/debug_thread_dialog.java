package emetcode.crypto.bitshake.utils;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

public class debug_thread_dialog extends JFrame {
	private static final long serialVersionUID = 1L;

	Object lock;

	private static WindowListener closeWindow = new WindowAdapter() {
		public void windowClosing(WindowEvent ee) {
			debug_thread_dialog dd = (debug_thread_dialog) ee.getWindow();
			synchronized (dd.lock) {
				dd.setVisible(false);
				dd.lock.notify();
				dd.dispose();
			}
		}
	};

	private debug_thread_dialog(String msg) {
		super(msg);
		lock = new Object();
	}

	public static void msg(String msg) {
		debug_thread_dialog f7 = new debug_thread_dialog(msg);
		f7.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
		f7.setSize(300, 300);
		f7.addWindowListener(closeWindow);
		f7.setVisible(true);

		synchronized (f7.lock) {
			while (f7.isVisible()) {
				try {
					f7.lock.wait();
				} catch (InterruptedException ee) {
					ee.printStackTrace();
				}
			}
		}
		// System.out.println("Finished debug_thread_dialog " + msg);
	}
}
