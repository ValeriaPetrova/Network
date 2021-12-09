package ru.nsu.task3;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.nsu.task3.Model.*;
import ru.nsu.task3.Model.NearbyPlaces.Features;
import ru.nsu.task3.Model.Place.Hits;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

public class Controller {
    private Model model = new Model();

    @FXML
    public TextField searchField;
    @FXML
    public ListView<String> listOfPlaces;
    @FXML
    public ListView<String> listOfNearbyPlaces;
    @FXML
    public TextArea description;
    @FXML
    public TextArea weather;

    public Controller() throws FileNotFoundException {
    }

    @FXML
    protected void keyListener(KeyEvent event)  {
        if (event.getCode() == KeyCode.ENTER) {
            try {
                search();
            } catch (Exception e) {
                e.printStackTrace();
                error();
            }
        }
    }
    @FXML
    protected void mouseListener()  {
        try {
            search();
        } catch (Exception e) {
            e.printStackTrace();
            error();
        }
    }

    private void updateListOfPlaces() {
        listOfPlaces.getItems().clear();
        listOfNearbyPlaces.getItems().clear();
        description.clear();
        weather.clear();
        CompletableFuture.supplyAsync(() -> model.getPlaces()
                .thenAcceptAsync(place -> {
                    Platform.runLater(() -> {
                        if (place.getHits().isEmpty()) {
                            listOfPlaces.getItems().add("Places not found");
                        } else {
                            for (Hits current : place.getHits()) {
                                listOfPlaces.getItems().add(current.toString());
                            }
                        }
                    });
                    listOfPlaces.refresh();
                })
        );
    }

    private void updateListOfNearbyPlaces() {
        listOfNearbyPlaces.getItems().clear();
        description.clear();
        weather.clear();
        CompletableFuture.supplyAsync(() -> model.getWeather()
                .thenAcceptAsync((weather -> this.weather.appendText(weather.toString())), Platform::runLater)
        );
        CompletableFuture.supplyAsync(() -> model.getListOfNearbyPlaces()
                .thenAcceptAsync((nearbyPlaces -> {
                    Platform.runLater(() -> {
                        if (nearbyPlaces.getFeatures().isEmpty()) {
                            listOfNearbyPlaces.getItems().add("Places not found.");
                        } else {
                            for (Features current : nearbyPlaces.getFeatures()) {
                                listOfNearbyPlaces.getItems().add(current.toString());
                            }
                        }
                    });
                    listOfNearbyPlaces.refresh();
                }))
        );
    }

    private void updateDescriptionOfPlace() {

        CompletableFuture.supplyAsync(() -> model.getDescription()
                .thenAcceptAsync(description -> Platform.runLater(() -> {
                    this.description.clear();
                    if (description.getTitle() == null && description.getText() == null) {
                        this.description.appendText("No description for the selected place.");
                    } else {
                        this.description.appendText(description.toString());
                    }
                }))
        );
    }

    private void search() throws UnsupportedEncodingException, ModelException {
        model.setPlaceName(searchField.getText());
        model.searchPlaces();
        updateListOfPlaces();
        listOfPlaces.setOnMouseClicked(event -> {
            try {
                model.choosePlace(listOfPlaces.getSelectionModel().getSelectedIndex());
                updateListOfNearbyPlaces();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listOfNearbyPlaces.setOnMouseClicked(event -> {
            try {
                model.setDescriptionOfPlace(listOfNearbyPlaces.getSelectionModel().getSelectedIndex());
                updateDescriptionOfPlace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void error() {
        if (model.getMode() == Mode.SEARCH) {
            listOfPlaces.getItems().clear();
            listOfNearbyPlaces.getItems().clear();
            description.clear();
            weather.clear();
            listOfPlaces.getItems().add("No such place.");
        } else {
            listOfNearbyPlaces.getItems().clear();
            description.clear();
            weather.clear();
            listOfNearbyPlaces.getItems().add("No description for the selected place.");
        }
    }

}