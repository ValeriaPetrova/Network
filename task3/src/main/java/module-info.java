module ru.nsu.task {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires lombok;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;

    opens ru.nsu.task3 to javafx.fxml;
    exports ru.nsu.task3;
    exports ru.nsu.task3.Model;
    opens ru.nsu.task3.Model to javafx.fxml;
}