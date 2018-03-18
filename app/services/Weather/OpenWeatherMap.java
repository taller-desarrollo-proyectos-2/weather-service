package services.Weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;


public class OpenWeatherMap implements WeatherProvider {
    private static final Logger.ALogger logger = Logger.of("weather-service.services.Weather.OpenWeatherMap");

    private static String url = "https://api.openweathermap.org/data/2.5/forecast";
    private static String apiKey = ConfigFactory.load().getString("weather.provider.apiKey");

    @Override
    public F.Promise<String> getWeather(String city, int days) {

        return WS.url(url).setQueryParameter("id", city).setHeader("x-api-key", apiKey).get().map(
                response -> response.asJson().toString()
        );


    }

    @Override
    public F.Promise<JsonNode> parseResponse(F.Promise<String> response) {
        return response.map(s -> {
            JsonNode openWeatherResponse = Json.parse(s);
            ObjectNode apiResponse = JsonNodeFactory.instance.objectNode();
            apiResponse.put("status", openWeatherResponse.get("cod").asInt());
            if(openWeatherResponse.get("cod").asInt() == 200) {
                // -- Filtrar parametros a devolver de la api
                ArrayNode forecast = JsonNodeFactory.instance.arrayNode();
                ObjectNode bodyResponse = JsonNodeFactory.instance.objectNode();
                bodyResponse.put("cityCode", openWeatherResponse.get("city").get("id"));
                for (JsonNode node : openWeatherResponse.get("list")) {
                    ObjectNode dayNode = JsonNodeFactory.instance.objectNode();
                    dayNode.put("data", node.get("main"));
                    forecast.add(dayNode);
                }
                bodyResponse.put("forecast", forecast);
                apiResponse.put("body", bodyResponse);
            }else{
                apiResponse.put("body", JsonNodeFactory.instance.objectNode().put("error", "error with provider communication"));
            }
            return apiResponse;
        }
        );
    }
}
