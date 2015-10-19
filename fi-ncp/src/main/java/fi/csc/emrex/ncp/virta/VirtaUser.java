package fi.csc.emrex.ncp.virta;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDate;
import java.util.Date;
import java.util.Enumeration;

/**
 * Created by marko.hollanti on 07/10/15.
 */
@Getter
@Setter
public class VirtaUser {

    public VirtaUser(String oid, String ssn) {
        this.oid = oid;
        this.ssn = ssn;
    }

    private String oid;
    private String ssn;

    public boolean isOidSet() {
        return !StringUtils.isBlank(oid);
    }
}
