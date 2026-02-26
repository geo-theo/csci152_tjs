public class App {
    public static void main(String[] args) {
        Cube cube = new Cube();

        // If you want to apply command line moves before showing:
        if (args != null && args.length > 0) {
            for (String m : args) {
                cube.applyMove(m);
            }
        }

        RubiksCube view = new RubiksCube();
        view.show(cube);
    }
}