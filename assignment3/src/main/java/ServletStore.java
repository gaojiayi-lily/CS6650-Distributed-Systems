import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class ServletStore extends javax.servlet.http.HttpServlet {

    private ConnectionFactory factory;
    private Connection conn;
    private ChannelFactory channelFactory = new ChannelFactory();
    private Channel dummy;
    private ObjectPool<Channel> channelPool;

    String RPC_QUEUE_NAME = "requestQueue";
    String REPLY_QUEUE = "responseQueue";
    String STORE = "store";
    String TOP10 = "top10";

    public final String USERNAME = "jgaoac";
    public final String PASSWORD = "jgaoac_password";
    public final String HOST = "3.89.158.154";

    public class ChannelFactory extends BasePooledObjectFactory<Channel> {
        @Override
        public Channel create() throws Exception {
            return conn.createChannel();
        }

        @Override
        public PooledObject<Channel> wrap(Channel channel) {
            return new DefaultPooledObject<Channel>(channel);
        }
    }

    @Override
    public void init() throws ServletException {
        factory = new ConnectionFactory();

        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        factory.setHost(HOST);

        dummy = null;
        try {
            conn = factory.newConnection();
            channelPool = new GenericObjectPool<>(channelFactory);
            dummy = channelPool.borrowObject();
            dummy.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void destroy() {
        if (conn != null) {
            try {
                conn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (channelPool != null) { channelPool.close(); }

        if (dummy != null) {
            try {
                dummy.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: Create doGet method of the servlet, doPost method is not necessary
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        String urlPath = request.getPathInfo();

        if (urlPath == null || urlPath.isEmpty() || urlPath.length() == 1) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            //response.getWriter().write("missing parameters!");

            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print("missing parameters!");
            out.flush();
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isValid(urlParts)) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print("parameters not in a valid format!");
            out.flush();

        } else {
            String corrId = UUID.randomUUID().toString();

            Channel channel = null;
            try {
                channel = channelPool.borrowObject();
                channel.queueDeclare(REPLY_QUEUE, false, false, false, null);
                AMQP.BasicProperties props = new AMQP.BasicProperties
                        .Builder()
                        .deliveryMode(1) // set persistence as 1
                        .correlationId(corrId)
                        .replyTo(REPLY_QUEUE)
                        .build();
                channel.basicPublish("", RPC_QUEUE_NAME, props, urlPath.getBytes("UTF-8"));

                BlockingQueue<String> responseQueue = new ArrayBlockingQueue<>(1);

                String cTag = channel.basicConsume(REPLY_QUEUE, true, (consumerTag, delivery) -> {
                    if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                        responseQueue.offer(new String(delivery.getBody(), "UTF-8"));
                    }
                }, consumerTag -> {
                });

                //TODO: Retrieves and removes the head of this queue,
                // waiting if necessary until an element becomes available.
                String result = responseQueue.take();
                channel.basicCancel(cTag);

                if (result != null && result.length() != 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write(result);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("The requested data is not valid");
                }

            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            } finally {
                if (channel != null) {
                    try {
                        channelPool.returnObject(channel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean isValid(String[] urlParts) {
        // http://18.232.80.41:8080/assignment3_war_exploded/store/store/172
        // http://18.232.80.41:8080/assignment3_war_exploded/store/top10/68960
        // the input urlPath should be in the following format
        // ["", "store", "001"] or ["", "top10", "100"]

        if (urlParts[1].equals(STORE) || urlParts[1].equals(TOP10)) {
            return true;
        }
        return false;
    }

}
