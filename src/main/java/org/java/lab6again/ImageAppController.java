package org.java.lab6again;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class ImageAppController {

    private ImageView originalImageView = new ImageView();
    private ImageView processedImageView = new ImageView();
    private ComboBox<String> operationBox = new ComboBox<>();
    private Button executeButton = new Button("Wykonaj");
    private Button loadButton = new Button("Wczytaj obraz");
    private Button saveButton = new Button("Zapisz obraz");
    private Button scaleButton = new Button("Skaluj obraz");
    private Button rotateLeftButton = new Button("\u21ba");
    private Button rotateRightButton = new Button("\u21bb");
    private boolean operationApplied = false;
    private static final Logger logger = Logger.getLogger(ImageAppController.class.getName());
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private File loadedFile = null;
    private double originalWidth = 0;
    private double originalHeight = 0;

    public void initialize(Stage stage) {
        setupLogger();
        logger.info("Uruchomiono aplikację.");
        stage.setOnCloseRequest(event -> {
            logger.info("Zamknięto aplikację.");
            executor.shutdown(); // zamknięcie wątków
        });

        Image logo = new Image("logo.png");
        ImageView logoView = new ImageView(logo);
        logoView.setFitHeight(40);
        logoView.setPreserveRatio(true);

        Label headerText = new Label("Image Filter App");
        headerText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        HBox header = new HBox(10, logoView, headerText);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #e0e0e0;");

        Label subtitle = new Label("Cześć! Miłego dnia :)");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        VBox top = new VBox(5, header, subtitle);

        operationBox.getItems().addAll("Negatyw", "Progowanie", "Konturowanie");
        operationBox.setPromptText("Wybierz operację");
        operationBox.setPrefWidth(200);

        executeButton.setDisable(true);
        executeButton.setOnAction(e -> {
            String selected = operationBox.getValue();
            if (selected == null) {
                showToast("Nie wybrano operacji do wykonania", Alert.AlertType.WARNING);
                return;
            }

            Image sourceImage = originalImageView.getImage();
            if (sourceImage == null) {
                showToast("Brak obrazu do przetworzenia", Alert.AlertType.ERROR);
                return;
            }

            try {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(sourceImage, null);
                switch (selected) {
                    case "Negatyw":
                        applyNegative();
                        break;
                    case "Progowanie":
                        showThresholdDialog();
                        break;
                    case "Konturowanie":
                        applyEdgeDetection();
                        break;
                    default:
                        showToast("Operacja '" + selected + "' nie jest jeszcze zaimplementowana", Alert.AlertType.WARNING);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                showToast("Nie udało się wykonać operacji.", Alert.AlertType.ERROR);
                logger.log(Level.SEVERE, "Błąd podczas operacji: " + ex);
            }
        });
        loadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Wybierz obraz JPG");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki JPG", "*.jpg"));
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                try {
                    if (!selectedFile.getName().toLowerCase().endsWith(".jpg")) {
                        showToast("Niedozwolony format pliku", Alert.AlertType.ERROR);
                        logger.log(Level.SEVERE, "Wybrano niedozwolony format pliku.");
                        return;
                    }

                    Image img = new Image(new FileInputStream(selectedFile));

                    originalImageView.setImage(img);
                    originalImageView.setFitWidth(300);
                    originalImageView.setPreserveRatio(true);

                    processedImageView.setImage(null);

                    originalWidth = img.getWidth();
                    originalHeight = img.getHeight();

                    loadedFile = selectedFile;

                    executeButton.setDisable(false);
                    saveButton.setDisable(false);
                    scaleButton.setDisable(false);
                    rotateLeftButton.setDisable(false);
                    rotateRightButton.setDisable(false);

                    showToast("Pomyślnie załadowano plik", Alert.AlertType.INFORMATION);
                    logger.info("Wykonano operację: Załadowanie obrazu");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showToast("Nie udało się załadować pliku", Alert.AlertType.ERROR);
                    logger.log(Level.SEVERE, "Nie udało się załadować pliku: " + e);
                }
            }
        });


        saveButton.setDisable(true);
        saveButton.setOnAction(e -> showSaveDialog(stage));

        scaleButton.setDisable(true);
        scaleButton.setOnAction(e -> showScaleDialog());

        rotateLeftButton.setDisable(true);
        rotateRightButton.setDisable(true);

        rotateLeftButton.setOnAction(e -> rotateImage(-90));
        rotateRightButton.setOnAction(e -> rotateImage(90));

        HBox controls = new HBox(10, loadButton, operationBox, executeButton, saveButton, scaleButton, rotateLeftButton, rotateRightButton);
        controls.setPadding(new Insets(10));

        Label originalLabel = new Label("Oryginalny obraz:");
        Label processedLabel = new Label("Po przetworzeniu:");

        VBox imageBox = new VBox(10, originalLabel, originalImageView, processedLabel, processedImageView);
        imageBox.setPadding(new Insets(10));

        VBox mainContent = new VBox(10, controls, imageBox);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(mainContent);

        Label footer = new Label("Julia Kucharska");
        footer.setPadding(new Insets(10));
        footer.setStyle("-fx-background-color: #e0e0e0; -fx-font-size: 12px; -fx-text-fill: #333333;");
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setAlignment(javafx.geometry.Pos.CENTER);

        root.setBottom(footer);

        Scene scene = new Scene(root, 900, 700);
        stage.setTitle("Image Filter App");
        stage.setScene(scene);
        stage.show();
    }

    private void rotateImage(int angle) {
        Image source = processedImageView.getImage();
        if (source == null) source = originalImageView.getImage();
        if (source == null) {
            showToast("Brak obrazu do obrócenia", Alert.AlertType.ERROR);
            logger.log(Level.SEVERE, "Brak obrazu do obrócenia");
            return;
        }
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(source, null);
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        BufferedImage rotatedImage = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (angle == 90) rotatedImage.setRGB(h - y - 1, x, bufferedImage.getRGB(x, y));
                else if (angle == -90) rotatedImage.setRGB(y, w - x - 1, bufferedImage.getRGB(x, y));
            }
        }
        Image fxRotated = SwingFXUtils.toFXImage(rotatedImage, null);
        processedImageView.setImage(fxRotated);
        operationApplied = true;
        logger.info("Wykonano operację: Obrót obrazu");
    }

    private void showToast(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Komunikat");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSaveDialog(Stage stage) {
        if (!operationApplied) {
            showToast("Na pliku nie zostały wykonane żadne operacje!", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz obraz jako");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki JPG", "*.jpg"));

        Path picturesDir = javax.swing.filechooser.FileSystemView.getFileSystemView()
                .getDefaultDirectory().toPath().getParent();
        File defaultPicturesFolder = picturesDir.resolve("Pictures").toFile();
        if (defaultPicturesFolder.exists()) {
            fileChooser.setInitialDirectory(defaultPicturesFolder);
        } else {
            fileChooser.setInitialDirectory(picturesDir.toFile());
        }

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            String filename = file.getName();

            // Dodaj rozszerzenie .jpg jeśli brak
            if (!filename.toLowerCase().endsWith(".jpg")) {
                file = new File(file.getAbsolutePath() + ".jpg");
                filename += ".jpg";
            }

            // Walidacja długości nazwy (bez ścieżki)
            String nameWithoutExtension = filename.replaceFirst("[.][^.]+$", ""); // usuwa rozszerzenie
            if (nameWithoutExtension.length() < 3 || nameWithoutExtension.length() > 100) {
                showToast("Nazwa pliku musi mieć od 3 do 100 znaków!", Alert.AlertType.ERROR);
                return;
            }

            if (processedImageView.getImage() == null) {
                showToast("Brak obrazu do zapisania", Alert.AlertType.ERROR);
                return;
            }

            try {
                WritableImage image = processedImageView.snapshot(null, null);
                BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bImage, "jpg", file);
                showToast("Obraz zapisany pomyślnie", Alert.AlertType.INFORMATION);
                logger.info("Wykonano operację: Zapisano obraz jako: " + filename);
            } catch (IOException e) {
                showToast("Nie udało się zapisać obrazu", Alert.AlertType.ERROR);
                logger.log(Level.SEVERE, "Błąd podczas zapisywania obrazu: ", e);
            }
        }
    }


    private void showScaleDialog() {
        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("Skaluj obraz");

        Label widthLabel = new Label("Szerokość:");
        TextField widthField = new TextField();
        Label heightLabel = new Label("Wysokość:");
        TextField heightField = new TextField();

        VBox content = new VBox(10, widthLabel, widthField, heightLabel, heightField);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Skaluj", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                try {
                    int w = Integer.parseInt(widthField.getText());
                    int h = Integer.parseInt(heightField.getText());
                    if (w < 0 || h < 0) {
                        showToast("Wymiary nie mogą być ujemne!", Alert.AlertType.ERROR);
                        return null;
                    } else if (w > 3000 || h > 3000) {
                        showToast("Wymiary nie mogą przekraczać 3000!", Alert.AlertType.ERROR);
                        return null;
                    }
                    return new int[]{w, h};
                } catch (NumberFormatException e) {
                    showToast("Wprowadź poprawne wymiary", Alert.AlertType.ERROR);
                    logger.log(Level.SEVERE, "Błąd podczas skalowania: " + e);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            Image source = processedImageView.getImage();
            if (source == null) source = originalImageView.getImage();
            if (source == null) {
                showToast("Brak obrazu do skalowania", Alert.AlertType.ERROR);
                logger.log(Level.SEVERE, "Brak obrazu do skalowania. ");
                return;
            }
            BufferedImage buffered = SwingFXUtils.fromFXImage(source, null);
            java.awt.Image tmp = buffered.getScaledInstance(result[0], result[1], java.awt.Image.SCALE_SMOOTH);
            BufferedImage scaled = new BufferedImage(result[0], result[1], BufferedImage.TYPE_INT_RGB);
            scaled.getGraphics().drawImage(tmp, 0, 0, null);
            processedImageView.setImage(SwingFXUtils.toFXImage(scaled, null));
            operationApplied = true;
            logger.info("Wykonano operację: Skalowanie");
        });
    }

    private void showThresholdDialog() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Progowanie");

        Label label = new Label("Podaj próg (0-255):");
        Spinner<Integer> spinner = new Spinner<>(0, 255, 128);
        spinner.setEditable(true);

        VBox content = new VBox(10, label, spinner);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Wykonaj progowanie", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                return spinner.getValue();
            }
            return null;
        });
        dialog.showAndWait().ifPresent(threshold -> applyThreshold(threshold));
    }

    private void applyThreshold(int threshold) {
        Image source = originalImageView.getImage();
        if (source == null) {
            showToast("Brak obrazu do przetworzenia", Alert.AlertType.ERROR);
            return;
        }

        BufferedImage buffered = SwingFXUtils.fromFXImage(source, null);
        int width = buffered.getWidth();
        int height = buffered.getHeight();
        int numThreads = 4;
        int chunkHeight = height / numThreads;

        Runnable[] tasks = new Runnable[numThreads];
        for (int i = 0; i < numThreads; i++) {
            final int startY = i * chunkHeight;
            final int endY = (i == numThreads - 1) ? height : (i + 1) * chunkHeight;

            tasks[i] = () -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgba = buffered.getRGB(x, y);
                        int r = (rgba >> 16) & 0xff;
                        int g = (rgba >> 8) & 0xff;
                        int b = rgba & 0xff;
                        int gray = (r + g + b) / 3;
                        int binary = gray < threshold ? 0 : 255;
                        int newPixel = (0xff << 24) | (binary << 16) | (binary << 8) | binary;
                        buffered.setRGB(x, y, newPixel);
                    }
                }
            };
        }

        executor.submit(() -> {
            try {
                for (Runnable task : tasks) executor.submit(task);
                executor.submit(() -> javafx.application.Platform.runLater(() -> {
                    processedImageView.setImage(SwingFXUtils.toFXImage(buffered, null));
                    operationApplied = true;
                    showToast("Progowanie zostało przeprowadzone pomyślnie!", Alert.AlertType.INFORMATION);
                    logger.info("Wykonano operację: Progowanie");
                }));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Błąd podczas progowania: " + e);
                javafx.application.Platform.runLater(() -> showToast("Nie udało się wykonać progowania.", Alert.AlertType.ERROR));
            }
        });
    }


    private void applyNegative() {
        Image source = originalImageView.getImage();
        if (source == null) {
            showToast("Brak obrazu do przetworzenia", Alert.AlertType.ERROR);
            logger.warning("Próba zastosowania negatywu bez załadowanego obrazu.");
            return;
        }

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(source, null);
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();
        int numThreads = 4;
        int chunkHeight = height / numThreads;

        Runnable[] tasks = new Runnable[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int startY = i * chunkHeight;
            final int endY = (i == numThreads - 1) ? height : (i + 1) * chunkHeight;
            tasks[i] = () -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgba = bufferedImage.getRGB(x, y);
                        int a = (rgba >> 24) & 0xff;
                        int r = 255 - ((rgba >> 16) & 0xff);
                        int g = 255 - ((rgba >> 8) & 0xff);
                        int b = 255 - (rgba & 0xff);
                        int neg = (a << 24) | (r << 16) | (g << 8) | b;
                        bufferedImage.setRGB(x, y, neg);
                    }
                }
            };
        }

        executor.submit(() -> {
            try {
                for (Runnable task : tasks) executor.submit(task);
                executor.submit(() -> javafx.application.Platform.runLater(() -> {
                    processedImageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
                    operationApplied = true;
                    showToast("Negatyw został wygenerowany pomyślnie!", Alert.AlertType.INFORMATION);
                    logger.info("Wykonano operację: Negatyw");
                }));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Błąd przy operacji negatywu", e);
                javafx.application.Platform.runLater(() -> showToast("Nie udało się wykonać negatywu.", Alert.AlertType.ERROR));
            }
        });
    }


    private void applyEdgeDetection() {
        Image source = originalImageView.getImage();
        if (source == null) {
            showToast("Brak obrazu do przetworzenia", Alert.AlertType.ERROR);
            return;
        }

        try {
            BufferedImage src = SwingFXUtils.fromFXImage(source, null);
            int width = src.getWidth();
            int height = src.getHeight();
            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            int numThreads = 4;
            Thread[] threads = new Thread[numThreads];
            int chunkHeight = height / numThreads;

            for (int i = 0; i < numThreads; i++) {
                final int startY = Math.max(1, i * chunkHeight);
                final int endY = (i == numThreads - 1) ? height - 1 : Math.min(height - 1, (i + 1) * chunkHeight);

                threads[i] = new Thread(() -> {
                    for (int y = startY; y < endY; y++) {
                        for (int x = 1; x < width - 1; x++) {
                            int rgb = src.getRGB(x, y);
                            int r = (rgb >> 16) & 0xff;
                            int g = (rgb >> 8) & 0xff;
                            int b = rgb & 0xff;
                            int gray = (r + g + b) / 3;

                            int rgbRight = src.getRGB(x + 1, y);
                            int rR = (rgbRight >> 16) & 0xff;
                            int gR = (rgbRight >> 8) & 0xff;
                            int bR = rgbRight & 0xff;
                            int grayRight = (rR + gR + bR) / 3;

                            int rgbDown = src.getRGB(x, y + 1);
                            int rD = (rgbDown >> 16) & 0xff;
                            int gD = (rgbDown >> 8) & 0xff;
                            int bD = rgbDown & 0xff;
                            int grayDown = (rD + gD + bD) / 3;

                            int dx = gray - grayRight;
                            int dy = gray - grayDown;
                            int magnitude = Math.min(255, Math.abs(dx) + Math.abs(dy));

                            int edgeColor = (0xff << 24) | (magnitude << 16) | (magnitude << 8) | magnitude;
                            result.setRGB(x, y, edgeColor);
                        }
                    }
                });
                threads[i].start();
            }

            for (Thread t : threads) {
                t.join();
            }

            processedImageView.setImage(SwingFXUtils.toFXImage(result, null));
            operationApplied = true;
            showToast("Konturowanie zostało zakończone pomyślnie!", Alert.AlertType.INFORMATION);
            logger.info("Wykonano operację: Konturowanie");
        } catch (Exception e) {
            showToast("Błąd podczas konturowania.", Alert.AlertType.ERROR);
            logger.log(Level.SEVERE, "Błąd przy operacji konturowania", e);
        }
    }
    private void setupLogger () {
        try {
            File logFile = new File("logi_aplikacji.log");
            FileHandler handler = new FileHandler(logFile.getAbsolutePath(), true);
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Nie udało się utworzyć loggera: " + e.getMessage());
        }
    }
}


