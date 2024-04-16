package com.example.klienciuslugsieciowych;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;


//TODO: Poprawiłem kod wydzielając go lepiej na metody zgodnie z zasadami DRY i SOLID na tyle ile umiałem. Dodałem obsługę błedów :>

public class Service {
    private String KRAJ;
    private String MIASTO;
    private String weatherJson;
    private String CURRENCY;
    private Double KURS;

    public Service(String kraj) {
        this.KRAJ = kraj;
    }

    public Double getCountryCurrency(String currency) {
        CURRENCY = getActualCountryCurrency();

        String url = "https://v6.exchangerate-api.com/v6/435b81bcab82a93ce46c63d6/latest/" + currency;
        System.out.println();

        String request = sendRequest(url);
        JSONObject jsonObject = new JSONObject(request);
        Double change = jsonObject.getJSONObject("conversion_rates").getDouble(CURRENCY);
        KURS = change;

        return change;
    }
    private String getActualCountryCurrency() {
        JSONObject jsonObject = new JSONObject(weatherJson);
        JSONObject sysObject = jsonObject.getJSONObject("sys");
        String countryCode = sysObject.getString("country");

        String apiUrl = "https://restcountries.com/v3/alpha/" + countryCode;
        String countryDetails = sendRequest(apiUrl);
        System.out.println("Response:");
        System.out.println(countryDetails);


        JSONArray jsonArray = new JSONArray(countryDetails);
        JSONObject jsonObject1 = jsonArray.getJSONObject(0);

        JSONObject currenciesObject1 = jsonObject1.getJSONObject("currencies");
        System.out.println(currenciesObject1);
        String currentValue = String.valueOf(currenciesObject1);


        int start = currentValue.indexOf("{");
        if (start != -1) {
            String trimmedString = currentValue.substring(start);
            trimmedString = trimmedString.replaceAll("\\s", "");
            int end = trimmedString.indexOf("}");

            if (end != -1) {
                String keyValue = trimmedString.substring(0, end + 1);
                String[] parts = keyValue.split(":");
                String key = parts[0].replaceAll("[{}\"]", "");
                System.out.println("Extracted Key: " + key);
                return key;
            } else {
                System.out.println("Niepoprawny Json do wydobycia waluty");
            }
        } else {
            System.out.println("Invalid JSON format: Opening brace not found.");
        }
        return "Some problems ;<";
    }
    public String getWeather(String miasto) {
        Double[] lanAndLonTable = checkCity(miasto);
        String apiKey = "&appid=73bb2cb2b9741e51cecc8bc4d4010e9b";
        String lat = "lat=" + lanAndLonTable[0];
        String lon = "&lon=" + lanAndLonTable[1];
        String urlString = "https://api.openweathermap.org/data/2.5/weather?" + lat + lon + apiKey;

        this.weatherJson = sendRequest(urlString);
        return weatherJson;
    }
    public String getCelcious() {
        JSONObject jsonObject = new JSONObject(weatherJson);
        JSONObject mainObject = jsonObject.getJSONObject("main");
        double temperature = mainObject.getDouble("temp");
        System.out.println("Temperature: " + temperature + " Kelvin");

        double temperatureCelsius = temperature - 273.15;
        DecimalFormat df = new DecimalFormat("#.##");
        String formattedTemperature = df.format(temperatureCelsius);


        return String.valueOf(formattedTemperature);
    }
    private Double[] checkCity(String miasto) {
        String apiKey = "&limit=5&appid=73bb2cb2b9741e51cecc8bc4d4010e9b";
        String urlString = "http://api.openweathermap.org/geo/1.0/direct?q=" + miasto + apiKey;
        MIASTO = miasto;

        return getLonLat(sendRequest(urlString));
    }
    private Double[] getLonLat(String jsonString) {
        try {
            JSONArray jsonArray = new JSONArray(jsonString);

                JSONObject jsonObject = jsonArray.getJSONObject(0);
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
                symbols.setDecimalSeparator('.');
                DecimalFormat df = new DecimalFormat("#.##", symbols);
                Double lat = Double.parseDouble(df.format(jsonObject.getDouble("lat")));
                Double lon = Double.parseDouble(df.format(jsonObject.getDouble("lon")));

                System.out.println("Latitude: " + lat);
                System.out.println("Longitude: " + lon);
                System.out.println();

                return new Double[] {lat, lon};
        } catch (JSONException e) {
            System.out.println("Nie udało wyciągnąć się Lon i Lat");
        }
        return new Double[0];
    }
    private String sendRequest(String linkToSend) {
        try {
            URL url = new URL(linkToSend);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // 200
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return response.toString();
            } else {
                System.out.println("Problem z pobraniem, nie znaleziono takiej wartości " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println("Bład przy pobieraniu Jsona, brak wartosci ");
        }
        return null;
    }
    public Double getNBPRateSrodek() {
        String answer = String.valueOf(getOfertaOrZapytanie("MID"));

        return Double.parseDouble(answer);
    }
    public Double getOfertaOrZapytanie(String atx) {
        String url = "";
        switch (atx) {
            case "MID":
                url = "http://api.nbp.pl/api/exchangerates/rates/A/" + CURRENCY;
                break;
            case "KUPNO":
                url = "http://api.nbp.pl/api/exchangerates/rates/B/" + CURRENCY;
                break;
            case "ZAPYTANIE":
                url = "http://api.nbp.pl/api/exchangerates/rates/C/" + CURRENCY;
                break;
            default:
                return 0.0;
        }

        try {
            String answer = sendRequest(url);
            if (answer != null) {
                JSONObject jsonObject = new JSONObject(answer);
                JSONArray ratesArray = jsonObject.getJSONArray("rates");
                JSONObject ratesObject = ratesArray.getJSONObject(0);
                if (atx.equals("MID")) {
                    return ratesObject.getDouble("mid");
                } else if (atx.equals("KUPNO")) {
                    return ratesObject.getDouble("ask");
                } else if (atx.equals("ZAPYTANIE")) {
                    return ratesObject.getDouble("bid");
                }
            }
        } catch (Exception e) {
            System.out.println("Nie udało się wyciągnąć wartości dla tabeli");
        }
        return 0.0;
    }
    public void getPageOfCity(WebView webView) {
        WebEngine webEngine = webView.getEngine();
        String linkWiki = "https://pl.wikipedia.org/wiki/";
        webEngine.load(linkWiki + MIASTO);
    }
}
