import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    public static final int OPERATION_HOURS = 9;
    public static final int TIMEOUT_MSEC = 1000;
    public static final String[] TIME_ZONE = {"EAST", "CENTRAL", "WEST"};

    public static int portNumber = 8080;
    public static String serverAddress = "localhost";
    public static int maxStores = 16;
    public static int custIDs = 1000; // range of customer IDs per store
    public static int itemID = 100000;
    public static int numberPurchase = 60; // number of purchases per hour
    public static int itemInPurchase = 5; // number of items for each purchase (range 1-20)
    public static String date = "20210101";

    public static AtomicInteger successCount = new AtomicInteger(0);
    public static AtomicInteger failureCount = new AtomicInteger(0);
    public static AtomicInteger storeCount = new AtomicInteger(0);
    public static AtomicInteger requestCount = new AtomicInteger(0);
    public static String BASE_PATH = "http://" + serverAddress+ ":" + portNumber + "/WEB_APP/assignment1";

    public static synchronized void main(String[] args) throws ParseException, InterruptedException {

        Scanner scan = new Scanner(System.in);
        String[] input = null;

        boolean status = true;
        while (status) {
            System.out.println("Enter the portNumber && optional serverAddress: ");
            input = scan.nextLine().split("\\s+");

            switch (input.length) {
                case 2:
                    serverAddress = input[1];
                case 1:
                    try {
                        portNumber = Integer.parseInt(input[0]);
                        status = false;
                    } catch (Exception e) {
                        System.out.println("Invalid port number.");
                        System.out.println("Usage is: > java Client [portNumber] [serverAddress]");
                    }
            }
        }
        BASE_PATH = "http://" + serverAddress+ ":" + portNumber + "/WEB_APP/assignment1";

        System.out.println("Enter the maxStores && optional range of customer IDs per store: ");
        input = scan.nextLine().split("\\s+");
        maxStores = Integer.parseInt(input[0]);
        if (input.length == 2) custIDs = Integer.parseInt(input[1]);

        System.out.println("Do you want to use default settings for other initial inputs?");
        System.out.println("Enter y or n: ");
        if (scan.nextLine().equals("n")) {
            String value = "";
            System.out.println("Enter the max item IDs (enter 'd' if default 100,000 selected): ");
            value = scan.nextLine();
            if (!value.equals("d")) itemID = Integer.parseInt(value);

            System.out.println("Enter the number of purchases per hr (enter 'd' if default 60 selected): ");
            value = scan.nextLine();
            if (!value.equals("d")) numberPurchase = Integer.parseInt(value);

            System.out.println("Enter the number of items per purchase (enter 'd' if default 5 selected)");
            System.out.println("Please note that the range is 1 - 20: ");
            value = scan.nextLine();
            if (!value.equals("d")) itemInPurchase = Integer.parseInt(value);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            date = simpleDateFormat.format(new Date());
            System.out.println(date);

        } else {
            System.out.println("Default settings are applied, have fun :)");
        }

        Timestamp startTime = new Timestamp(System.currentTimeMillis());

        ExecutorService threadPoolEast = Executors.newFixedThreadPool(maxStores / 4);
        ExecutorService threadPoolMid = Executors.newFixedThreadPool(maxStores / 4);
        ExecutorService threadPoolWest = Executors.newFixedThreadPool(maxStores / 2);


        PoolExecutor executor = new PoolExecutor(OPERATION_HOURS, BASE_PATH, itemID, custIDs, date,
                storeCount, requestCount, successCount,
                failureCount);
        executor.startNewStoreThread(threadPoolEast, maxStores / 4);

        while (requestCount.get() <= (maxStores / 4) * numberPurchase * 3) {
            Thread.sleep(1);
        }
        executor.startNewStoreThread(threadPoolMid, maxStores / 4);

        while (requestCount.get() <= (maxStores / 4) * numberPurchase * (3 + 2 + 2)) {
            Thread.sleep(1);
        }
        executor.startNewStoreThread(threadPoolWest, maxStores / 2);

        threadPoolEast.shutdown();
        threadPoolMid.shutdown();
        threadPoolWest.shutdown();

        threadPoolEast.awaitTermination(1000, TimeUnit.SECONDS);
        threadPoolMid.awaitTermination(1000, TimeUnit.SECONDS);
        threadPoolWest.awaitTermination(1000, TimeUnit.SECONDS);

        Timestamp finishTime = new Timestamp(System.currentTimeMillis());
        long wallTime = finishTime.getTime() - startTime.getTime();
        long throughput = requestCount.get() * 1000 / wallTime;

        System.out.println("Thread start time: " + startTime);
        System.out.println("Thread finish time: " + finishTime);
        System.out.println("Successful POST: " + successCount.get());
        System.out.println("Failure POST: " + failureCount.get());
        System.out.println("Wall time: " + wallTime);
        System.out.println("Throughput (based on sec): " + throughput);


    }

}
