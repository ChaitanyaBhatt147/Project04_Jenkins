package in.co.rays.proj4.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.util.ServletUtility;

/**
 * WelcomeCtl is a simple controller servlet that forwards requests to the
 * welcome view (home page) of the application.
 * <p>
 * It extends {@link BaseCtl} and currently supports only HTTP GET requests.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 */
@WebServlet(name = "WelcomeCtl", urlPatterns = { "/WelcomeCtl" })
public class WelcomeCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(WelcomeCtl.class);

    /**
     * Handles HTTP GET requests by forwarding to the welcome view.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("WelcomeCtl doGet() called - forwarding to welcome view");
        ServletUtility.forward(getView(), request, response);
        log.info("Forwarded to view: " + getView());
    }

    /**
     * Returns the JSP view path for the welcome page.
     *
     * @return the welcome view path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("WelcomeCtl getView() called");
        return ORSView.WELCOME_VIEW;
    }
}
