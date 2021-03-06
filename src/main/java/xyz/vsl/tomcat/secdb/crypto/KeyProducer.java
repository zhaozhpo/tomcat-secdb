package xyz.vsl.tomcat.secdb.crypto;

import java.util.Properties;

/**
 * @author Vladimir Lokhov
 */
public interface KeyProducer {

    public String getText();

    public String getOptions();

    public interface Factory {
        public KeyProducer keyProducer(String options, String property, Properties properties);
    }

}
