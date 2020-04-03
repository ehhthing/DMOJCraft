package science.larry.dmojcraft.dmoj;

public class Testcase {
    public int id;
    public String descriptor;
    public String status;
    public String details;

    public String toString() {
        return "[" + id + "] " + descriptor + " " + status + " " + details;
    }
}
