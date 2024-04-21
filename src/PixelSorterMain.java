/**
 * PixelSorterMain.java
 * 
 * This class implements a UI for sorting pixels in an image based on their color intensities
 * and HSV values to create visually appealing sorted effects on images.
 *
 * @author Citizen Cyrus
 * @version 1.0
 * @date 2024-04-21
 * @file PixelSorterMain.java
 * @license The Unlicense
 */
package imagetools;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;

public class PixelSorterMain extends Application {
    private final ImageView imageView = new ImageView();
    private final Button btnSave = new Button("Save Image");
    private final Button btnLoad = new Button("Load Image");
    private final Button btnSort = new Button("Start Sorting");

    /**
     * Initializes the primary stage of the application, setting up the user interface,
     * configuring buttons, and preparing the event handlers.
     *
     * @param primaryStage The main stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Pixel Sorter");

        FileChooser fileChooser = createFileChooser();

        btnSave.setDisable(true);
        btnSort.setDisable(true);

        btnLoad.setOnAction(e -> loadAndDisplayImage(fileChooser, primaryStage));
        btnSave.setOnAction(e -> saveImage(primaryStage));
        btnSort.setOnAction(e -> startSorting());

        imageView.setPreserveRatio(true);
        imageView.setFitHeight(400);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(btnLoad, btnSort, btnSave);

        VBox root = new VBox(10);
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().addAll(buttonBox, imageView);

        Scene scene = new Scene(root, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates and configures a FileChooser with specific file extension filters.
     * This setup helps ensure that users can only select image files, which are
     * necessary for the pixel sorting process.
     *
     * @return A configured FileChooser with appropriate filters.
     */
    private FileChooser createFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PNG Images", "*.png"),
            new FileChooser.ExtensionFilter("JPG Images", "*.jpg"),
            new FileChooser.ExtensionFilter("BMP Images", "*.bmp")
        );
        return fileChooser;
    }

    /**
     * Loads an image from a file chosen by the user and displays it immediately.
     * Enables further user interactions such as starting the pixel sorting process
     * or saving the image after it's loaded.
     *
     * @param fileChooser The FileChooser to use for selecting an image.
     * @param primaryStage The primary stage of the application to display errors if needed.
     */
    private void loadAndDisplayImage(FileChooser fileChooser, Stage primaryStage) {
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                BufferedImage loadedImage = ImageIO.read(file);
                Image displayImage = SwingFXUtils.toFXImage(loadedImage, null);
                imageView.setImage(displayImage);
                btnSave.setDisable(true);
                btnSort.setDisable(false);
            } catch (IOException ex) {
                showErrorDialog(primaryStage, "Error loading image", "Failed to load or process the image.");
                btnSave.setDisable(true);
                btnSort.setDisable(true);
            }
        }
    }

    /**
     * Initiates the pixel sorting process on the currently displayed image.
     * This method retrieves the displayed image, applies the sorting algorithm,
     * and updates the display with the sorted image.
     */
    private void startSorting() {
        try {
            BufferedImage loadedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            BufferedImage sortedImage = ImagePixelSorter.processImage(loadedImage);
            Image displayImage = SwingFXUtils.toFXImage(sortedImage, null);
            imageView.setImage(displayImage);
            btnSave.setDisable(false);
        } catch (Exception ex) {
            Alert alert = new Alert(AlertType.ERROR, "Failed to sort the image.");
            alert.showAndWait();
        }
    }

    /**
     * Saves the currently displayed image to a location chosen by the user.
     * This function is activated after the user has performed any necessary
     * modifications to the image and wishes to preserve the result.
     *
     * @param primaryStage The primary stage of the application to display dialog.
     */
    private void saveImage(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null && imageView.getImage() != null) {
            File outputFile = new File(selectedDirectory.getAbsolutePath(), "sorted-image.png");
            try {
                BufferedImage bImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                ImageIO.write(bImage, "PNG", outputFile);
                Alert alert = new Alert(AlertType.INFORMATION, "Image saved successfully!");
                alert.showAndWait();
            } catch (IOException ex) {
                showErrorDialog(primaryStage, "Save Error", "Failed to save the image.");
            }
        }
    }

    /**
     * Displays an error dialog with a specified message.
     * This method helps to inform the user about issues during the application's operation,
     * such as failures in loading, processing, or saving images.
     *
     * @param owner The window that owns this dialog.
     * @param title The title of the dialog.
     * @param content The message to display in the dialog.
     */
    private void showErrorDialog(Stage owner, String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
