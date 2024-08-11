import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Stack;

enum DrawingMode { FREEHAND, LINE, RECTANGLE, ELLIPSE, POLYGON, SELECT }
enum EditMode { NONE, MOVE, CUT, COPY, PASTE }

class DrawingArea extends JPanel {
    private static final long serialVersionUID = 1L;
    private DrawingMode mode;
    private EditMode editMode;
    private Color currentColor;
    private ArrayList<ColoredShape> shapes;
    private Stack<ArrayList<ColoredShape>> undoStack; // Stack for undo functionality
    private Shape currentShape;
    private int polygonSides;
    private boolean polygonSelected;
    private ArrayList<ColoredShape> selectedShapes;
    private ArrayList<ShapeWithOffset> copiedShapesWithOffsets;
    private Rectangle2D selectionBox;
    private Point startPoint;

    public DrawingArea() {
        shapes = new ArrayList<>();
        undoStack = new Stack<>(); // Initialize the undo stack
        selectedShapes = new ArrayList<>();
        copiedShapesWithOffsets = new ArrayList<>();
        mode = DrawingMode.FREEHAND;
        editMode = EditMode.NONE;
        currentColor = Color.BLACK;
        polygonSelected = false;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                if (editMode == EditMode.NONE) {
                    switch (mode) {
                        case FREEHAND:
                            currentShape = new Path2D.Double();
                            ((Path2D) currentShape).moveTo(startPoint.x, startPoint.y);
                            break;
                        case LINE:
                            currentShape = new Line2D.Double(startPoint, startPoint);
                            break;
                        case RECTANGLE:
                            currentShape = new Rectangle2D.Double(startPoint.x, startPoint.y, 0, 0);
                            break;
                        case ELLIPSE:
                            currentShape = new Ellipse2D.Double(startPoint.x, startPoint.y, 0, 0);
                            break;
                        case POLYGON:
                            if (!polygonSelected) {
                                showPolygonDialog(startPoint);
                                polygonSelected = true;
                            }
                            break;
                        case SELECT:
                            selectionBox = new Rectangle2D.Double(startPoint.x, startPoint.y, 0, 0);
                            selectedShapes.clear();
                            break;
                    }
                } else if (editMode == EditMode.MOVE || editMode == EditMode.COPY || editMode == EditMode.CUT) {
                    if (!selectedShapes.isEmpty()) {
                        saveStateToUndoStack(); // Save the current state before modifying
                        if (editMode == EditMode.CUT) {
                            shapes.removeAll(selectedShapes);
                            selectedShapes.clear();
                            selectionBox = null;
                            repaint();
                        } else if (editMode == EditMode.COPY) {
                            copiedShapesWithOffsets.clear();
                            Rectangle2D bounds = selectionBox.getBounds2D();
                            for (ColoredShape shape : selectedShapes) {
                                Point2D offset = new Point2D.Double(
                                        shape.getShape().getBounds2D().getX() - bounds.getX(),
                                        shape.getShape().getBounds2D().getY() - bounds.getY()
                                );
                                copiedShapesWithOffsets.add(new ShapeWithOffset(shape.getShape(), shape.getColor(), offset));
                            }
                        }
                    }
                } else if (editMode == EditMode.PASTE && !copiedShapesWithOffsets.isEmpty()) {
                    saveStateToUndoStack(); // Save the current state before modifying
                    selectedShapes.clear(); // Clear previously selected shapes
                    for (ShapeWithOffset shapeWithOffset : copiedShapesWithOffsets) {
                        Point2D newLocation = new Point2D.Double(
                                startPoint.x + shapeWithOffset.offset.getX(),
                                startPoint.y + shapeWithOffset.offset.getY()
                        );
                        Shape newShape = copyShape(shapeWithOffset.shape, newLocation);
                        ColoredShape newColoredShape = new ColoredShape(newShape, shapeWithOffset.color);
                        shapes.add(newColoredShape);
                        selectedShapes.add(newColoredShape); // Automatically select pasted shapes
                    }
                    selectionBox = createSelectionBox(new Rectangle2D.Double(startPoint.x, startPoint.y, 0, 0));
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point endPoint = e.getPoint();
                if (editMode == EditMode.NONE) {
                    switch (mode) {
                        case FREEHAND:
                            ((Path2D) currentShape).lineTo(endPoint.x, endPoint.y);
                            break;
                        case LINE:
                            ((Line2D) currentShape).setLine(startPoint, endPoint);
                            break;
                        case RECTANGLE:
                            ((Rectangle2D) currentShape).setFrameFromDiagonal(startPoint, endPoint);
                            break;
                        case ELLIPSE:
                            ((Ellipse2D) currentShape).setFrameFromDiagonal(startPoint, endPoint);
                            break;
                        case POLYGON:
                            int radius = (int) startPoint.distance(endPoint);
                            currentShape = createRegularPolygon(startPoint, polygonSides, radius);
                            break;
                        case SELECT:
                            selectionBox.setFrameFromDiagonal(startPoint, endPoint);
                            break;
                    }
                    repaint();
                } else if (editMode == EditMode.MOVE && !selectedShapes.isEmpty()) {
                    double dx = endPoint.getX() - startPoint.getX();
                    double dy = endPoint.getY() - startPoint.getY();
                    for (ColoredShape shape : selectedShapes) {
                        Shape s = shape.getShape();
                        AffineTransform transform = AffineTransform.getTranslateInstance(dx, dy);
                        s = transform.createTransformedShape(s);
                        shape.setShape(s);
                    }
                    selectionBox.setFrameFromDiagonal(
                            selectionBox.getX() + dx,
                            selectionBox.getY() + dy,
                            selectionBox.getMaxX() + dx,
                            selectionBox.getMaxY() + dy
                    );
                    startPoint = endPoint;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (editMode == EditMode.NONE) {
                    if (currentShape != null && mode != DrawingMode.SELECT) {
                        saveStateToUndoStack(); // Save the current state before modifying
                        shapes.add(new ColoredShape(currentShape, currentColor));
                        currentShape = null;
                    } else if (mode == DrawingMode.SELECT) {
                        selectShapesInBox();
                    }
                    polygonSelected = false;  // Reset the polygon selection flag
                    repaint();
                } else if (editMode == EditMode.MOVE && !selectedShapes.isEmpty()) {
                    saveStateToUndoStack(); // Save the current state after the move operation
                    repaint();
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        // Add key listener for deselecting shapes and undo functionality
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    clearSelection();
                } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    undo();
                }
            }
        });
        setFocusable(true);
    }

    // Method to save the current drawing to an image file
    public void saveImage() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        paint(g2d);
        g2d.dispose();

        // Choose where to save the image
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ImageIO.write(image, "png", new File(fileToSave.getAbsolutePath() + ".png"));
                JOptionPane.showMessageDialog(this, "Image saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage());
            }
        }
    }

    private void saveStateToUndoStack() {
        // Save the current state of the shapes to the undo stack
        ArrayList<ColoredShape> state = new ArrayList<>();
        for (ColoredShape shape : shapes) {
            // Deep copy the shape, ensuring that its position and properties are preserved
            state.add(shape.deepCopy());
        }
        undoStack.push(state);
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            shapes = undoStack.pop();
            selectedShapes.clear();  // Clear any selection
            selectionBox = null;     // Clear the selection box
            repaint();
        }
    }

    private void clearSelection() {
        selectedShapes.clear();
        selectionBox = null;
        repaint();
    }

    private void selectShapesInBox() {
        selectedShapes.clear();
        for (ColoredShape shape : shapes) {
            if (selectionBox.intersects(shape.getShape().getBounds2D())) {
                selectedShapes.add(shape);
            }
        }
    }

    public void setMode(DrawingMode mode) {
        this.mode = mode;
        this.editMode = EditMode.NONE;
    }

    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    public void chooseColor() {
        Color newColor = JColorChooser.showDialog(this, "Choose a color", currentColor);
        if (newColor != null) {
            currentColor = newColor;
        }
    }
    private Shape copyShape(Shape shape, Point2D newLocation) {
        if (newLocation == null) {
            // Return a copy of the shape without any transformation if no new location is provided
            return (Shape) ((Path2D) shape).clone();
        }

        Rectangle bounds = shape.getBounds();
        double dx = newLocation.getX() - bounds.getX();
        double dy = newLocation.getY() - bounds.getY();
        AffineTransform transform = AffineTransform.getTranslateInstance(dx, dy);
        return transform.createTransformedShape(shape);
    }

    private Rectangle2D createSelectionBox(Rectangle2D bounds) {
        return new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    private void showPolygonDialog(Point startPoint) {
        String[] options = {"Triangle", "Pentagon", "Hexagon"};
        int choice = JOptionPane.showOptionDialog(this, "Choose a polygon type", "Polygon Selector",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            polygonSides = 3; // Triangle
        } else if (choice == 1) {
            polygonSides = 5; // Pentagon
        } else if (choice == 2) {
            polygonSides = 6; // Hexagon
        }
    }

    private Shape createRegularPolygon(Point center, int sides, int radius) {
        Polygon polygon = new Polygon();
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            int x = (int) (center.x + radius * Math.cos(angle));
            int y = (int) (center.y + radius * Math.sin(angle));
            polygon.addPoint(x, y);
        }
        return polygon;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        for (ColoredShape coloredShape : shapes) {
            g2.setColor(coloredShape.getColor());
            g2.draw(coloredShape.getShape());
        }
        if (currentShape != null) {
            g2.setColor(currentColor);
            g2.draw(currentShape);
        }
        if (selectionBox != null) {
            g2.setColor(Color.GRAY);
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{10.0f}, 0.0f));
            g2.draw(selectionBox);
        }
    }
}

class ColoredShape {
    private Shape shape;
    private Color color;

    public ColoredShape(Shape shape, Color color) {
        this.shape = shape;
        this.color = color;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public Color getColor() {
        return color;
    }

    public ColoredShape deepCopy() {
        Shape copiedShape = null;
        if (shape instanceof Path2D) {
            copiedShape = (Shape) ((Path2D) shape).clone();
        } else if (shape instanceof Rectangle2D) {
            copiedShape = (Shape) ((Rectangle2D) shape).clone();
        } else if (shape instanceof Ellipse2D) {
            copiedShape = (Shape) ((Ellipse2D) shape).clone();
        } else if (shape instanceof Line2D) {
            copiedShape = (Shape) ((Line2D) shape).clone();
        } else if (shape instanceof Polygon) {
            copiedShape = new Polygon(((Polygon) shape).xpoints, ((Polygon) shape).ypoints, ((Polygon) shape).npoints);
        }
        return new ColoredShape(copiedShape, new Color(color.getRGB()));
    }
}

class ShapeWithOffset {
    public Shape shape;
    public Color color;
    public Point2D offset;

    public ShapeWithOffset(Shape shape, Color color, Point2D offset) {
        this.shape = shape;
        this.color = color;
        this.offset = offset;
    }
}