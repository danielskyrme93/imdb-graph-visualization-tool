package app.io;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;

/**
 * Used to request data from the OMDb API
 * @author Daniel Skyrme
 */
public class OMDbService {

    public static JSONObject getJSONFromURL(OMDbCommand command) {
        URLConnection connection;
        try {
            connection = command.getURL().openConnection();
            InputStream stream = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String asStr = br.readLine();
            return new JSONObject(asStr);
        } catch (IOException e) {
            return null;
        }

    }
}
