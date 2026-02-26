import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RubiksCube extends JPanel implements ActionListener {

    private double angleX = -0.5, angleY = 0.5, angleZ = 0;
    private Point lastMousePos;
    private final List<Cubie> cubies = new ArrayList<>();
    private boolean isAutoRotating = true;

    public RubiksCube() {
        setBackground(new Color(20, 20, 20));

        // Initialize 27 cubies (3x3x3)
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    cubies.add(new Cubie(x, y, z));
                }
            }
        }

        // Mouse listeners for rotation logic
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
                isAutoRotating = false; // Stop auto-rotation on click
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos != null) {
                    Point currentPos = e.getPoint();
                    double dx = currentPos.x - lastMousePos.x;
                    double dy = currentPos.y - lastMousePos.y;

                    angleY += dx * 0.01;
                    angleX += dy * 0.01;

                    lastMousePos = currentPos;
                    repaint();
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        Timer timer = new Timer(16, this);
        timer.start();
    }

    // Renderer input: String[6][9] with order:
    // 0=Front, 1=Back, 2=Right, 3=Left, 4=Top, 5=Bottom
    public void setCubeColors(String[][] faceData) {
        for (Cubie cubie : cubies) {
            int gx = (int) Math.round(cubie.ox / (cubie.size + cubie.gap));
            int gy = (int) Math.round(cubie.oy / (cubie.size + cubie.gap));
            int gz = (int) Math.round(cubie.oz / (cubie.size + cubie.gap));

            // Front (Z = 1)
            if (gz == 1)  cubie.faceColors[0] = getColor(faceData[0][(gy + 1) * 3 + (gx + 1)]);
            // Back (Z = -1)
            if (gz == -1) cubie.faceColors[1] = getColor(faceData[1][(gy + 1) * 3 + (1 - gx)]);
            // Right (X = 1)
            if (gx == 1)  cubie.faceColors[2] = getColor(faceData[2][(gy + 1) * 3 + (1 - gz)]);
            // Left (X = -1)
            if (gx == -1) cubie.faceColors[3] = getColor(faceData[3][(gy + 1) * 3 + (gz + 1)]);
            // Top (Y = 1)
            if (gy == 1)  cubie.faceColors[4] = getColor(faceData[4][(gz + 1) * 3 + (gx + 1)]);
            // Bottom (Y = -1)
            if (gy == -1) cubie.faceColors[5] = getColor(faceData[5][(1 - gz) * 3 + (gx + 1)]);
        }
        repaint();
    }

    private Color getColor(String code) {
        switch (code.toLowerCase()) {
            case "w": return Color.WHITE;
            case "y": return Color.YELLOW;
            case "r": return new Color(183, 18, 52);
            case "o": return new Color(255, 88, 0);
            case "b": return new Color(0, 70, 173);
            case "g": return new Color(0, 155, 72);
            default:  return Color.BLACK;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isAutoRotating) {
            angleX += 0.005;
            angleY += 0.01;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // 1. Project all faces
        List<Face> allFaces = new ArrayList<>();
        for (Cubie cubie : cubies) {
            allFaces.addAll(cubie.getProjectedFaces(angleX, angleY, angleZ, centerX, centerY));
        }

        // 2. Sort by Z-depth
        Collections.sort(allFaces, (f1, f2) -> Double.compare(f2.avgZ, f1.avgZ));

        // 3. Render
        for (Face face : allFaces) {
            g2d.setColor(face.color);
            g2d.fill(face.path);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.draw(face.path);
        }

        g2d.setColor(Color.GRAY);
        g2d.drawString("Drag mouse to rotate", 20, 30);
    }

    private static class Cubie {
        double ox, oy, oz;
        double size = 50;
        double gap = 4;
        // Order: Front, Back, Right, Left, Top, Bottom
        Color[] faceColors = new Color[6];

        Cubie(double x, double y, double z) {
            this.ox = x * (size + gap);
            this.oy = y * (size + gap);
            this.oz = z * (size + gap);

            for (int i = 0; i < 6; i++) faceColors[i] = Color.BLACK;
        }

        List<Face> getProjectedFaces(double ax, double ay, double az, int cx, int cy) {
            List<Face> faces = new ArrayList<>();
            double s = size / 2.0;

            faces.add(createFace(new double[][]{{s,s,s}, {-s,s,s}, {-s,-s,s}, {s,-s,s}}, faceColors[0], ax, ay, az, cx, cy));  // Front
            faces.add(createFace(new double[][]{{s,s,-s}, {-s,s,-s}, {-s,-s,-s}, {s,-s,-s}}, faceColors[1], ax, ay, az, cx, cy)); // Back
            faces.add(createFace(new double[][]{{s,s,s}, {s,-s,s}, {s,-s,-s}, {s,s,-s}}, faceColors[2], ax, ay, az, cx, cy));    // Right
            faces.add(createFace(new double[][]{{-s,s,s}, {-s,-s,s}, {-s,-s,-s}, {-s,s,-s}}, faceColors[3], ax, ay, az, cx, cy)); // Left
            faces.add(createFace(new double[][]{{s,s,s}, {s,s,-s}, {-s,s,-s}, {-s,s,s}}, faceColors[4], ax, ay, az, cx, cy));     // Top
            faces.add(createFace(new double[][]{{s,-s,s}, {s,-s,-s}, {-s,-s,-s}, {-s,-s,s}}, faceColors[5], ax, ay, az, cx, cy)); // Bottom

            return faces;
        }

        private Face createFace(double[][] localVertices, Color color, double ax, double ay, double az, int cx, int cy) {
            Polygon poly = new Polygon();
            double totalZ = 0;

            for (double[] lv : localVertices) {
                double x = lv[0] + ox;
                double y = lv[1] + oy;
                double z = lv[2] + oz;

                // Rotation X
                double dy = y * Math.cos(ax) - z * Math.sin(ax);
                double dz = y * Math.sin(ax) + z * Math.cos(ax);
                y = dy; z = dz;

                // Rotation Y
                double dx = x * Math.cos(ay) + z * Math.sin(ay);
                dz = -x * Math.sin(ay) + z * Math.cos(ay);
                x = dx; z = dz;

                // Perspective projection
                double fov = 800;
                double viewDistance = 1000;
                double scale = fov / (viewDistance + z);

                poly.addPoint((int) (x * scale) + cx, (int) (y * scale) + cy);
                totalZ += z;
            }

            return new Face(poly, color, totalZ / 4.0);
        }
    }

    private static class Face {
        Polygon path;
        Color color;
        double avgZ;

        Face(Polygon path, Color color, double avgZ) {
            this.path = path;
            this.color = color;
            this.avgZ = avgZ;
        }
    }

    // ---------- Your professor-style show(String[][]) (kept) ----------
    public void show(String[][] faceData) {
        JFrame frame = new JFrame("Interactive 3D Rubik's Cube");
        RubiksCube cubeView = new RubiksCube();
        cubeView.setCubeColors(faceData);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(40, 40, 40));

        JButton UBtn = new JButton("U");
        UBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Step 1 renderer-only. Use show(Cube) for move buttons."));

        JButton DBtn = new JButton("D");
        DBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Step 1 renderer-only. Use show(Cube) for move buttons."));

        JButton RBtn = new JButton("R");
        RBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Step 1 renderer-only. Use show(Cube) for move buttons."));

        JButton LBtn = new JButton("L");
        LBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Step 1 renderer-only. Use show(Cube) for move buttons."));

        JButton FBtn = new JButton("F");
        FBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Step 1 renderer-only. Use show(Cube) for move buttons."));

        JButton BBtn = new JButton("B");
        BBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Step 1 renderer-only. Use show(Cube) for move buttons."));

        buttonPanel.add(UBtn);
        buttonPanel.add(DBtn);
        buttonPanel.add(RBtn);
        buttonPanel.add(LBtn);
        buttonPanel.add(FBtn);
        buttonPanel.add(BBtn);

        frame.setLayout(new BorderLayout());
        frame.add(cubeView, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ---------- NEW: professor-style buttons that actually do moves ----------
    public void show(Cube cube) {
        JFrame frame = new JFrame("Interactive 3D Rubik's Cube");
        RubiksCube cubeView = new RubiksCube();
        cubeView.setCubeColors(cube.toRendererFaceData());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(40, 40, 40));

        JButton UBtn = new JButton("U");
        UBtn.addActionListener(e -> {
            cube.applyMove("u");
            cubeView.setCubeColors(cube.toRendererFaceData());
        });

        JButton DBtn = new JButton("D");
        DBtn.addActionListener(e -> {
            cube.applyMove("d");
            cubeView.setCubeColors(cube.toRendererFaceData());
        });

        JButton RBtn = new JButton("R");
        RBtn.addActionListener(e -> {
            cube.applyMove("r");
            cubeView.setCubeColors(cube.toRendererFaceData());
        });

        JButton LBtn = new JButton("L");
        LBtn.addActionListener(e -> {
            cube.applyMove("l");
            cubeView.setCubeColors(cube.toRendererFaceData());
        });

        JButton FBtn = new JButton("F");
        FBtn.addActionListener(e -> {
            cube.applyMove("f");
            cubeView.setCubeColors(cube.toRendererFaceData());
        });

        JButton BBtn = new JButton("B");
        BBtn.addActionListener(e -> {
            cube.applyMove("b");
            cubeView.setCubeColors(cube.toRendererFaceData());
        });

        buttonPanel.add(UBtn);
        buttonPanel.add(DBtn);
        buttonPanel.add(RBtn);
        buttonPanel.add(LBtn);
        buttonPanel.add(FBtn);
        buttonPanel.add(BBtn);

        frame.setLayout(new BorderLayout());
        frame.add(cubeView, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}