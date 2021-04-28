package MongoDB;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {

    static String USER = "cs6650_mongoDB"; // the user name
    static String DATABASE = "Records"; // the name of the database in which the user is defined
    static String PASSWORD = "cs6650_password"; // the password as a character array


    static MongoClientURI uri = new MongoClientURI(
            "mongodb+srv://" + USER + ":" + PASSWORD +
            "@cluster0.79mm4.mongodb.net/" + DATABASE + "?retryWrites=true&w=majority");

    static MongoClient mongoClient = new MongoClient(uri);
    static MongoDatabase database = mongoClient.getDatabase(DATABASE); //GET Database
    static MongoCollection usersCollection = database.getCollection(DATABASE); //GET Collection

    public static MongoCollection getUsersCollection() {
        return usersCollection;
    }

}
