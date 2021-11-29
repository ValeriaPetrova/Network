package ru.nsu.task3;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.nsu.task3.Model.*;
import ru.nsu.task3.Model.Description.Description;
import ru.nsu.task3.Model.NearbyPlaces.InformationAboutPlace;
import ru.nsu.task3.Model.Weather.Weather;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

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

    private void updateListOfPlaces() throws ExecutionException, InterruptedException {
        listOfPlaces.getItems().clear();
        listOfNearbyPlaces.getItems().clear();
        description.clear();
        weather.clear();
        CopyOnWriteArrayList<Place> place = model.getPlaces().get();
        if (place.isEmpty()) {
            listOfPlaces.getItems().add("Places not found");
        } else {
            for (Place current : place) {
                listOfPlaces.getItems().add(current.toString());
            }
        }
        listOfPlaces.refresh();
    }

    private void updateListOfNearbyPlaces() throws ExecutionException, InterruptedException, IOException {
        listOfNearbyPlaces.getItems().clear();
        description.clear();
        weather.clear();
        Weather weather = model.getWeather().get();
        this.weather.appendText(weather.toString());
        CopyOnWriteArrayList<InformationAboutPlace> nearbyPlaces = model.getListOfNearbyPlaces().get();
        if (nearbyPlaces.isEmpty()) {
            listOfNearbyPlaces.getItems().add("Places not found.");
        } else {
            for (InformationAboutPlace current : nearbyPlaces) {
                listOfNearbyPlaces.getItems().add(current.toString());
            }
        }
        listOfNearbyPlaces.refresh();
    }

    private void updateDescriptionOfPlace() throws ExecutionException, InterruptedException {
        this.description.clear();
        Description description = model.getDescription().get();
        if (description.getTitle() == null && description.getText() == null) {
            this.description.appendText("No description for the selected place.");
        } else {
            this.description.appendText(description.toString());
        }
    }

    private void search() throws UnsupportedEncodingException, ExecutionException, InterruptedException, ModelException, MalformedURLException {
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