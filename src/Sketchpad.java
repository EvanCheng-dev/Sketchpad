import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Sketchpad extends JFrame {
    private static final long serialVersionUID = 1L;
    private DrawingArea drawingArea;
    private JButton freehandButton, lineButton, rectangleButton, ellipseButton, polygonButton;
    private JButton selectButton, colorButton, moveButton, cutButton, copyButton, pasteButton, undoButton, saveButton;

    public Sketchpad() {
        setTitle("Sketchpad");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        drawingArea = new DrawingArea();
        add(drawingArea, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        freehandButton = new JButton("Freehand");
        lineButton = new JButton("Line");
        rectangleButton = new JButton("Rectangle");
        ellipseButton = new JButton("Ellipse");
        polygonButton = new JButton("Polygon");
        selectButton = new JButton("Select");
        colorButton = new JButton("Color");
        moveButton = new JButton("Move");
        cutButton = new JButton("Cut");
        copyButton = new JButton("Copy");
        pasteButton = new JButton("Paste");
        undoButton = new JButton("Undo");
        saveButton = new JButton("Save");

        buttonPanel.add(freehandButton);
        buttonPanel.add(lineButton);
        buttonPanel.add(rectangleButton);
        buttonPanel.add(ellipseButton);
        buttonPanel.add(polygonButton);
        buttonPanel.add(selectButton);
        buttonPanel.add(colorButton);
        buttonPanel.add(moveButton);
        buttonPanel.add(cutButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(pasteButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.NORTH);

        freehandButton.addActionListener(e -> drawingArea.setMode(DrawingMode.FREEHAND));
        lineButton.addActionListener(e -> drawingArea.setMode(DrawingMode.LINE));
        rectangleButton.addActionListener(e -> drawingArea.setMode(DrawingMode.RECTANGLE));
        ellipseButton.addActionListener(e -> drawingArea.setMode(DrawingMode.ELLIPSE));
        polygonButton.addActionListener(e -> drawingArea.setMode(DrawingMode.POLYGON));
        selectButton.addActionListener(e -> drawingArea.setMode(DrawingMode.SELECT));
        colorButton.addActionListener(e -> drawingArea.chooseColor());
        moveButton.addActionListener(e -> drawingArea.setEditMode(EditMode.MOVE));
        cutButton.addActionListener(e -> drawingArea.setEditMode(EditMode.CUT));
        copyButton.addActionListener(e -> drawingArea.setEditMode(EditMode.COPY));
        pasteButton.addActionListener(e -> drawingArea.setEditMode(EditMode.PASTE));
        undoButton.addActionListener(e -> drawingArea.undo());
        saveButton.addActionListener(e -> drawingArea.saveImage());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Sketchpad sketchpad = new Sketchpad();
            sketchpad.setVisible(true);
        });
    }
}