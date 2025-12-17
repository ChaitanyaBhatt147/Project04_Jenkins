package in.co.rays.proj4.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.RoleBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DatabaseException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.model.RoleModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * RoleCtl is a controller servlet responsible for handling CRUD operations for
 * roles. It validates role form input, populates {@link RoleBean} objects from
 * request parameters, and delegates persistence operations to {@link RoleModel}.
 * <p>
 * Supported operations include Save (add), Update, Cancel and Reset.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.RoleModel
 * @see in.co.rays.proj4.bean.RoleBean
 */
@WebServlet(name = "RoleCtl", urlPatterns = { "/ctl/RoleCtl" })
public class RoleCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(RoleCtl.class);

    /**
     * Validates the role form inputs.
     * <ul>
     *   <li>name is required and must be a valid name.</li>
     *   <li>description is required.</li>
     * </ul>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return {@code true} if validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {

        log.debug("RoleCtl validate() started");

        boolean pass = true;

        if (DataValidator.isNull(request.getParameter("name"))) {
            request.setAttribute("name", PropertyReader.getValue("error.require", "Name"));
            pass = false;
        } else if (!DataValidator.isName(request.getParameter("name"))) {
            request.setAttribute("name", "Invalid Name");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("description"))) {
            request.setAttribute("description", PropertyReader.getValue("error.require", "Description"));
            pass = false;
        }

        log.debug("RoleCtl validate() completed with status: " + pass);
        return pass;
    }

    /**
     * Populates a {@link RoleBean} from request parameters and fills audit
     * information via {@link #populateDTO(BaseBean, HttpServletRequest)}.
     *
     * @param request the {@link HttpServletRequest} containing form data
     * @return populated {@link BaseBean} (actually a {@link RoleBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("RoleCtl populateBean() started");

        RoleBean bean = new RoleBean();

        bean.setId(DataUtility.getLong(request.getParameter("id")));
        bean.setName(DataUtility.getString(request.getParameter("name")));
        bean.setDescription(DataUtility.getString(request.getParameter("description")));

        populateDTO(bean, request);

        log.debug("RoleCtl populateBean() completed");
        return bean;
    }

    /**
     * Handles HTTP GET requests. If an 'id' parameter is provided (> 0), loads
     * the corresponding role and sets it on the request for editing/view.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("RoleCtl doGet() started");

        long id = DataUtility.getLong(request.getParameter("id"));

        RoleModel model = new RoleModel();

        if (id > 0) {
            try {
                log.debug("Fetching role by ID: " + id);
                RoleBean bean = model.findByPk(id);
                ServletUtility.setBean(bean, request);
            } catch (ApplicationException e) {
                log.error("Error fetching role by ID: " + id, e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        }

        ServletUtility.forward(getView(), request, response);
        log.info("RoleCtl doGet() completed");
    }

    /**
     * Handles HTTP POST requests for saving and updating roles.
     * <ul>
     *   <li>OP_SAVE: Adds a new role (handles {@link DuplicateRecordException}).</li>
     *   <li>OP_UPDATE: Updates an existing role.</li>
     *   <li>OP_CANCEL: Redirects to role list controller.</li>
     *   <li>OP_RESET: Redirects back to role form.</li>
     * </ul>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("RoleCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));
        long id = DataUtility.getLong(request.getParameter("id"));

        log.debug("Operation: " + op);

        RoleModel model = new RoleModel();

        if (OP_SAVE.equalsIgnoreCase(op)) {

            RoleBean bean = (RoleBean) populateBean(request);

            try {
                long pk = model.add(bean);
                log.info("Role added successfully with PK: " + pk);
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Data is successfully saved", request);
            } catch (DuplicateRecordException e) {
                log.warn("Duplicate role detected");
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Role already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException during role save", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            } catch (DatabaseException e) {
                log.error("DatabaseException during role save", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }

        } else if (OP_UPDATE.equalsIgnoreCase(op)) {

            RoleBean bean = (RoleBean) populateBean(request);

            try {
                if (id > 0) {
                    log.info("Updating role with ID: " + id);
                    model.update(bean);
                }
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Data is successfully updated", request);
            } catch (DuplicateRecordException e) {
                log.warn("Duplicate role during update");
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Role already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException during role update", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }

        } else if (OP_CANCEL.equalsIgnoreCase(op)) {
            log.info("Cancel operation - redirecting to Role List");
            ServletUtility.redirect(ORSView.ROLE_LIST_CTL, request, response);
            return;

        } else if (OP_RESET.equalsIgnoreCase(op)) {
            log.info("Reset operation - redirecting to Role Form");
            ServletUtility.redirect(ORSView.ROLE_CTL, request, response);
            return;
        }

        ServletUtility.forward(getView(), request, response);
        log.info("RoleCtl doPost() completed");
    }

    /**
     * Returns the JSP view path for the role form.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning Role View");
        return ORSView.ROLE_VIEW;
    }
}
