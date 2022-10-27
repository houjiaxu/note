/***
 * 需依赖hutool包
 */
public class SM{

	public static void main(String[] args) {
			String content = "00003333";
	//		SM2 sm2 = new SM2("00e7e77943b97df66e0426670b40cf0de65336fd2cd1a5ca80581832c4655131a7",
	//				"047472c78673c0786c5c9e4c98af52ff5f662a6aacc1ab9f6a987d7503256e3f9985bee544285e19b5c5689f7e1d1e38b18c4e1c56c96a468c25ad2b37b3b7bf77");
			/*SM2 sm2 = new SM2("00cceddcaf2ea5a7d00f3c5c7d419a3baaccf40c10b69ef5f97c0d6655033e41a1",
					"04aa8e541af50100bc070fad296205978f4ea8bd710207c6bb5586ce510dba27751406ccfcabcade86fb7bf19b94a9178cd92c1426dfe6f699c19c589d008904d2");

		String encryptBase64 = sm2.encryptBase64("00003333", KeyType.PublicKey);
		System.out.println("加密后: "+encryptBase64);
		String decryptStr = sm2.decryptStr(encryptBase64, KeyType.PrivateKey, StandardCharsets.UTF_8);
		System.out.println("解密后: "+decryptStr);*/

		// 2. hutool写法 生成秘钥
		/*SM2 sm2 = SmUtil.sm2();
		String hutoolPrivateKeyHex = HexUtil.encodeHexStr(BCUtil.encodeECPrivateKey(sm2.getPrivateKey()));
		String hutoolPublicKeyHex = HexUtil.encodeHexStr(((BCECPublicKey) sm2.getPublicKey()).getQ().getEncoded(false));
		System.out.println(hutoolPrivateKeyHex);
		System.out.println(hutoolPublicKeyHex);*/

		//SM3
		//System.out.println(SmUtil.sm3(content));


		//3.SM4 秘钥生成及测试
		//秘钥生成
		/*String sm4Key = RandomUtil.randomString(RandomUtil.BASE_CHAR_NUMBER, 16);
		System.out.println("sm4Key:"+sm4Key);
		//加密

		sm4Key = "48wrph8bymalern6";
		SM4 sm4 = SmUtil.sm4(sm4Key.getBytes(StandardCharsets.UTF_8));
		String encryptBase641 = sm4.encryptBase64(content);
		System.out.println("加密后: "+encryptBase641);
		//解密
		String s = sm4.decryptStr(encryptBase641, StandardCharsets.UTF_8);
		System.out.println("解密后: "+s);*/

		//4.base64
		/*String contentstr = Base64Util.encode(content);
		System.out.println("加密后: "+contentstr);
		String s1 = Base64Util.decodeAndGetString(contentstr);
		System.out.println("解密后: "+s1);*/


		//5. aes
		/*String mykey ="1234567980121123";//16位自定义密码,必须16位.
		byte[] key = KeyUtil.generateKey("AES",mykey.getBytes()).getEncoded();
		SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, key);
		//加密为16进制表示
		String encryptHex = aes.encryptHex(content); //6196aebf894993b820ed97ba1928b047
		System.out.println(encryptHex);
		String decryptStr = aes.decryptStr(encryptHex, StandardCharsets.UTF_8);
		 //test中文
		System.out.println(decryptStr);*/


		}
}