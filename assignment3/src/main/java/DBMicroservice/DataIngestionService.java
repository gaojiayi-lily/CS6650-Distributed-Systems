package DBMicroservice;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataIngestionService {
    public static String QUEUE_NAME = "dbWriter";

    public static final String USERNAME = "jgaoac";
    public static final String PASSWORD = "jgaoac_password";
    public static final String HOST = "3.89.158.154";

    public static int threadTotal = 500;

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();

        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        factory.setHost(HOST);

        ExecutorService dbWriterPool = Executors.newFixedThreadPool(threadTotal);

        Connection conn = factory.newConnection(dbWriterPool);
        Channel channel = conn.createChannel();
        channel.exchangeDeclare("micro", "fanout", false);
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, "micro", "");
        System.out.println(" Data ingestion service successfully started. To exit press CTRL+C");

        for (int i = 0; i < threadTotal; i++) {
            dbWriterPool.execute(new DBDataInput(conn, QUEUE_NAME));
        }

    }
}

