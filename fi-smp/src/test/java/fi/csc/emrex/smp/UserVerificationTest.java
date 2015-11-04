/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import fi.csc.emrex.common.model.Person;
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

        assertTrue(VerificationReply.verify(shib,elmo1,0.1).isVerified());
        assertFalse(VerificationReply.verify(shib, elmo2, 0.1).isVerified());
        VerificationReply r=  VerificationReply.verify(shib, elmo3, 0.1);
        System.out.println(r.getMessages());
        assertTrue(VerificationReply.verify(shib, elmo3, 0.1).isVerified());
        assertFalse(VerificationReply.verify(shib, elmo4, 0.1).isVerified());

    }

}
