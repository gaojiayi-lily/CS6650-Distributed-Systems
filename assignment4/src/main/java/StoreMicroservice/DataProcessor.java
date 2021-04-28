package StoreMicroservice;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;

import java.io.IOException;
import java.util.List;

public class DataProcessor implements Runnable {

    public Connection conn;
    public String QUEUE_NAME;
    public int[][] itemByStore;

    public DataProcessor(Connection conn, String QUEUE_NAME, int[][] cache) {
        this.conn = conn;
        this.QUEUE_NAME = QUEUE_NAME;
        this.itemByStore = cache;
    }

    @Override
    public void run() {
        try {
            final Channel channel = conn.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                try {

                    // TODO：check message format
                    String message = new String(delivery.getBody(), "UTF-8");

                    //System.out.println("Debug: " + message);

                    String[] purchaseInfo = message.split("#");
                    int storeID = Integer.valueOf(purchaseInfo[1]);

                    Purchase purchase = new Gson().fromJson(purchaseInfo[0], Purchase.class); // from swagger i/o
                    List<PurchaseItems> items = purchase.getItems();

                    for (PurchaseItems i : items) {

                        synchronized (itemByStore[Integer.parseInt(i.getItemID())]) {
                            itemByStore[Integer.parseInt(i.getItemID())][storeID] += i.getNumberOfItems();
                        }

                    }

                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                } catch (Exception e) {
                    e.printStackTrace();
                    channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
                }
            };

            channel.basicConsume(QUEUE_NAME, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

