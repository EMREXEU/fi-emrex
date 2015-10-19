/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import fi.csc.emrex.smp.model.Person;
import fi.csc.emrex.smp.model.VerificationReply;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author salum
 */
public class UserVerificationTest extends TestCase {

    private Person shib;
    private Person elmo1;
    private Person elmo2;
    private Person elmo3;
    private Person elmo4;

    @Before
    public void setUp() throws Exception {
        shib = new Person();
        shib.setBirthDate("1980-01-01", "yyyy-MM-dd");
        shib.setFirstName("Arvi Arvo Aatami");
        shib.setLastName("Lind");
        shib.setThreshold(0.1);

        elmo1 = new Person();
        elmo1.setBirthDate("1980-01-01", "yyyy-MM-dd");
        elmo1.setFirstName("Arvi Arvo Aatami");
        elmo1.setLastName("Lind");

        elmo2 = new Person();
        elmo2.setBirthDate("1980-10-01", "yyyy-MM-dd");
        elmo2.setFirstName("Arvi Arvo Aatmami");
        elmo2.setLastName("Lind");

        elmo3 = new Person();
        elmo3.setBirthDate("1980-01-01", "yyyy-MM-dd");
        elmo3.setFirstName("Arvi Arvo Aatmami");
        elmo3.setLastName("Lind");

        elmo4 = new Person();
        elmo4.setBirthDate("1980-01-01", "yyyy-MM-dd");
        elmo4.setFirstName("Jori");
        elmo4.setLastName("Hulkkonen");

    }

    @Test
    public void testVerifyPerson() {

        assertTrue(shib.verifiy(elmo1).isVerified());
        assertFalse(shib.verifiy(elmo2).isVerified());
        VerificationReply r= shib.verifiy(elmo3);
        System.out.println(r.getMessages());
        assertTrue(shib.verifiy(elmo3).isVerified());
        assertFalse(shib.verifiy(elmo4).isVerified());

    }

}
