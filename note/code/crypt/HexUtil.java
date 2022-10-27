/**
 * 二进制byte数组转十六进制byte数组
 */
public class HexUtil {

	private static final char[] toDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


	public static byte[] decodeHex(char[] chars) throws MateException {
		int length = chars.length;
		if ((length & 1) != 0) {
			throw new MateException("Odd number of characters.");
		} else {
			byte[] out = new byte[length >> 1];
			int i = 0;

			for(int j = 0; j < length; ++i) {
				int var5 = toDigit(chars[j], j) << 4;
				++j;
				var5 |= toDigit(chars[j], j);
				++j;
				out[i] = (byte)(var5 & 255);
			}
			return out;
		}
	}

	protected static int toDigit(char ch, int index) throws MateException {
		int digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new MateException("Illegal hexadecimal charcter " + ch + " at index " + index);
		} else {
			return digit;
		}
	}


	public static char[] encodeHex(byte[] data) {
		int len = data.length;
		char[] var2 = new char[len << 1];
		int i = 0;

		for(int var4 = 0; i < len; ++i) {
			var2[var4++] = toDigits[(240 & data[i]) >>> 4];
			var2[var4++] = toDigits[15 & data[i]];
		}

		return var2;
	}
}
