package fi.csc.emrex.common.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Created by marko.hollanti on 06/10/15.
 */
public class TestUtil {

    private final static String ENCODING = StandardCharsets.UTF_8.name();

    private static TestUtil instance;

    private TestUtil() {
    }

    public static String getFileContent(String filename) throws Exception {
        if (instance == null) {
            instance = new TestUtil();
        }
        return FileUtils.readFileToString(FileUtils.toFile(instance.getClass().getResource("/" + filename)), ENCODING);
    }

    public static File getFile(String filename) throws Exception {
        if (instance == null) {
            instance = new TestUtil();
        }
        return FileUtils.toFile(instance.getClass().getResource("/" + filename));
    }
}
