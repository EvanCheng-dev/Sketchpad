# Sketchpad

## Overview

Sketchpad is a simple Java-based drawing project for ECE 9020. 
That allows users to create, manipulate and save various shapes. The application features common drawing tools such as freehand drawing, lines, rectangles, ellipses, polygons. It also includes selection, cut, copy, paste, move, undo and save operations.

## Features

- **Drawing Tools**: 
  - Freehand drawing
  - Straight lines
  - Rectangles
  - Ellipses
  - Polygons (triangle, pentagon, hexagon)

- **Editing Tools**:
  - Select objects
  - Move objects
  - Cut and paste objects
  - Copy and paste objects
  - Undo actions
  
- **Color Picker**: Choose different colors for drawing.
  
- **Save Functionality**: Save the current drawing as a PNG image.

## Usage

### Controls

- **Freehand Drawing**: Select the `Freehand` button and draw freely on the canvas.
- **Lines**: Select the `Line` button and click and drag to draw a straight line.
- **Rectangles**: Select the `Rectangle` button and click and drag to draw a rectangle.
- **Ellipses**: Select the `Ellipse` button and click and drag to draw an ellipse.
- **Polygons**: Select the `Polygon` button and choose the desired polygon shape (triangle, pentagon, hexagon), then click and drag to draw it.
- **Select Objects**: Select the `Select` button and click and drag to create a selection box around the shapes you want to select.
- **Move Objects**: After selecting objects, click `Move` and drag the selected objects to a new location.
- **Cut, Copy, Paste**: 
  - Select objects using the `Select` button.
  - Use the `Cut` or `Copy` button to cut or copy the selected objects.
  - Use the `Paste` button to paste the copied/cut objects onto the canvas.
- **Undo**: Use the `Undo` button to revert the last action.
- **Color Picker**: Select the `Color` button to choose a new color for drawing.
- **Save**: Select the `Save` button to save the current drawing as a PNG file.

### Saving Your Drawing

1. Click on the `Save` button.
2. A file dialog will appear, prompting you to select the location and name for the saved image.
3. The image will be saved as a PNG file in the chosen directory.

## Installation

To run Sketchpad, you need to have Java Development Kit (JDK) installed on your system.

### Steps:

1. **Clone or Download the Project**: 
   - Clone the repository using `git clone https://github.com/EvanCheng-dev/Sketchpad`
   - Or download the ZIP and extract it.

2. **Compile the Code**:
   - Open a terminal or command prompt.
   - Navigate to the project directory.
   - Compile the Java files:
     ```sh
     javac Sketchpad.java DrawingArea.java
     ```

3. **Run the Application**:
   - After compilation, run the application:
     ```sh
     java Sketchpad
     ```

## Requirements

- **Java Development Kit (JDK) 8 or later**: Ensure that Java is properly installed and configured on your system.

## License

This project is licensed under the MIT License.

## Contributing

This project is contributed by Yufeng Cheng (yche3724@uwo.ca).
Piracy will be prosecuted.
