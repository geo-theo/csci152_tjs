public class App {
    public static void main(String[] args) {
        Cube cube = new Cube();

        // Apply command line moves before showing:
        if (args != null && args.length > 0) {
            for (String m : args) {
                cube.applyMove(m);
            }
        }

        // Ponsole output (final cube representation & solve commands)
        System.out.print(cube.toRequiredStringFormat());
        System.out.println("Solve commands: " + cube.getSolveCommands());

        RubiksCube view = new RubiksCube();
        view.show(cube);
    }
}