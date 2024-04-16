package com.example.klienciuslugsieciowych;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;



public class HelloController {
    @FXML
    private Text walutaWybrana, walutaPLN, temp;
    @FXML
    private TextField current, city, country;
    @FXML
    private WebView webView;
    Service s;

    public void szukaj() {
        String cityName = city.getText().trim();
        String countryName = country.getText().trim();

        if (isEmpty(cityName, countryName)) {
            showEmptyFieldsAlert();
        } else if (containsDigits(cityName, countryName)) {
            showInvalidInputAlert();
        } else {
            performSearch(cityName, countryName);
        }
    }

    private boolean isEmpty(String cityName, String countryName) {
        return cityName.isEmpty() || countryName.isEmpty();
    }

    private boolean containsDigits(String cityName, String countryName) {
        return cityName.matches(".*\\d.*") || countryName.matches(".*\\d.*");
    }

    private void showEmptyFieldsAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Empty Fields");
        alert.setHeaderText(null);
        alert.setContentText("Please enter both city and country before searching.");
        alert.showAndWait();
    }

    private void showInvalidInputAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText("City and country names cannot contain digits.");
        alert.showAndWait();
    }

    private void performSearch(String cityName, String countryName) {
        s = new Service("Poland");
        s.getWeather(cityName);
        temp.setText(s.getCelcious() + " C");
        walutaWybrana.setText(String.valueOf(s.getCountryCurrency(current.getText())));
        walutaPLN.setText(String.valueOf(s.getNBPRateSrodek()));

        s.getPageOfCity(webView);
        System.out.println(current.getText());
        System.out.println(cityName);
        System.out.println(countryName);
    }


}