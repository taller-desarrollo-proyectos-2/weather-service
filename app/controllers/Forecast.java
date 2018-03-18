package controllers;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.typesafe.config.ConfigFactory;
import play.Logger;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import services.Weather.WeatherProvider;
import services.Weather.WeatherProviderFactory;

import java.util.Map;


public class    Forecast extends Controller{
    private static final Logger.ALogger logger = Logger.of("weather-service.controllers.Forecast");

    public static F.Promise<Result> forecast(){

        Map<String, String[]> queryParams = request().queryString();

        if(!queryParams.containsKey("city")){
            logger.error("Attempting to obtain forecast but no city was specified.");
            return F.Promise.pure(badRequest(JsonNodeFactory.instance.objectNode().put("error", "City not specified on query string")));
        }

        WeatherProviderFactory factory = new WeatherProviderFactory();
        String provider = ConfigFactory.load().getString("weather.provider.type");
        if(provider == null){
            logger.error("Weather provider not specified on application.conf file");
        }
        WeatherProvider forecastProvider = factory.getInstance(provider);

        String[] days = queryParams.get("days");

        try{
            //Aunque pudiera haber más de un valor para city siempre me quedo con el primero
            F.Promise<String> response = forecastProvider.getWeather(queryParams.get("city")[0], days == null ? 5 : Integer.valueOf(days[0]));
            //Devuelvo la respuesta parseada por el proveedor.
            return forecastProvider.parseResponse(response).map(
                    jsonNode -> status(jsonNode.get("status").asInt(), jsonNode.get("body"))
            );
        }catch(Exception e){
            logger.error("Error attempting to obtain and parse response from forecast provider", e);
            return F.Promise.pure(internalServerError(JsonNodeFactory.instance.objectNode().put("error", "The server was unable to process the request")));
        }
    }
}
