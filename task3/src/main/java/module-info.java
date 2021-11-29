module ru.nsu.task {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires lombok;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires org.slf4j;

    opens ru.nsu.task3 to javafx.fxml;
    exports ru.nsu.task3;
    exports ru.nsu.task3.Model;
    opens ru.nsu.task3.Model to javafx.fxml;
    exports ru.nsu.task3.Model.Weather;
    opens ru.nsu.task3.Model.Weather to javafx.fxml, com.fasterxml.jackson.databind;
    exports ru.nsu.task3.Model.Description;
    opens ru.nsu.task3.Model.Description to javafx.fxml, com.fasterxml.jackson.databind;
    exports ru.nsu.task3.Model.NearbyPlaces;
    opens ru.nsu.task3.Model.NearbyPlaces to javafx.fxml, com.fasterxml.jackson.databind;
}