package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.SubjectBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.model.CourseModel;
import in.co.rays.proj4.model.SubjectModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * SubjectCtl is a controller servlet that handles CRUD operations for Subject
 * entities. It preloads the course list for the subject form, validates input
 * parameters, populates {@link SubjectBean} from request parameters and
 * delegates persistence operations to {@link SubjectModel}.
 * <p>
 * Supported operations include Save, Update, Cancel and Reset.
 * </p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.SubjectModel
 * @see in.co.rays.proj4.bean.SubjectBean
 */
@WebServlet(name = "SubjectCtl", urlPatterns = { "/ctl/SubjectCtl" })
public class SubjectCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(SubjectCtl.class);

    /**
     * Preloads the list of courses and sets it as request attribute "courseList"
     * so the subject form can render a course dropdown.
     *
     * @param request the {@link HttpServletRequest}
     */
    @Override
    protected void preload(HttpServletRequest request) {
        log.debug("SubjectCtl preload() called");
        CourseModel courseModel = new CourseModel();
        try {
            List courseList = courseModel.list();
            request.setAttribute("courseList", courseList);
            log.info("Course list preloaded, size=" + courseList.size());
        } catch (ApplicationException e) {
            log.error("ApplicationException in preload()", e);
            e.printStackTrace();
        }
    }

    /**
     * Validates the subject form parameters.
     * <ul>
     *   <li>name is required.</li>
     *   <li>courseId is required.</li>
     *   <li>description is required.</li>
     * </ul>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return {@code true} if validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {
        log.debug("SubjectCtl validate() called");

        boolean pass = true;

        if (DataValidator.isNull(request.getParameter("name"))) {
            log.warn("Validation failed: Subject Name required");
            request.setAttribute("name", PropertyReader.getValue("error.require", "Subject Name"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("courseId"))) {
            log.warn("Validation failed: Course Name required");
            request.setAttribute("courseId", PropertyReader.getValue("error.require", "Course Name"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("description"))) {
            log.warn("Validation failed: Description required");
            request.setAttribute("description", PropertyReader.getValue("error.require", "Description"));
            pass = false;
        }

        return pass;
    }

    /**
     * Populates a {@link SubjectBean} from request parameters and sets audit
     * fields using {@link #populateDTO(BaseBean, HttpServletRequest)}.
     *
     * @param request the {@link HttpServletRequest} containing form data
     * @return populated {@link BaseBean} (actually a {@link SubjectBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {
        log.debug("SubjectCtl populateBean() called");

        SubjectBean bean = new SubjectBean();

        bean.setId(DataUtility.getLong(request.getParameter("id")));
        bean.setName(DataUtility.getString(request.getParameter("name")));
        bean.setCourseId(DataUtility.getLong(request.getParameter("courseId")));
        bean.setDescription(DataUtility.getString(request.getParameter("description")));

        populateDTO(bean, request);

        return bean;
    }

    /**
     * Handles HTTP GET requests. If an 'id' parameter is provided (> 0), loads
     * the corresponding subject and sets it on the request for editing/view.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("SubjectCtl doGet() started");

        long id = DataUtility.getLong(request.getParameter("id"));

        SubjectModel model = new SubjectModel();

        if (id > 0) {
            try {
                SubjectBean bean = model.findByPk(id);
                ServletUtility.setBean(bean, request);
                log.info("Subject loaded with id=" + id);
            } catch (ApplicationException e) {
                log.error("ApplicationException in doGet()", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        }
        ServletUtility.forward(getView(), request, response);
        log.info("SubjectCtl doGet() completed");
    }

    /**
     * Handles HTTP POST requests for saving and updating subjects.
     * <ul>
     *   <li>OP_SAVE: Adds a new subject (handles {@link DuplicateRecordException}).</li>
     *   <li>OP_UPDATE: Updates an existing subject.</li>
     *   <li>OP_CANCEL: Redirects to subject list controller.</li>
     *   <li>OP_RESET: Redirects back to subject form.</li>
     * </ul>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("SubjectCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));

        SubjectModel model = new SubjectModel();

        long id = DataUtility.getLong(request.getParameter("id"));

        if (OP_SAVE.equalsIgnoreCase(op)) {
            log.debug("Save operation started");
            SubjectBean bean = (SubjectBean) populateBean(request);
            try {
                long pk = model.add(bean);
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Subject added successfully", request);
                log.info("Subject added successfully with id=" + pk);
            } catch (DuplicateRecordException e) {
                log.warn("DuplicateRecordException: Subject Name already exists");
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Subject Name already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException in doPost() save operation", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        } else if (OP_UPDATE.equalsIgnoreCase(op)) {
            log.debug("Update operation started");
            SubjectBean bean = (SubjectBean) populateBean(request);
            try {
                if (id > 0) {
                    model.update(bean);
                }
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Subject updated successfully", request);
                log.info("Subject updated successfully with id=" + id);
            } catch (DuplicateRecordException e) {
                log.warn("DuplicateRecordException: Subject Name already exists");
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Subject Name already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException in doPost() update operation", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        } else if (OP_CANCEL.equalsIgnoreCase(op)) {
            log.info("Cancel operation, redirecting to Subject List");
            ServletUtility.redirect(ORSView.SUBJECT_LIST_CTL, request, response);
            return;
        } else if (OP_RESET.equalsIgnoreCase(op)) {
            log.info("Reset operation, redirecting to Subject Form");
            ServletUtility.redirect(ORSView.SUBJECT_CTL, request, response);
            return;
        }

        ServletUtility.forward(getView(), request, response);
        log.info("SubjectCtl doPost() completed");
    }

    /**
     * Returns the JSP view path for the subject form.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning Subject view page");
        return ORSView.SUBJECT_VIEW;
    }
}
