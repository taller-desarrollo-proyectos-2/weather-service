package services.Weather;

import play.Logger;

public class WeatherProviderFactory {
    private static final Logger.ALogger logger = Logger.of("weather-service.services.Weather.WeatherProviderFactory");


    public WeatherProvider getInstance(String type){
        switch(type){
            case "OpenWeatherMap":
                return new OpenWeatherMap();
            default:
                return new OpenWeatherMap();
        }
    }
}
