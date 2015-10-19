package fi.csc.emrex.ncp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by marko.hollanti on 07/10/15.
 */
public class GzipUtil {

    public static byte[] compress(String string) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(string.getBytes());
        gos.close();
        byte[] compressed = os.toByteArray();
        os.close();
        return compressed;
    }


    public static String gzipDecompress(byte[] compressed) throws IOException {
        byte[] bytes = gzipDecompressBytes(compressed);
        return new String(bytes, StandardCharsets.UTF_8);
    }


    public static byte[] gzipDecompressBytes(byte[] compressed) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            os.write(data, 0, bytesRead);
        }
        gis.close();
        return os.toByteArray();
    }
}
