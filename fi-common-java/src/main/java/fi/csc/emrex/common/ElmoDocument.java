package fi.csc.emrex.common;

import java.util.List;

/**
 * Created by marko.hollanti on 20/08/15.
 */
public class ElmoDocument {

    private String personName;
    private String institutionName;
    private String birthday;
    private List<ElmoResult> results;

    public String getPersonName() {
        return personName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public List<ElmoResult> getResults() {
        return results;
    }

    public void setResults(List<ElmoResult> results) {
        this.results = results;
    }
}
