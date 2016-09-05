package io.github.kambio.kambio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.kambio.kambio.R;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.crypto.gamal.gamal_generator;
import emetcode.economics.passet.config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

public class misce {
	public static final String LOGTAG = "misce";

	public static final String UNKNOWN_STR = "unknown";

	private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";
	private static Pattern VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern,
			Pattern.CASE_INSENSITIVE);
	private static Pattern VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern,
			Pattern.CASE_INSENSITIVE);

	public static boolean is_ipv6(String addr) {
		Matcher m1 = VALID_IPV6_PATTERN.matcher(addr);
		return m1.matches();
	}

	public static boolean is_ipv4(String addr) {
		Matcher m1 = VALID_IPV4_PATTERN.matcher(addr);
		return m1.matches();
	}

	public static String get_local_phone_number(Activity act) {
		TelephonyManager mgr = ((TelephonyManager) act
				.getSystemService(Activity.TELEPHONY_SERVICE));
		String phone_num = mgr.getLine1Number();
		if (phone_num == null) {
			return UNKNOWN_STR;
		}
		return phone_num;
	}

	public static String get_local_country_iso(Activity act) {
		TelephonyManager mgr = ((TelephonyManager) act
				.getSystemService(Activity.TELEPHONY_SERVICE));
		String phone_num = mgr.getSimCountryIso();
		if (phone_num == null) {
			return UNKNOWN_STR;
		}
		return phone_num;
	}

	public static String[] get_all_local_ip_addr() {
		List<String> all_ip = new ArrayList<String>();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					String ip_str = inetAddress.getHostAddress().toString();

					boolean is_v4 = is_ipv4(ip_str);
					if (!inetAddress.isLoopbackAddress() && is_v4) {
						all_ip.add(ip_str);
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("no_ip", ex.toString());
		}
		return all_ip.toArray(new String[0]);
	}

	public static String get_uri_path(Context ctx, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = ctx.getContentResolver().query(contentUri, proj, null,
				null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public static File get_new_media_file() {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		Environment.getExternalStorageState();

		File mediaStorageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(LOGTAG, "No directory" + mediaStorageDir);
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());

		File mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ "CASHME_PHOTO_" + timeStamp + ".jpg");

		return mediaFile;
	}

	public static void show_message(final Activity act, int resId,
			ok_doer doer, final boolean end_act) {
		String msg = act.getString(resId);
		show_message(act, msg, doer, end_act);
	}

	public static void show_message(final Activity act, String msg,
			final ok_doer the_doer, final boolean end_act) {

		boolean with_cancel = (the_doer != null);
		AlertDialog.Builder bb = new AlertDialog.Builder(act);

		String ok_str = act.getString(R.string.ok);

		bb.setMessage(msg);
		bb.setCancelable(with_cancel);
		bb.setPositiveButton(ok_str, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dd, int id) {
				if (the_doer != null) {
					the_doer.on_ok_do();
				}
				dd.cancel();
			}
		});
		if (with_cancel) {
			bb.setNegativeButton(R.string.cancel, null);
		}

		AlertDialog dlg = bb.create();
		if (end_act) {
			dlg.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					act.finish();
				}
			});
		}

		dlg.show();

		TextView txt = (TextView) dlg.findViewById(android.R.id.message);
		txt.setGravity(Gravity.CENTER);
	}

	public static gamal_generator read_gamal_sys(Context ctx, int num) {
		String nm_gam = "gam_sys/gamal_sys_" + num + ".dat";
		try {
			AssetManager am = ctx.getAssets();
			InputStream is = am.open(nm_gam);
			byte[] dat = mem_file.read_stream(is);
			if (dat == null) {
				return null;
			}
			String gg_str = new String(dat, config.UTF_8);
			gamal_generator gg = new gamal_generator(gg_str);
			return gg;
		} catch (IOException e) {
			Log.i(LOGTAG, "Cannot open '" + nm_gam + "'");
		}
		return null;
	}

	public static byte[] read_bytes_asset(Context ctx, String pth) {
		try {
			AssetManager am = ctx.getAssets();
			InputStream is = am.open(pth);
			byte[] dat = mem_file.read_stream(is);
			return dat;
		} catch (IOException e) {
			Log.i(LOGTAG, "Cannot open '" + pth + "'");
		}
		return null;
	}

	public static Drawable read_image_asset(Context ctx, String pth) {
		try {
			AssetManager am = ctx.getAssets();
			InputStream is = am.open(pth);
			return Drawable.createFromStream(is, pth);
		} catch (IOException e) {
			Log.i(LOGTAG, "Cannot open '" + pth + "'");
		}
		return null;
	}

	public static String get_futbol_image_name(int num) {
		String nm_gam = "futbol_img/futbol_" + num + ".jpeg";
		return nm_gam;
	}

	public static byte[] read_futbol_image(Context ctx, int num) {
		String nm_gam = get_futbol_image_name(num);
		return read_bytes_asset(ctx, nm_gam);
	}

	public static Drawable read_futbol_img(Context ctx, int num) {
		String nm_gam = get_futbol_image_name(num);
		return read_image_asset(ctx, nm_gam);
	}

}
