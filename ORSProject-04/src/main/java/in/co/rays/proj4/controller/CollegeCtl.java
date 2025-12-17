package in.co.rays.proj4.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.CollegeBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.model.CollegeModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * Controller for managing College operations such as add, update, and view.
 * <p>
 * This servlet handles request validation, populating {@link CollegeBean}
 * instances from request parameters, and delegating persistence operations to
 * {@link CollegeModel}. It also performs navigation actions like cancel and reset.
 * </p>
 * 
 * author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.CollegeModel
 * @see in.co.rays.proj4.bean.CollegeBean
 */
@WebServlet(name = "CollegeCtl", urlPatterns = { "/ctl/CollegeCtl" })
public class CollegeCtl extends BaseCtl {

    private static final Logger log = Logger.getLogger(CollegeCtl.class);

    /**
     * Validates college form fields (name, address, state, city, phoneNo).
     * <ul>
     *   <li>Name is required and must be a valid name.</li>
     *   <li>Address, State and City are required.</li>
     *   <li>Phone number is required, must be 10 digits and a valid phone number.</li>
     * </ul>
     *
     * @param request the {@link HttpServletRequest} carrying form parameters
     * @return {@code true} if validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {

        log.debug("CollegeCtl validate() started");

        boolean pass = true;

        if (DataValidator.isNull(request.getParameter("name"))) {
            log.warn("College name is null");
            request.setAttribute("name", PropertyReader.getValue("error.require", "Name"));
            pass = false;
        } else if (!DataValidator.isName(request.getParameter("name"))) {
            log.warn("Invalid college name entered");
            request.setAttribute("name", "Invalid Name");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("address"))) {
            log.warn("College address is null");
            request.setAttribute("address", PropertyReader.getValue("error.require", "Address"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("state"))) {
            log.warn("College state is null");
            request.setAttribute("state", PropertyReader.getValue("error.require", "State"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("city"))) {
            log.warn("College city is null");
            request.setAttribute("city", PropertyReader.getValue("error.require", "City"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("phoneNo"))) {
            log.warn("College phone number is null");
            request.setAttribute("phoneNo", PropertyReader.getValue("error.require", "Phone No"));
            pass = false;
        } else if (!DataValidator.isPhoneLength(request.getParameter("phoneNo"))) {
            log.warn("College phone number length invalid");
            request.setAttribute("phoneNo", "Phone No must have 10 digits");
            pass = false;
        } else if (!DataValidator.isPhoneNo(request.getParameter("phoneNo"))) {
            log.warn("Invalid college phone number");
            request.setAttribute("phoneNo", "Invalid Phone No");
            pass = false;
        }

        log.debug("CollegeCtl validate() completed with status: " + pass);
        return pass;
    }

    /**
     * Populates a {@link CollegeBean} from request parameters.
     * <p>
     * This method maps request parameters to bean properties and calls
     * {@link #populateDTO(BaseBean, HttpServletRequest)} to set audit fields.
     * </p>
     *
     * @param request the {@link HttpServletRequest} containing form data
     * @return populated {@link BaseBean} (actually a {@link CollegeBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("CollegeCtl populateBean() started");

        CollegeBean bean = new CollegeBean();

        bean.setId(DataUtility.getLong(request.getParameter("id")));
        bean.setName(DataUtility.getString(request.getParameter("name")));
        bean.setAddress(DataUtility.getString(request.getParameter("address")));
        bean.setState(DataUtility.getString(request.getParameter("state")));
        bean.setCity(DataUtility.getString(request.getParameter("city")));
        bean.setPhoneNo(DataUtility.getString(request.getParameter("phoneNo")));

        populateDTO(bean, request);

        log.debug("CollegeCtl populateBean() completed");
        return bean;
    }

    /**
     * Handles HTTP GET requests. If an id parameter is present (> 0), the
     * corresponding college record is loaded and set on the request for display.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("CollegeCtl doGet() started");

        long id = DataUtility.getLong(request.getParameter("id"));
        log.debug("Requested college id: " + id);

        CollegeModel model = new CollegeModel();

        if (id > 0) {
            try {
                log.info("Fetching college record for id: " + id);
                CollegeBean bean = model.findByPk(id);
                ServletUtility.setBean(bean, request);
            } catch (ApplicationException e) {
                log.error("ApplicationException while fetching college", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        }

        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Handles HTTP POST requests for save, update, cancel and reset operations.
     * <ul>
     *   <li>OP_SAVE: Adds a new college (handles {@link DuplicateRecordException}).</li>
     *   <li>OP_UPDATE: Updates existing college data.</li>
     *   <li>OP_CANCEL: Redirects to college list controller.</li>
     *   <li>OP_RESET: Redirects back to college form.</li>
     * </ul>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("CollegeCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));
        log.debug("Operation received: " + op);

        CollegeModel model = new CollegeModel();
        long id = DataUtility.getLong(request.getParameter("id"));

        if (OP_SAVE.equalsIgnoreCase(op)) {
            CollegeBean bean = (CollegeBean) populateBean(request);
            try {
                log.info("Adding new college");
                long pk = model.add(bean);
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Data is successfully saved", request);
            } catch (DuplicateRecordException e) {
                log.warn("Duplicate college record detected", e);
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("College Name already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException while saving college", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        } else if (OP_UPDATE.equalsIgnoreCase(op)) {
            CollegeBean bean = (CollegeBean) populateBean(request);
            try {
                if (id > 0) {
                    log.info("Updating college id: " + id);
                    model.update(bean);
                }
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Data is successfully updated", request);
            } catch (DuplicateRecordException e) {
                log.warn("Duplicate college record on update", e);
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("College Name already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException while updating college", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        } else if (OP_CANCEL.equalsIgnoreCase(op)) {
            log.info("Operation CANCEL: redirecting to college list");
            ServletUtility.redirect(ORSView.COLLEGE_LIST_CTL, request, response);
            return;
        } else if (OP_RESET.equalsIgnoreCase(op)) {
            log.info("Operation RESET: redirecting to college form");
            ServletUtility.redirect(ORSView.COLLEGE_CTL, request, response);
            return;
        }

        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Returns the JSP view path for the college form.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        return ORSView.COLLEGE_VIEW;
    }
}
