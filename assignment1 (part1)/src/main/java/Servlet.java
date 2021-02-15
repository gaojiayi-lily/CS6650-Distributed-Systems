import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.*;

public class Servlet extends javax.servlet.http.HttpServlet {
    public final String PURCHASE = "purchase";
    public final String CUSTOMER = "customer";
    public final String DATE = "date";

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("application/json");

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
            //response.getWriter().write("parameters not in a valid format!");

            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print("parameters not in a valid format!");
            out.flush();

        } else {
            response.setStatus(HttpServletResponse.SC_OK);

            //response.getWriter().write("It works!");
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print("It works!");
            out.flush();
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        // http://localhost:8080/WEB_APP/assignment1/purchase/0/customer/0/date/0
        // the input urlPath should be in the following format
        // ["", "purchase", "storeID", "customer", "customerID", "date", "date(String)"]

        if (urlPath[1].equals(PURCHASE) && urlPath[3].equals(CUSTOMER) && urlPath[5].equals(DATE)) {
            return true;
        }
        return false;
    }
}
