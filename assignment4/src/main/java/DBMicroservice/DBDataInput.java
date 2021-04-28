package DBMicroservice;

import DatabasePool.PurchaseRecord;
import MongoDB.PurchaseRecordMongoDB;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.swagger.client.api.PurchaseApi;

import java.io.IOException;

public class DBDataInput implements Runnable {
    private Connection conn;
    private String queueName;

    public DBDataInput(Connection conn, String queueName) {
        this.conn = conn;
        this.queueName = queueName;
    }

    @Override
    public void run() {

        try {

            Channel channel = conn.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);

            channel.basicQos(1);
            ChannelConsumer consumer = new ChannelConsumer(channel);
            channel.basicConsume(queueName, consumer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ChannelConsumer extends DefaultConsumer {

        public ChannelConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
                                   byte[] body) throws IOException {
            String message = new String(body, "UTF-8");

            String[] purchaseInfo = message.split("#");
            int storeID = Integer.valueOf(purchaseInfo[1]);
            int customerID = Integer.valueOf(purchaseInfo[2]);
            String date = purchaseInfo[3];

//            PurchaseRecord purchaseRecord = new PurchaseRecord();
//            purchaseRecord.createPurchaseRecord(storeID, customerID, date);

            PurchaseRecordMongoDB record = new PurchaseRecordMongoDB();
            record.createPurchaseRecordMongoDB(storeID, customerID, date);

            getChannel().basicAck(envelope.getDeliveryTag(), false);
        }

    }

}
