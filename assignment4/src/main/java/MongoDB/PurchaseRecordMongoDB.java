package MongoDB;

import DatabasePool.DBCPDataSource;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;


public class PurchaseRecordMongoDB {

    MongoCollection usersCollection;

    public PurchaseRecordMongoDB() {
        usersCollection = MongoDBConnection.getUsersCollection();
    }

    public void createPurchaseRecordMongoDB(int storeId, int customerId, String date) {
        Document record = new Document("_id", new ObjectId())
                .append("storeId", storeId)
                .append("customerId", customerId)
                .append("date", date);

        usersCollection.insertOne(record);
    }
}
