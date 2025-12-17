package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.MarksheetBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.model.MarksheetModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * Controller to display the merit list of marksheets.
 * <p>
 * It retrieves the merit list from {@link MarksheetModel#getMeritList(int,int)}
 * and forwards the result to the view with pagination support. Supports a
 * simple back operation to return to the welcome page.
 * </p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.MarksheetModel
 * @see in.co.rays.proj4.bean.MarksheetBean
 */
@WebServlet(name = "MarksheetMeritListCtl", urlPatterns = { "/ctl/MarksheetMeritListCtl" })
public class MarksheetMeritListCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(MarksheetMeritListCtl.class);

    /**
     * Handles HTTP GET requests to fetch and display the merit list.
     * It sets list, pageNo and pageSize attributes and forwards to the view.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.debug("MarksheetMeritListCtl doGet() started");

        int pageNo = 1;
        int pageSize = DataUtility.getInt(PropertyReader.getValue("page.size"));

        log.debug("Page No: " + pageNo + ", Page Size: " + pageSize);

        MarksheetModel model = new MarksheetModel();

        try {
            log.info("Fetching merit list");
            List<MarksheetBean> list = model.getMeritList(pageNo, pageSize);

            if (list == null || list.isEmpty()) {
                log.warn("No merit list records found");
                ServletUtility.setErrorMessage("No record found", request);
            }

            ServletUtility.setList(list, request);
            ServletUtility.setPageNo(pageNo, request);
            ServletUtility.setPageSize(pageSize, request);

            log.debug("Forwarding to view");
            ServletUtility.forward(getView(), request, response);

        } catch (ApplicationException e) {
            log.error("Exception in MarksheetMeritListCtl doGet()", e);
            e.printStackTrace();
            ServletUtility.handleException(e, request, response);
            return;
        }

        log.debug("MarksheetMeritListCtl doGet() ended");
    }

    /**
     * Handles HTTP POST requests. Supports the back operation which redirects
     * to the welcome controller.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.debug("MarksheetMeritListCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));
        log.debug("Operation received: " + op);

        if (OP_BACK.equalsIgnoreCase(op)) {
            log.info("Back operation triggered");
            ServletUtility.redirect(ORSView.WELCOME_CTL, request, response);
            return;
        }

        log.debug("MarksheetMeritListCtl doPost() ended");
    }

    /**
     * Returns the JSP view path for the merit list page.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning Marksheet Merit List View");
        return ORSView.MARKSHEET_MERIT_LIST_VIEW;
    }
}
