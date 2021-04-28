package StoreMicroservice;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InMemoryStore {
    public static String RPC_QUEUE_NAME = "requestQueue";
    public static String QUEUE_NAME = "responseQueue";

    public static final String USERNAME = "jgaoac";
    public static final String PASSWORD = "jgaoac_password";
    public static final String HOST = "54.91.165.13";

//    public static final String USERNAME = "guest";
//    public static final String PASSWORD = "guest";
//    public static final String HOST = "localhost";

    //TODO: how to change the file path and the config file when push to ec2 instance
    public static int NUM_ITEMS = 100000;
    public static int NUM_STORES = 512;
    public static int threadTotal = 500;

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        factory.setHost(HOST);

        ExecutorService dataProcessorPool = Executors.newFixedThreadPool(25);

        try {
            Connection conn = factory.newConnection(dataProcessorPool);
            Channel channel = conn.createChannel();

            channel.exchangeDeclare("micro", BuiltinExchangeType.FANOUT, false);
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // Bind a queue to an exchange, with no extra arguments.
            channel.queueBind(QUEUE_NAME, "micro", "");
            System.out.println("In memory store is now waiting for message. To exit press CTRL+C");

            int[][] itemByStore = new int[NUM_ITEMS + 1][NUM_STORES + 1];

            for (int i = 0; i < threadTotal; i++) {
                dataProcessorPool.execute(new DataProcessor(conn, QUEUE_NAME, itemByStore));
            }

            new Thread(new DataGenerator(conn, RPC_QUEUE_NAME, itemByStore)).start();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
