import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import DatabaseSingle.MySQLConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;


@WebServlet(name = "Servlet")
public class ServletPurchase extends javax.servlet.http.HttpServlet {

    public final String PURCHASE = "purchase";
    public final String CUSTOMER = "customer";
    public final String DATE = "date";
    public MySQLConnection mySQLConnection;

    public ConnectionFactory factory;
    public Connection conn;
    public ChannelFactory channelFactory = new ChannelFactory();
    public Channel dummy;
    public ObjectPool<Channel> channelPool;

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
            return new DefaultPooledObject<>(channel);
        }
    }

    @Override
    public void init() {

        factory = new ConnectionFactory();

        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        factory.setHost(HOST);

        dummy = null;
        try {
            conn = factory.newConnection();

            channelPool = new GenericObjectPool<>(channelFactory);
            dummy = channelPool.borrowObject(); // dummy channel
            dummy.exchangeDeclare("micro", "fanout", false);

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
        if (channelPool != null) {
            channelPool.close();
        }

        if (dummy != null) {
            try {
                dummy.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("plain/text");
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

        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print("Let's see: parameters not in a valid format!");
            out.flush();

        } else {

            BufferedReader reqBodyBuffer = request.getReader();
            String reqBody = reqBodyBuffer.lines().collect(Collectors.joining());

            reqBody = reqBody.concat("#").concat(urlParts[2]).concat("#").concat(urlParts[4]).concat("#").concat(urlParts[6]);


            // TODO: create a exchange channel && publish to RabbitMQ.
            Channel channel = null;
            try {
                channel = channelPool.borrowObject();
                channel.basicPublish("micro", "", null, reqBody.getBytes("UTF-8"));

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("It works! You are now calling the post method! Connected to RabbitMQ");

            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("RabbitMQ publish failure");
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: For test -> do get with the right link will be directly saved to mysql database
        response.setContentType("application/json");

        String urlPath = request.getPathInfo();

        if (urlPath == null || urlPath.isEmpty() || urlPath.length() == 1) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters!");

            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print("missing parameters!");
            out.flush();
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("parameters not in a valid format!");

            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print("parameters not in a valid format!");
            out.flush();

        } else {
        response.setStatus(HttpServletResponse.SC_OK);

        // TODO: Write the received data in to the RDS database
            mySQLConnection = new MySQLConnection();
            mySQLConnection.CustomerConnection();
            mySQLConnection.setPurchaseRecord(Integer.parseInt(urlParts[2]), Integer.parseInt(urlParts[4]), urlParts[6]);
            mySQLConnection.close();

        PrintWriter out = response.getWriter();
        response.setCharacterEncoding("UTF-8");
        out.print("It works! You are now calling the get method!");
        out.flush();
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        // http://localhost:8080/assignment3_war_exploded/post/purchase/1/customer/10/date/20210401
        // the input urlPath should be in the following format
        // ["", "purchase", "storeID", "customer", "customerID", "date", "date(String)"]

        if (urlPath[1].equals(PURCHASE) && urlPath[3].equals(CUSTOMER) && urlPath[5].equals(DATE)) {
            return true;
        }
        return false;
    }
}