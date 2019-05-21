package breaking;

import java.util.Optional;

public class BreakingTest {
    public static void main(String[] args) {
        System.out.println("In basic test");
    }

    private Optinal<String> neverUsed;

    public BreakingTest() {
        try {
            neverUsed.get();
        }
        catch (Exception e) {

        }
    }

    private void doThing() throws NullPointerException {
        System.out.println("Doing a thing");
    }
}