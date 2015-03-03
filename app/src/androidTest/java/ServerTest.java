



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import json.JSONModels.*;


public class ServerTest {


    public static void dispatchLocalServer() {
        Thread t= new Thread(new Runnable () {

            @Override
            public void run() {
                try {
                    core.Server.main(new String[0]);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });
        t.start();
    }

    public static boolean setupDatabase() throws SQLException {
        DatabaseSetup setup = new DatabaseSetup();
        return setup.setup();
    }

    public static String delimiter = "==========================================================================";
    public static String host;
    public static String protocol = "http";
    public static int port = 8000;

    public static String token = null;
    public static int householdID;
    public static int listID;
    public static long timestamp;

    public static int rcode;
    public static String response;

    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @BeforeClass
    public static void setupClass() throws IOException, InterruptedException, SQLException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Use local server? [Y/N] ");
        host = "127.0.0.1";
        if (in.readLine().equalsIgnoreCase("Y")) {
            dispatchLocalServer();
            Thread.sleep(5000);
            if (!setupDatabase()) throw new IOException("Cannot instantiate database");
        } else {
            System.out.println("Enter host address (without http://): ");
            host = in.readLine();
        }
    }
    @Test
    public void test() throws MalformedURLException, IOException {
        System.out.println("Starting server test");
        register("email1@gmail.com", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "John", "Doe");
        assertEquals("Registration 1 pass", 201, rcode);
        register("email1@gmail.com", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "John", "Doe");
        assertNotEquals("Registration 1 retry", 201, rcode);
        register("email2@gmail.com", "d9298a10d1b0735837dc4bd85dac641b0f3cef27a47e5d53a54f2f3f5b2fcffa", "Jane", "Robinson");
        assertEquals("Registration 2 pass", 201, rcode);
        login("email1@gmail.com", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d9");
        assertEquals("Login failure", 403, rcode);
        login("email1@gmail.com", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8");
        assertEquals("Login pass", 200, rcode);
        token = gson.fromJson(response, LoginResJSON.class).token;
        createHousehold("Stash", "Private Inventory");
        assertEquals("Household creation pass", 201, rcode);
        householdID = gson.fromJson(response, HouseholdCreateResJSON.class).householdID;
        link("029000071858", "Planters Cocktail Peanuts", "oz.");
        assertEquals("Link 1 pass", 200, rcode);
        link( "04963406", "Coca Cola, Can", "oz.");
        assertEquals("Link 2 pass", 200, rcode);
        link("036632001085", "Dannon Fruit on Bottom Blueberry", "oz.");
        assertEquals("Link 3 pass", 200, rcode);
        createList("Weekly Shopping");
        assertEquals("Create List pass", 201, rcode);
        ListCreateResJSON lcr = gson.fromJson(response, ListCreateResJSON.class);
        listID = lcr.listID;
        timestamp = lcr.version;
        List<ListUpdateItem> items = new ArrayList<ListUpdateItem>();
        items.add(new ListUpdateItem("029000071858", 3, 0));
        items.add(new ListUpdateItem( "04963406", 12, 0));
        items.add(new ListUpdateItem("036632001085", 6, 0));
        updateList(items);
        assertEquals("Update list pass", 200, rcode);
        timestamp = gson.fromJson(response, ListUpdateResJSON.class).timestamp;
        getList();
        assertEquals("Get list pass", 200, rcode);
        getSelfInfo();
        assertEquals("Get self pass", 200, rcode);
        getHousehold();
        assertEquals("Get household pass", 200, rcode);

    }


    public static class Transaction {
        private HttpURLConnection connection;
        public Transaction(String protocol, String host, int port, String file) throws MalformedURLException, IOException {
            connection = (HttpURLConnection) new URL(protocol, host, port, file).openConnection();
        }

        public String getRequestURL() {
            return connection.getURL().toString();
        }
        public void setGetMethod() throws ProtocolException {
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
        }
        public void setPostMethod() throws ProtocolException {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);
        }
        public void send(String request) throws IOException {
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(request);
            wr.flush(); wr.close();
        }
        public int getResponseCode() throws IOException {
            return connection.getResponseCode();
        }

        public String getResponse() throws IOException {
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append("\n");
            }
            rd.close();
            return response.toString();
        }
        public void close() {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void register(String emailAddress, String password, String first, String last) throws MalformedURLException, IOException {
        Transaction request = new Transaction(protocol, host, port, "/users/register");
        request.setPostMethod();
        String reqstr = gson.toJson(new RegisterReqJSON(emailAddress, password, first, last));
        System.out.println(delimiter + "\nRequest: REGISTER");
        System.out.println(request.getRequestURL());
        System.out.println(reqstr);
        request.send(reqstr);
        System.out.println("Response:");
        rcode = request.getResponseCode();
        System.out.println("HTTP " + rcode);
        request.close();
    }


    public void login(String emailAddress, String password) throws IOException {
        Transaction request = new Transaction(protocol, host, port, "/users/login");
        request.setPostMethod();
        String reqstr = gson.toJson(new LoginReqJSON(emailAddress, password));
        System.out.println(delimiter + "\nRequest: LOGIN ");
        System.out.println(request.getRequestURL());
        System.out.println(reqstr);
        request.send(reqstr);
        System.out.println("Response:");
        rcode = request.getResponseCode();
        System.out.println("HTTP " + rcode);
        try {
            response = request.getResponse();
            System.out.println(response);
        }catch (IOException e) {}
        request.close();
    }

    public void createHousehold(String name, String description) throws IOException {
        Transaction request = new Transaction(protocol, host, port, "/households/create?token=" + token);
        request.setPostMethod();
        String reqstr = gson.toJson(new HouseholdCreateReqJSON(name, description));
        System.out.println(delimiter + "\nRequest: CREATE HOUSEHOLD");
        System.out.println(request.getRequestURL());
        System.out.println(reqstr);
        request.send(reqstr);
        System.out.println("Response:");
        rcode = request.getResponseCode();
        System.out.println("HTTP " + rcode);
        try {
            response = request.getResponse();
            System.out.println(response);
        }catch (IOException e) {}
        request.close();
    }

    public void link(String UPC, String description, String unitName)throws IOException {
        Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/items/" + UPC + "/link?token=" + token);
        request.setPostMethod();
        String reqstr = gson.toJson(new LinkReqJSON(description, unitName));
        System.out.println(delimiter + "\nRequest: LINK UPC");
        System.out.println(request.getRequestURL());
        System.out.println(reqstr);
        request.send(reqstr);
        System.out.println("Response:");
        rcode = request.getResponseCode();
        System.out.println("HTTP " + rcode);
        request.close();
    }

    public void createList(String listName) throws IOException {
        Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/lists/create?token=" + token);
        request.setPostMethod();
        String reqstr = gson.toJson(new ListCreateReqJSON(listName));
        System.out.println(delimiter + "\nRequest: CREATE LIST");
        System.out.println(request.getRequestURL());
        System.out.println(reqstr);
        request.send(reqstr);
        System.out.println("Response:");
        rcode = request.getResponseCode();
        System.out.println("HTTP " + rcode);
        try {
            response = request.getResponse();
            System.out.println(response);
        }catch (IOException e) {}
        request.close();
    }

    public void updateList(List<ListUpdateItem> items) throws IOException {
        Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/lists/" + listID + "/update?token=" + token);
        request.setPostMethod();
        String reqstr = gson.toJson(new ListUpdateReqJSON(timestamp, items));
        System.out.println(delimiter + "\nRequest: UPDATE LIST");
        System.out.println(request.getRequestURL());
        System.out.println(reqstr);
        request.send(reqstr);
        System.out.println("Response:");
        rcode = request.getResponseCode();
        System.out.println("HTTP " + rcode);
        try {
            response = request.getResponse();
            System.out.println(response);
        }catch (IOException e) {}
        request.close();
    }

    public void getList() throws IOException{
        Transaction request = new Transaction(protocol, host, port, "/households/" + householdID + "/lists/" + listID + "?token=" + token);
        request.setGetMethod();
        System.out.println(delimiter + "\nRequest: GET LIST");
        System.out.println(request.getRequestURL());
        System.out.println("Response:");
        rcode = request.getResponseCode();
        System.out.println("HTTP " + rcode);
        try {
            response = request.getResponse();
            System.out.println(response);
        }catch (IOException e) {}
        request.close();
    }

    public void getSelfInfo() throws IOException{
        Transaction request = new Transaction(protocol, host, port, "/users/me?token=" + token);
        request.setGetMethod();
        System.out.println(delimiter + "\nRequest: GET USER INFORMATION");
        System.out.println(request.getRequestURL());
        System.out.println("Response:");
        rcode = request.getResponseCode();
        System.out.println("HTTP " + rcode);
        try {
            response = request.getResponse();
            System.out.println(response);
        }catch (IOException e) {}
        request.close();
    }
    public void getHousehold() throws IOException{
        Transaction request = new Transaction(protocol, host, port, "/households/" + householdID +"?token=" + token);
        request.setGetMethod();
        System.out.println(delimiter + "\nRequest: GET HOUSEHOLD");
        System.out.println(request.getRequestURL());
        System.out.println("Response:");
        rcode = request.getResponseCode();
        System.out.println("HTTP " + rcode);
        try {
            response = request.getResponse();
            System.out.println(response);
        }catch (IOException e) {}
        request.close();
    }


}