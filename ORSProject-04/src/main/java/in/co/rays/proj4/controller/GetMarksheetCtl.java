package in.co.rays.proj4.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.MarksheetBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.model.MarksheetModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * GetMarksheetCtl handles requests to retrieve a marksheet by roll number.
 * <p>
 * It validates the roll number input, populates a {@link MarksheetBean} from the
 * request, delegates lookup to {@link MarksheetModel#findByRollNo(String)}, and
 * forwards the result (or an error message) to the view.
 * </p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.MarksheetModel
 * @see in.co.rays.proj4.bean.MarksheetBean
 */
@WebServlet(name = "GetMarksheetCtl", urlPatterns = { "/ctl/GetMarksheetCtl" })
public class GetMarksheetCtl extends BaseCtl {

    private static final Logger log = Logger.getLogger(GetMarksheetCtl.class);

    /**
     * Validates the request parameters for retrieving a marksheet.
     * <ul>
     *   <li>rollNo is required.</li>
     * </ul>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return {@code true} when validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {

        log.debug("GetMarksheetCtl validate() started");

        boolean pass = true;

        if (DataValidator.isNull(request.getParameter("rollNo"))) {
            log.warn("Roll number is null");
            request.setAttribute("rollNo", PropertyReader.getValue("error.require", "Roll Number"));
            pass = false;
        }

        log.debug("GetMarksheetCtl validate() completed with status: " + pass);
        return pass;
    }

    /**
     * Populates a {@link MarksheetBean} from the request parameters.
     *
     * @param request the {@link HttpServletRequest} containing parameters
     * @return populated {@link BaseBean} (actually a {@link MarksheetBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("GetMarksheetCtl populateBean() called");

        MarksheetBean bean = new MarksheetBean();

        bean.setRollNo(DataUtility.getString(request.getParameter("rollNo")));

        return bean;
    }

    /**
     * Handles HTTP GET by forwarding to the get-marksheet view.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("GetMarksheetCtl doGet() called");
        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Handles HTTP POST. When operation is {@link BaseCtl#OP_GO}, looks up the
     * marksheet by roll number via {@link MarksheetModel#findByRollNo(String)}.
     * On success the bean is placed on the request; if not found an error
     * message is set. Application exceptions are handled via {@link ServletUtility}.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("GetMarksheetCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));
        log.debug("Operation received: " + op);

        MarksheetModel model = new MarksheetModel();
        MarksheetBean bean = (MarksheetBean) populateBean(request);

        if (OP_GO.equalsIgnoreCase(op)) {
            try {
                log.info("Searching marksheet for rollNo: " + bean.getRollNo());
                bean = model.findByRollNo(bean.getRollNo());
                if (bean != null) {
                    log.info("Marksheet found for rollNo: " + bean.getRollNo());
                    ServletUtility.setBean(bean, request);
                } else {
                    log.warn("No marksheet found for rollNo: " + bean.getRollNo());
                    ServletUtility.setErrorMessage("RollNo Does Not exists", request);
                }
            } catch (ApplicationException e) {
                log.error("ApplicationException while fetching marksheet", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        }

        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Returns the view path for the get-marksheet page.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        return ORSView.GET_MARKSHEET_VIEW;
    }
}
