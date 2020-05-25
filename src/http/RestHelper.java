package http;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class RestHelper {

    private static final String API = "";


    public static JSONObject doLogin(String user, String password) {
        try {
            URL url = new URL(API + "login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user", user);
            jsonObject.put("password", password);

            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(jsonObject.toJSONString());
            osw.flush();
            osw.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if(!response.toString().isEmpty()) {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(response.toString());
                System.out.println("Response: " + connection.getResponseCode());

                return json;
            }


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean createUser(String user, String password, String publicKey, String privateKey) {
        try {
            URL url = new URL(API + "register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user", user);
            jsonObject.put("password", password);
            jsonObject.put("publicKey", publicKey);
            jsonObject.put("privateKey", privateKey);

            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(jsonObject.toJSONString());
            osw.flush();
            osw.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Response: " + connection.getResponseCode());

            if(connection.getResponseCode() == 200)
                return true;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static JSONObject searchUser(String user) {
        try {
            URL url = new URL(API + "search");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user", user);

            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(jsonObject.toJSONString());
            osw.flush();
            osw.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if(!response.toString().isEmpty()) {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(response.toString());

                System.out.println("Response: " + connection.getResponseCode());

                return json;
            }

            System.out.println("Response: " + connection.getResponseCode());

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
