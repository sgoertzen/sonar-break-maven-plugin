public class Basic {
    public static void main(String[] args) {
        try {
            int unused = 3;
        } catch (Exception e) {
            // Should not have done this.. Oh no!
        }
    }
}