import java.util.Scanner;

public class App {
    static String[][] checkCube =   {
                                        {
                                            "w","b","w",
                                            "b","w","b",
                                            "w","b","w"
                                        },
                                        {
                                            "b","w","b",
                                            "w","b","w",
                                            "b","w","b"
                                        },
                                        {
                                            "r","g","r",
                                            "g","r","g",
                                            "r","g","r"
                                        },
                                        {
                                            "g","r","g",
                                            "r","g","r",
                                            "g","r","g"
                                        },
                                        {
                                            "y","o","y",
                                            "o","y","o",
                                            "y","o","y"
                                        },
                                        {
                                            "o","y","o",
                                            "y","o","y",
                                            "o","y","o"
                                        }, 
                                    };
    static String[][] cube =   {
                                    {
                                        "w","w","w",
                                        "w","w","w",
                                        "w","w","w"
                                    },
                                    {
                                        "b","b","b",
                                        "b","b","b",
                                        "b","b","b"
                                    },
                                    {
                                        "r","r","r",
                                        "r","r","r",
                                        "r","r","r"
                                    },
                                    {
                                        "g","g","g",
                                        "g","g","g",
                                        "g","g","g"
                                    },
                                    {
                                        "y","y","y",
                                        "y","y","y",
                                        "y","y","y"
                                    },
                                    {
                                        "o","o","o",
                                        "o","o","o",
                                        "o","o","o"
                                    }, 
                                };
    public static void main(String[] args) throws Exception {

        RubiksCube Cube = new RubiksCube();

        Scanner scn = new Scanner(System.in);

        System.out.println("Would you like to see the solved cube (1) or checkered cube (2)? (1,2) ");
        int ans = scn.nextInt();

        if(ans == 1)
            Cube.show(cube);
        else
            Cube.show(checkCube);

        scn.close();

    }
}