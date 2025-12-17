package in.co.rays.proj4.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.CourseBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.model.CourseModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * Controller for managing Course operations like add, update, view and navigation.
 * <p>
 * This servlet validates course form input, populates {@link CourseBean} from
 * request parameters, and delegates persistence operations to
 * {@link in.co.rays.proj4.model.CourseModel}. It supports operations such as
 * Save, Update, Cancel and Reset.
 * </p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.CourseModel
 * @see in.co.rays.proj4.bean.CourseBean
 */
@WebServlet(name = "CourseCtl", urlPatterns = { "/ctl/CourseCtl" })
public class CourseCtl extends BaseCtl {

    private static final Logger log = Logger.getLogger(CourseCtl.class);

    /**
     * Validates the course form fields.
     * <ul>
     *   <li>Name is required and must be a valid name.</li>
     *   <li>Duration is required.</li>
     *   <li>Description is required.</li>
     * </ul>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return {@code true} if validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {

        log.debug("CourseCtl validate() started");

        boolean pass = true;

        if (DataValidator.isNull(request.getParameter("name"))) {
            log.warn("Course name is null");
            request.setAttribute("name", PropertyReader.getValue("error.require", "Name"));
            pass = false;
        } else if (!DataValidator.isName(request.getParameter("name"))) {
            log.warn("Invalid course name");
            request.setAttribute("name", "Invalid Name");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("duration"))) {
            log.warn("Course duration is null");
            request.setAttribute("duration", PropertyReader.getValue("error.require", "Duration"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("description"))) {
            log.warn("Course description is null");
            request.setAttribute("description", PropertyReader.getValue("error.require", "Description"));
            pass = false;
        }

        log.debug("CourseCtl validate() completed with status: " + pass);
        return pass;
    }

    /**
     * Populates a {@link CourseBean} from request parameters and fills audit
     * information by calling {@link #populateDTO(BaseBean, HttpServletRequest)}.
     *
     * @param request the {@link HttpServletRequest} carrying form data
     * @return a populated {@link BaseBean} (actually a {@link CourseBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("CourseCtl populateBean() started");

        CourseBean bean = new CourseBean();

        bean.setId(DataUtility.getLong(request.getParameter("id")));
        bean.setName(DataUtility.getString(request.getParameter("name")));
        bean.setDuration(DataUtility.getString(request.getParameter("duration")));
        bean.setDescription(DataUtility.getString(request.getParameter("description")));

        populateDTO(bean, request);

        log.debug("CourseCtl populateBean() completed");
        return bean;
    }

    /**
     * Handles HTTP GET requests. If an 'id' parameter is provided (> 0), loads
     * the corresponding course record and places it on the request for editing/view.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("CourseCtl doGet() started");

        long id = DataUtility.getLong(request.getParameter("id"));
        log.debug("Requested course id: " + id);

        CourseModel model = new CourseModel();

        if (id > 0) {
            try {
                CourseBean bean = model.findByPk(id);
                ServletUtility.setBean(bean, request);
                log.info("Course record loaded for id: " + id);
            } catch (ApplicationException e) {
                log.error("ApplicationException while loading course", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        }

        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Handles HTTP POST requests for Save, Update, Cancel and Reset operations.
     * <ul>
     *   <li>OP_SAVE: Adds a new course (handles {@link DuplicateRecordException}).</li>
     *   <li>OP_UPDATE: Updates existing course.</li>
     *   <li>OP_CANCEL: Redirects to course list controller.</li>
     *   <li>OP_RESET: Redirects back to course form.</li>
     * </ul>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("CourseCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));
        log.debug("Operation received: " + op);

        CourseModel model = new CourseModel();
        long id = DataUtility.getLong(request.getParameter("id"));

        if (OP_SAVE.equalsIgnoreCase(op)) {
            CourseBean bean = (CourseBean) populateBean(request);
            try {
                log.info("Adding new course");
                long pk = model.add(bean);
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Course added successfully", request);
            } catch (DuplicateRecordException e) {
                log.warn("Duplicate course record", e);
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Course already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException while saving course", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        } else if (OP_UPDATE.equalsIgnoreCase(op)) {
            CourseBean bean = (CourseBean) populateBean(request);
            try {
                if (id > 0) {
                    log.info("Updating course id: " + id);
                    model.update(bean);
                }
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Course updated successfully", request);
            } catch (DuplicateRecordException e) {
                log.warn("Duplicate course on update", e);
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Course already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException while updating course", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        } else if (OP_CANCEL.equalsIgnoreCase(op)) {
            log.info("Cancel operation: redirecting to course list");
            ServletUtility.redirect(ORSView.COURSE_LIST_CTL, request, response);
            return;
        } else if (OP_RESET.equalsIgnoreCase(op)) {
            log.info("Reset operation: redirecting to course form");
            ServletUtility.redirect(ORSView.COURSE_CTL, request, response);
            return;
        }

        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Returns the JSP view path for the course form.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        return ORSView.COURSE_VIEW;
    }
}
