package org.example.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 密钥工具类。
 * <p>
 * 当前项目使用私钥生成 JWT，使用公钥校验 JWT。
 */
public class RsaUtils {
    private static final int DEFAULT_KEY_SIZE = 2048;

    private static byte[] readFile(String fileName) throws IOException {
        return Files.readAllBytes(new File(fileName).toPath());
    }

    private static void writeFile(String destPath, byte[] bytes) throws IOException {
        File dest = new File(destPath);
        if (!dest.exists()) {
            dest.createNewFile();
        }
        Files.write(dest.toPath(), bytes);
    }

    /**
     * 从文件读取并解析 RSA 公钥。
     */
    public static PublicKey getPublicKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);
        return getPublicKey(bytes);
    }

    /**
     * 从 base64 字节内容解析 RSA 公钥。
     */
    public static PublicKey getPublicKey(byte[] bytes) throws Exception {
        bytes = Base64.getDecoder().decode(bytes);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    /**
     * 从文件读取并解析 RSA 私钥。
     */
    public static PrivateKey getPrivateKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);
        return getPrivateKey(bytes);
    }

    /**
     * 从 base64 字节内容解析 RSA 私钥。
     */
    public static PrivateKey getPrivateKey(byte[] bytes) throws InvalidKeySpecException,
            java.security.NoSuchAlgorithmException {
        bytes = Base64.getDecoder().decode(bytes);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    /**
     * 根据密钥种子生成 RSA 公私钥文件。
     */
    public static void generateKey(String publicKeyFilename,
                                   String privateKeyFilename,
                                   String secret,
                                   int keySize) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        keyPairGenerator.initialize(Math.max(keySize, DEFAULT_KEY_SIZE), secureRandom);
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        byte[] publicKeyBytes = Base64.getEncoder().encode(keyPair.getPublic().getEncoded());
        writeFile(publicKeyFilename, publicKeyBytes);

        byte[] privateKeyBytes = Base64.getEncoder().encode(keyPair.getPrivate().getEncoded());
        writeFile(privateKeyFilename, privateKeyBytes);
    }

    /**
     * 本地手动生成密钥时使用，运行前按需修改输出路径。
     */
    public static void main(String[] args) throws Exception {
        String publicKeyFilename = "C:\\Users\\caoyehang\\Desktop\\spring3-demo\\src\\main\\resources\\key\\pub_rsa";
        String privateKeyFilename = "C:\\Users\\caoyehang\\Desktop\\spring3-demo\\src\\main\\resources\\key\\pri_rsa";
        generateKey(publicKeyFilename, privateKeyFilename, "cyhlsz97%00~01*10&12#09", 2048);
    }
}
