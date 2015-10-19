package fi.csc.emrex.common;

/**
 * Created by marko.hollanti on 20/08/15.
 */
public class ElmoResult {

    private String code;
    private String name;
    private String result;
    private String credits;
    private String level;
    private String type;
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }



    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public String getLevel() {
        return level;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLevel(String level){
        this.level = level;
    }
}
