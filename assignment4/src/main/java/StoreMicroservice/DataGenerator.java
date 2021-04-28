package StoreMicroservice;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

public class DataGenerator implements Runnable {
    public int NUM_ITEMS = 100000;
    public int NUM_STORES = 512;
    public Connection conn;

    private String QUEUE_NAME;
    private int[][] itemByStore;


    public DataGenerator(Connection conn, String QUEUE_NAME, int[][] itemByStore) {
        this.conn = conn;
        this.QUEUE_NAME = QUEUE_NAME;
        this.itemByStore = itemByStore;
    }

    @Override
    public void run() {
        try {
            final Channel channel = conn.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queuePurge(QUEUE_NAME);
            channel.basicQos(1);

            Object monitor = new Object();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .deliveryMode(1)
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                String response = "";
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    String[] urlParts = message.split("/");
                    int single = Integer.parseInt(urlParts[2]);

                    String res = message.contains("store") ?
                            getTopTen(single, "store") : getTopTen(single, "top10");
                    response += res;

                } catch (Exception e) {
                    e.printStackTrace();
                    channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);

                } finally {

                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            channel.basicConsume(QUEUE_NAME, false, deliverCallback, (consumerTag -> {
            }));
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getTopTen(int id, String input) {

        PriorityQueue<int[]> pq = new PriorityQueue<>(new Comparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                if (a[0] == b[0]) return a[1] - b[1]; // item by store && itemID
                else return a[0] - b[0];
            }

        });
        if (input.equals("store")) {
            for (int i = 1; i <= NUM_ITEMS; i++) {
                pq.offer(new int[]{itemByStore[i][id], i}); //min heap -> total && items
                while (pq.size() > 10) pq.poll();
            }

            StringBuilder sb = new StringBuilder();
            sb.append("The 10 most purchased items in store " + id + " is :");
            sb.append("\r\n");

            int count = 10;
            while (!pq.isEmpty()) {
                int[] res = pq.poll();
                sb.append("The" + count + " most frequent itemID is: " + res[1] + ", with quantity: " + res[0]);
                sb.append("\r\n");
                count--;
            }

            return sb.toString();

        } else {
            for (int j = 1; j <= NUM_STORES; j++) {
                pq.offer(new int[]{itemByStore[id][j], j}); //min heap -> total && store
                while (pq.size() > 10) pq.poll();
            }

            StringBuilder sb = new StringBuilder();
            sb.append("The items " + id + " sells best in 10 stores listed below: ");
            sb.append("\r\n");

            int count = 10;
            while (!pq.isEmpty()) {
                int[] res = pq.poll();
                sb.append("The" + count + " best selling store is: " + res[1] + ", with quantity: " + res[0]);
                sb.append("\r\n");
                count--;
            }

            return sb.toString();
        }

    }

}
