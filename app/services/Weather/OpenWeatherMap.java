package services.Weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        return WS.url(url).setQueryParameter("id", city).setQueryParameter("units", "metric").setHeader("x-api-key", apiKey).get().map(
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
                Date date = null;
                float tempDay = 0;
                float tempNight = 0;
                int sDay = 0;
                int sNight = 0;
                String weatherDay = "";
                String weatherNight = "";
                
                for (JsonNode node : openWeatherResponse.get("list")) {
                    if(date == null) date = new Date(node.get("dt").asLong()*1000);
                    Date nodeDate = new Date(node.get("dt").asLong()*1000);
                    if (date.getDate()< nodeDate.getDate()){
                        ObjectNode dayNode = JsonNodeFactory.instance.objectNode();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        dayNode.put("date", sdf.format(date));
                        if(sDay > 0) {
                            dayNode.put("day_temperature", tempDay/sDay);
                            dayNode.put("day_weather", weatherDay);
                        } else {
                            dayNode.put("day_temperature", "");
                            dayNode.put("day_weather", "");
                        }
                        if(sNight > 0) {
                            dayNode.put("night_temperature", tempNight/sNight);
                            dayNode.put("night_weather", weatherNight);
                        } else {
                            dayNode.put("night_temperature", "");
                            dayNode.put("night_weather", "");
                        }
                        forecast.add(dayNode);
                        date = nodeDate;
                        tempDay = 0;
                        tempNight = 0;
                        sDay = 0;
                        sNight = 0;
                        weatherDay = "";
                        weatherNight = "";
                        if(nodeDate.getHours() < 12){
                            weatherDay = node.get("weather").get(0).get("main").asText();
                            tempDay += Float.parseFloat(node.get("main").get("temp").asText());
                            sDay ++;
                        } else {
                            weatherNight = node.get("weather").get(0).get("main").asText();
                            tempNight += Float.parseFloat(node.get("main").get("temp").asText());
                            sNight ++;
                        }
                    } else {
                        if(nodeDate.getHours() < 12){
                            weatherDay = node.get("weather").get(0).get("main").asText();
                            tempDay += Float.parseFloat(node.get("main").get("temp").asText());
                            sDay ++;
                        } else {
                            weatherNight = node.get("weather").get(0).get("main").asText();
                            tempNight += Float.parseFloat(node.get("main").get("temp").asText());
                            sNight ++;
                        }
                    }
                }
                ObjectNode dayNode = JsonNodeFactory.instance.objectNode();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                dayNode.put("date", sdf.format(date));
                if(sDay > 0) {
                    dayNode.put("day_temperature", tempDay/sDay);
                    dayNode.put("day_weather", weatherDay);
                } else {
                    dayNode.put("day_temperature", "");
                    dayNode.put("day_weather", "");
                }
                if(sNight > 0) {
                    dayNode.put("night_temperature", tempNight/sNight);
                    dayNode.put("night_weather", weatherNight);
                } else {
                    dayNode.put("night_temperature", "");
                    dayNode.put("night_weather", "");
                }
                forecast.add(dayNode);
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
