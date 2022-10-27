

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class AES {

	/**生成秘钥**/
	public static SecretKeySpec generateMySQLAESKey(String password) {
		byte[] bytes = new byte[16];
		int i = 0;
		byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
		int length = passwordBytes.length;

		for(int j = 0; j < length; ++j) {
			byte var6 = passwordBytes[j];
			int var10001 = i++;
			bytes[var10001 % 16] ^= var6;
		}

		return new SecretKeySpec(bytes, "AES");
	}

	//secret  textContent
	public static String decrypt(String password, String content) throws Exception {
		Cipher var2 = Cipher.getInstance("AES");
		var2.init(2, generateMySQLAESKey(password));
		return new String(var2.doFinal(HexUtil.decodeHex(content.toCharArray())));
	}

	//secret  textContent
	public static String encrypt(String password, String content) throws Exception {
		Cipher var2 = Cipher.getInstance("AES");
		var2.init(1, generateMySQLAESKey(password));
		char[] var3 = HexUtil.encodeHex(var2.doFinal(content.getBytes(StandardCharsets.UTF_8)));
		return new String(var3);
	}

	/*public static void main(String[] args) throws Exception {
		String originStr = "00003333";
		String password = "123456789";
		String encryptStr = encrypt(password, originStr);
		String str = "d28a02649600f181c2ede844491f4c82228d8bedd9f71848685be6c189c3e96b9b721bbfabb6a543ddf0a41fe6b69da3";
		System.out.println("加密后: " + encryptStr);
		System.out.println(encrypt(password, encryptStr).equals(str));

		String decrypt = decrypt(password, encryptStr);
		System.out.println("解密后: " +decrypt);

	}*/
}
