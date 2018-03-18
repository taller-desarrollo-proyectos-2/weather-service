package services.Weather;


import com.fasterxml.jackson.databind.JsonNode;
import play.libs.F;

/**
 * @Author facundo
 * @Date 18/03/18
 * @Since Vx.y.z
 **/
public interface WeatherProvider {

    F.Promise<String> getWeather(String city, int days);

    F.Promise<JsonNode> parseResponse(F.Promise<String> response);
}
