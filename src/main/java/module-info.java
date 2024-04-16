module com.example.klienciuslugsieciowych {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires javafx.web;


    opens com.example.klienciuslugsieciowych to javafx.fxml;
    exports com.example.klienciuslugsieciowych;
}