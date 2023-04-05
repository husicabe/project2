package ch.zhaw;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.bson.BsonNull;
import org.bson.Document;
import org.slf4j.LoggerFactory;

public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        // disable logging
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.getLogger("org.mongodb.driver").setLevel(Level.OFF);

        ConnectionString cs = new ConnectionString(
                "mongodb+srv://admin1:superSecret@test.ht5koju.mongodb.net/?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("Project2");
        MongoCollection<Document> collection = database.getCollection("nutrition");
        

// Berechne das Maximum und den Durchschnitt von "calories"
List<Bson> caloriesPipeline = Arrays.asList(
        Aggregates.group(null,
                Accumulators.max("max_calories", "$calories"),
                Accumulators.avg("avg_calories", "$calories")
        )
);
MongoCursor<Document> caloriesCursor = collection.aggregate(caloriesPipeline).iterator();
Document caloriesResult = caloriesCursor.next();
int maxCalories = caloriesResult.getInteger("max_calories");
double avgCalories = caloriesResult.getDouble("avg_calories");

// Query for two foods with more calories than average and one food with less calories than average
List<Bson> foodsPipeline = Arrays.asList(
        Aggregates.match(Filters.or(
                Filters.gt("calories", avgCalories),
                Filters.lt("calories", avgCalories)
        )),
        Aggregates.sample(3)
);
MongoCursor<Document> foodsCursor = collection.aggregate(foodsPipeline).iterator();
List<String> foodNames = new ArrayList<>();
while (foodsCursor.hasNext()) {
    Document food = foodsCursor.next();
    foodNames.add(food.getString("name"));
}

// Schließe die Verbindung zur MongoDB
mongoClient.close();

// Mische die Namen der Lebensmittel zufällig
Collections.shuffle(foodNames);

// Gib die Namen der Lebensmittel auf der Konsole aus und fordere den Benutzer auf, sie zu sortieren
System.out.println("Please order the following foods by increasing calories:");
for (int i = 0; i < foodNames.size(); i++) {
    System.out.println((i + 1) + ". " + foodNames.get(i));
}

// Lese die Eingabe des Benutzers ein und bewerte sie
Scanner scanner = new Scanner(System.in);
int[] userOrder = new int[3];
for (int i = 0; i < userOrder.length; i++) {
    System.out.print("Enter the position of " + foodNames.get(i) + ": ");
    userOrder[i] = scanner.nextInt();
}

int score = 0;
for (int i = 0; i < userOrder.length; i++) {
    if (userOrder[i] == (i + 1)) {
        score += 2;
    } else if (userOrder[i] == ((i + 1) % 3) + 1) {
        score += 1;
    }
}

// Gib die Bewertung des Benutzers aus
System.out.println("Your score is: " + score);

// Gib die tatsächlichen Kalorien der Lebensmittel aus
System.out.println("The actual calories are:");
for (int i = 0; i < foodNames.size(); i++) {
    System.out.println((i + 1) + ". " + foodNames.get(i) + ": " + maxCalories);
    
}
    }
}