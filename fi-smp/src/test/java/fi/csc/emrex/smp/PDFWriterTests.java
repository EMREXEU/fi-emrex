package fi.csc.emrex.smp;

import fi.csc.emrex.common.model.Person;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

/**
 * Created by jpentika on 02/11/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class PDFWriterTests extends TestCase {

    private InstitutionDataWriter institutionDataWriter;

    private static String pdfBaseDir = "/tmp/";
    private static String mapFile = "test_dirmap.json";
    private static String institutionDir1 = "testFolderFor";
    private static String institutionDir2 = "Blaablaa";

    @Before
    public void setup()
    {
        Person user;
        user = new Person();
        user.setHeiOid("HEIOID");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setHomeOrganization("blaablaa.fi");

        institutionDataWriter = new InstitutionDataWriter(user);
        institutionDataWriter.setPdfBaseDir(pdfBaseDir);
        File resourcesDirectory = new File("src/test/resources");
        String resourcePath = resourcesDirectory.getAbsolutePath();
        institutionDataWriter.setDirMap(resourcePath + "/" + mapFile);


    }

    @Before
    public void clearPreviousData() {
        deleteFolder(new File(pdfBaseDir + institutionDir1 + "/" + institutionDir2));
        new File(pdfBaseDir + institutionDir1).delete();
    }

    @Test
    public void testWriteFile() throws Exception {
        String data = "justTesting";
        byte[] testData = data.getBytes("UTF-8");

        institutionDataWriter.writeDataToInstitutionFolder(testData, ".pdf");
        File contentDir = new File(pdfBaseDir + institutionDir1 + "/" + institutionDir2);
        File[] files = contentDir.listFiles();
        String content = "";
        assertEquals(1, files.length);
        try {
            for (File f : files)
                content = FileUtils.readFileToString(f);

        } catch (Exception ex) {
            fail("Write data to institution folder failed. " + ex.getMessage());
        }
        assertEquals(data, content);
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files)
                    f.delete();

        }
        folder.delete();
    }


}
