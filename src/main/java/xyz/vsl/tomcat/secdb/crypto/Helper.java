package xyz.vsl.tomcat.secdb.crypto;

import javax.crypto.SecretKey;
import java.util.Properties;

/**
 * @author Vladimir Lokhov
 */
public class Helper {

    public static String encryptAndFormat(KeyProducer key, String value, boolean fragment) {
        SecretKey secretKey = Key.generate("AES", key);
        String encrypted = new Encryptor().encrypt(secretKey, value);
        if (fragment && !encrypted.endsWith("="))
            encrypted = encrypted + "=";
        return "{" + secretKey.getAlgorithm() + ":" + key.getOptions()+"}" + encrypted;
    }

    public static StringParts extract(String s) {
        StringParts sp = new StringParts();
        int pos = s.indexOf("{AES");
        if (pos < 0) return sp;
        sp.setHead(s.substring(0, pos));
        int i = pos + "{AES".length();
        boolean eq = false;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c != '=' && eq)
                break;
            eq = c == '=';
            i++;
        }
        sp.setText(s.substring(pos, i));
        sp.setTail(i < s.length() ? s.substring(i) : "");
        return sp;
    }

    public static String decrypt(String part, String targetProperty, Properties properties, KeyProducer.Factory keyProducerFactory) {
        int pos = part.indexOf('}');
        if (pos < 0 || pos == part.length() - 1)
            return null;
        String secret = part.substring(pos + 1);
        int colon = part.lastIndexOf(':', pos);
        String algorithm;
        String options;
        if (colon > 0) {
            options = part.substring(colon + 1, pos);
            algorithm = part.substring(1, colon);
        } else {
            options = "";
            algorithm = part.substring(1, pos);
        }
        KeyProducer kp = keyProducerFactory.keyProducer(options, targetProperty, properties);
        return new Encryptor().decrypt(Key.generate(algorithm, kp), secret);
    }

    public static String decryptAll(String value, String targetProperty, Properties properties, KeyProducer.Factory keyProducerFactory) {
        while (true) {
            StringParts sp = extract(value);
            if (sp.getText() == null)
                return value;
            value = sp.getHead() + decrypt(sp.getText(), targetProperty, properties, keyProducerFactory) + sp.getTail();
        }
    }
}
