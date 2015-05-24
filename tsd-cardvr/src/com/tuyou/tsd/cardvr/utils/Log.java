package com.tuyou.tsd.cardvr.utils;

import java.lang.reflect.Array;

public class Log {
	private static boolean closeLog = false;
	public static final int LOG_LEVEL_V = 1;
	public static final int LOG_LEVEL_D = 2;
	public static final int LOG_LEVEL_I = 4;
	public static final int LOG_LEVEL_W = 8;
	public static final int LOG_LEVEL_E = 16;
	public static final int LOG_LEVEL_NONE = 0;
	public static final int LOG_LEVEL_ALL = 31;
	private static int logLevel = 31;
	private static String THIS_CLASS = Log.class.getName();
	private static long timePoint;

	public static void print(Object[] os) {
		if (closeLog) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < os.length; i++) {
			if (i != 0)
				sb.append(", ");
			sb.append(os[i]);
		}
		android.util.Log.v("print", sb.toString());
	}

	public static void v() {
		v(null);
	}

	public static void v(Object o) {
		if (closeLog) {
			return;
		}
		if ((logLevel & 0x1) != 0)
			log(2, o);
	}

	public static void d() {
		d(null);
	}

	public static void d(Object o) {
		if (closeLog) {
			return;
		}
		if ((logLevel & 0x2) != 0)
			log(3, o);
	}

	public static void i() {
		i(null);
	}

	public static void i(Object o) {
		if (closeLog) {
			return;
		}
		if ((logLevel & 0x4) != 0)
			log(4, o);
	}

	public static void w() {
		w(null);
	}

	public static void w(Object o) {
		if (closeLog) {
			return;
		}
		if ((logLevel & 0x8) != 0)
			log(5, o);
	}

	public static void e() {
		e(null);
	}

	public static void e(Object o) {
		if (closeLog) {
			return;
		}
		if ((logLevel & 0x10) != 0)
			log(6, o);
	}

	private static String[] getCaller() {
		Throwable ex = new Throwable();
		StackTraceElement[] trace = ex.getStackTrace();
		for (int i = 3; i < trace.length; i++) {
			String className = trace[i].getClassName();
			if (!THIS_CLASS.equals(className)) {
				className = className.substring(className.lastIndexOf('.') + 1);
				if (className.indexOf('$') > 0)
					className = className.substring(0, className.indexOf('$'));
				return new String[] { className, trace[i].getMethodName(),
						String.valueOf(trace[i].getLineNumber()) };
			}
		}
		return new String[3];
	}

	private static void log(int type, Object o) {
		if (closeLog) {
			return;
		}
		String[] caller = getCaller();
		String tag = null;
		String msg = null;
		Throwable e = null;

		tag = caller[0];

		msg = caller[2] + '[' + caller[1] + ']';
		if ((o == null) || ((o instanceof Throwable))) {
			if (o != null)
				msg = msg + '\n'
						+ android.util.Log.getStackTraceString((Throwable) o);
		} else if (o.getClass().isArray()) {
			int len = Array.getLength(o);
			String name = o.getClass().getSimpleName();
			name = name.substring(0, name.length() - 1) + len + "]";
			msg = msg + name;
			Object item;
			if ((len > 0) && ((item = Array.get(o, 0)) != null))
				msg = msg + ": " + item.toString();
		} else {
			msg = msg + String.valueOf(o);
		}
		log(type, tag, msg, e);
	}

	private static void log(int type, String tag, String msg, Throwable e) {
		if (closeLog) {
			return;
		}
		android.util.Log.println(type, tag, msg);
	}

	public static void setLogLevel(int level) {
		logLevel = level & 0x1F;
	}

	public static void t(long time) {
		if (closeLog) {
			return;
		}
		timePoint = time;
		d();
	}

	public static void t() {
		if (closeLog) {
			return;
		}
		long now = System.currentTimeMillis();
		if (timePoint == 0L)
			timePoint = now;
		d(Long.valueOf(now - timePoint));
	}

	public static void printStackTrace() {
		if (closeLog) {
			return;
		}
		Throwable ex = new Throwable();
		StackTraceElement[] trace = ex.getStackTrace();
		StringBuilder sb = new StringBuilder();

		int i = 1;
		for (int j = Math.min(100, trace.length); i < j; i++) {
			sb.setLength(0);
			sb.append(i).append("@").append(trace[i].getMethodName())
					.append('\t').append(trace[i].getLineNumber());
			String className = trace[i].getClassName();
			className = className.substring(className.lastIndexOf('.') + 1);
			android.util.Log.println(i == 1 ? 4 : 2, className, sb.toString());
		}
	}

	public static void closeLog() {
		closeLog = true;
	}
}