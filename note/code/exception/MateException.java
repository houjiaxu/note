
public class MateException extends RuntimeException {
	public MateException(Throwable t) {
		super(t);
	}

	public MateException(Exception e) {
		super(e);
	}

	public MateException(String str) {
		super(str);
	}

	public MateException(String str, Object... var2) {
		super(String.format(str, var2));
	}


	/**
	 * 期望条件为false,否则抛出异常
	 * @param condition 条件
	 * @param msg 异常信息
	 */
	public static void expectFalse(boolean condition, String msg) {
		if (condition) {
			throw new MateException(msg);
		}
	}

	public static void throwEx(Exception e) {
		throw new MateException(e);
	}
}
