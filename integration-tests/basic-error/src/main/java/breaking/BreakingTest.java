package breaking;

public class BreakingTest {
    public static void main(String[] args) {
        System.out.println("In basic test");
    }

    private String neverUsed;

    public BreakingTest() {
        try {
            doThing();
        }
        catch (Exception e) {

        }
    }

    private void doThing() throws NullPointerException {
        System.out.println("Doing a thing");
    }
}