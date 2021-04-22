import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;

import java.sql.Timestamp;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class PoolExecutor {

    private int OPERATION_HOURS;
    private String BASE_PATH;
    private int itemID;
    private int custIDs;
    private String date;
    private AtomicInteger storeCount;
    private AtomicInteger requestCount;
    private AtomicInteger successCount;
    private AtomicInteger failureCount;
    private BlockingQueue<String> queue;

    public PoolExecutor(int OPERATION_HOURS, String BASE_PATH, int itemID, int custIDs, String date,
                        AtomicInteger storeCount, AtomicInteger requestCount, AtomicInteger successCount,
                        AtomicInteger failureCount, BlockingQueue<String> queue) {
        this.OPERATION_HOURS = OPERATION_HOURS;
        this.BASE_PATH = BASE_PATH;
        this.itemID = itemID;
        this.custIDs = custIDs;
        this.date = date;
        this.storeCount = storeCount;
        this.requestCount = requestCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.queue = queue;
    }


    public void startNewStoreThread(ExecutorService threadPool, int stores) {

        for (int count = 0; count < stores; count++) {

            threadPool.execute(() -> {
                try {
                    int storeID = storeCount.addAndGet(1);

                    for (int i = 0; i < OPERATION_HOURS; i++) {
                        for (int j = 0; j < 300; j++) {
                            PurchaseApi apiInstance = new PurchaseApi();
                            ApiClient apiClient = apiInstance.getApiClient();
                            apiClient.setBasePath(BASE_PATH);

                            PurchaseItems item = new PurchaseItems();
                            Random random = new Random();
                            item.itemID(String.valueOf(random.nextInt(itemID)));
                            item.setNumberOfItems(1);

                            Purchase body = new Purchase();
                            body.addItemsItem(item);
                            int custID = storeID * 1000 + random.nextInt(custIDs);

                            apiInstance.newPurchase(body, storeID, custID, date);
                            Timestamp beforePOST = new Timestamp(System.currentTimeMillis());
                            //record.createPurchaseRecord(storeID, custID, date);
                            ApiResponse<Void> apiResponse = apiInstance.newPurchaseWithHttpInfo(body, storeID, custID, date);

                            requestCount.addAndGet(1);
                            int res = apiResponse.getStatusCode();
                            Timestamp afterPost = new Timestamp(System.currentTimeMillis());
                            long latency = afterPost.getTime() - beforePOST.getTime();
                            if (res == 200 || res == 201) successCount.addAndGet(1);
                            else failureCount.addAndGet(1);
                            queue.offer(beforePOST.toString() + ",POST," + latency + "," + res);
                        }
                    }
                } catch (ApiException e) {
                    System.err.println("Exception when calling PurchaseApi#newPurchase");
                    e.printStackTrace();
                }
            });
        }
    }
}
