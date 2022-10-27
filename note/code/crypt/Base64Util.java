
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Util {
	/***
	 * 普通加密操作
	 * @param str
	 * @return
	 */
	public static String encode(String str) {
		return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
	}
	/***
	 * 普通加密操作
	 * @param data
	 * @return
	 */
	public static String encode(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	/***
	 * 普通解密操作
	 * @param str
	 * @return
	 */
	public static String decodeAndGetString(String str) {
		return new String(decode(str));
	}

	/***
	 * 普通解密操作
	 * @param encodedText
	 * @return
	 */
	public static byte[] decode(String encodedText) {
		return Base64.getDecoder().decode(encodedText);
	}
}
