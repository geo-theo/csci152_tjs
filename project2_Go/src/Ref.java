import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Ref {

    static String[][] Board = {
            { "♖", "♘", "♗", "♕", "♔", "♗", "♘", "♖" },
            { "♙", "♙", "♙", "♙", "♙", "♙", "♙", "♙", },
            { "", "", "", "", "", "", "", "", },
            { "", "", "", "", "", "", "", "", },
            { "", "", "", "", "", "", "", "", },
            { "", "", "", "", "", "", "", "", },
            { "♟︎", "♟︎", "♟︎", "♟︎", "♟︎", "♟︎", "♟︎", "♟︎", },
            { "♜", "♞", "♝", "♛", "♚", "♝", "♞", "♜" }
    };
    static String temp = " ";
    public static void main(String[] args) {
        JFrame frame = new JFrame("Chess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.add(new JLabel("Chess w/o Rules", SwingConstants.CENTER), BorderLayout.NORTH);
        
        JPanel panel = new JPanel(new GridLayout(8, 8));


        
        ActionListener clickListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton source = (JButton) e.getSource();
                if (temp == " ") {
                    temp = source.getText();
                    source.setText(" ");
                } else {
                    source.setText(temp);
                    temp = " ";
                }
            }
        };
        boolean flipper = true;
        Font largeFont = new Font("SansSerif", Font.BOLD, 30);


        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                flipper = !flipper;
                JButton button = new JButton(String.valueOf(Board[i][j]));
                button.addActionListener(clickListener);
                button.setFont(largeFont);
                button.setUI(new BasicButtonUI());
                if (flipper) {
                    button.setBackground(Color.GRAY);
                }
                panel.add(button);
            }
            flipper = !flipper;
        }
        
        frame.add(panel, BorderLayout.CENTER);
        
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}