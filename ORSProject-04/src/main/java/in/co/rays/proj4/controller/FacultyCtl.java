package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.FacultyBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.model.CollegeModel;
import in.co.rays.proj4.model.CourseModel;
import in.co.rays.proj4.model.FacultyModel;
import in.co.rays.proj4.model.SubjectModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * FacultyCtl is a controller servlet that manages faculty-related operations
 * such as add, update, view and navigation. It handles request validation,
 * preloading of related lists (college, course, subject), populating
 * {@link FacultyBean} from request parameters and delegating persistence
 * operations to {@link FacultyModel}.
 *
 * <p>Supported operations: Save, Update, Cancel, Reset.</p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.FacultyModel
 * @see in.co.rays.proj4.bean.FacultyBean
 */
@WebServlet(name = "FacultyCtl", urlPatterns = { "/ctl/FacultyCtl" })
public class FacultyCtl extends BaseCtl {

    private static final Logger log = Logger.getLogger(FacultyCtl.class);

    /**
     * Preloads lists required by the view: collegeList, subjectList and courseList.
     * These lists are typically used to populate dropdowns in the faculty form.
     *
     * @param request the {@link HttpServletRequest} to which attributes are added
     */
    @Override
    protected void preload(HttpServletRequest request) {

        log.debug("FacultyCtl preload() started");

        CollegeModel collegeModel = new CollegeModel();
        SubjectModel subjectModel = new SubjectModel();
        CourseModel courseModel = new CourseModel();

        try {
            List collegeList = collegeModel.list();
            request.setAttribute("collegeList", collegeList);

            List subjectList = subjectModel.list();
            request.setAttribute("subjectList", subjectList);

            List courseList = courseModel.list();
            request.setAttribute("courseList", courseList);

            log.debug("Faculty preload lists loaded successfully");

        } catch (ApplicationException e) {
            log.error("Error while preloading faculty dropdown data", e);
            e.printStackTrace();
        }
    }

    /**
     * Validates faculty form fields.
     * <ul>
     *   <li>firstName and lastName are required and must be valid names.</li>
     *   <li>gender is required.</li>
     *   <li>dob is required and must be a valid date.</li>
     *   <li>email is required and must be a valid email format.</li>
     *   <li>mobileNo is required, must be 10 digits and a valid phone number.</li>
     *   <li>collegeId, courseId and subjectId are required.</li>
     * </ul>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return {@code true} if validation succeeds; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {

        log.debug("FacultyCtl validate() started");

        boolean pass = true;

        if (DataValidator.isNull(request.getParameter("firstName"))) {
            log.warn("First Name is null");
            request.setAttribute("firstName", PropertyReader.getValue("error.require", "First Name"));
            pass = false;
        } else if (!DataValidator.isName(request.getParameter("firstName"))) {
            log.warn("Invalid First Name");
            request.setAttribute("firstName", "Invalid First Name");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("lastName"))) {
            log.warn("Last Name is null");
            request.setAttribute("lastName", PropertyReader.getValue("error.require", "Last Name"));
            pass = false;
        } else if (!DataValidator.isName(request.getParameter("lastName"))) {
            log.warn("Invalid Last Name");
            request.setAttribute("lastName", "Invalid Last Name");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("gender"))) {
            log.warn("Gender is null");
            request.setAttribute("gender", PropertyReader.getValue("error.require", "Gender"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("dob"))) {
            log.warn("DOB is null");
            request.setAttribute("dob", PropertyReader.getValue("error.require", "Date of Birth"));
            pass = false;
        } else if (!DataValidator.isDate(request.getParameter("dob"))) {
            log.warn("Invalid DOB format");
            request.setAttribute("dob", PropertyReader.getValue("error.date", "Date of Birth"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("email"))) {
            log.warn("Email is null");
            request.setAttribute("email", PropertyReader.getValue("error.require", "Email "));
            pass = false;
        } else if (!DataValidator.isEmail(request.getParameter("email"))) {
            log.warn("Invalid Email");
            request.setAttribute("email", PropertyReader.getValue("error.email", "Email "));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("mobileNo"))) {
            log.warn("Mobile No is null");
            request.setAttribute("mobileNo", PropertyReader.getValue("error.require", "Mobile No"));
            pass = false;
        } else if (!DataValidator.isPhoneLength(request.getParameter("mobileNo"))) {
            log.warn("Invalid Mobile No length");
            request.setAttribute("mobileNo", "Mobile No must have 10 digits");
            pass = false;
        } else if (!DataValidator.isPhoneNo(request.getParameter("mobileNo"))) {
            log.warn("Invalid Mobile No");
            request.setAttribute("mobileNo", "Invalid Mobile No");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("collegeId"))) {
            log.warn("College not selected");
            request.setAttribute("collegeId", PropertyReader.getValue("error.require", "College Name"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("courseId"))) {
            log.warn("Course not selected");
            request.setAttribute("courseId", PropertyReader.getValue("error.require", "Course Name"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("subjectId"))) {
            log.warn("Subject not selected");
            request.setAttribute("subjectId", PropertyReader.getValue("error.require", "Subject Name"));
            pass = false;
        }

        log.debug("FacultyCtl validate() completed with status: " + pass);
        return pass;
    }

    /**
     * Populates a {@link FacultyBean} from request parameters and fills audit
     * fields via {@link #populateDTO(BaseBean, HttpServletRequest)}.
     *
     * @param request the {@link HttpServletRequest} containing form data
     * @return populated {@link BaseBean} (actually a {@link FacultyBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("FacultyCtl populateBean() started");

        FacultyBean bean = new FacultyBean();

        bean.setId(DataUtility.getLong(request.getParameter("id")));
        bean.setFirstName(DataUtility.getString(request.getParameter("firstName")));
        bean.setLastName(DataUtility.getString(request.getParameter("lastName")));
        bean.setGender(DataUtility.getString(request.getParameter("gender")));
        bean.setDob(DataUtility.getDate(request.getParameter("dob")));
        bean.setMobileNo(DataUtility.getString(request.getParameter("mobileNo")));
        bean.setEmail(DataUtility.getString(request.getParameter("email")));
        bean.setCollegeId(DataUtility.getLong(request.getParameter("collegeId")));
        bean.setCourseId(DataUtility.getLong(request.getParameter("courseId")));
        bean.setSubjectId(DataUtility.getLong(request.getParameter("subjectId")));

        populateDTO(bean, request);

        log.debug("FacultyCtl populateBean() completed");
        return bean;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("FacultyCtl doGet() started");

        long id = DataUtility.getLong(request.getParameter("id"));
        FacultyModel model = new FacultyModel();

        if (id > 0) {
            try {
                FacultyBean bean = model.findByPk(id);
                ServletUtility.setBean(bean, request);
                log.info("Faculty record loaded for id: " + id);
            } catch (ApplicationException e) {
                log.error("ApplicationException while loading faculty", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        }

        ServletUtility.forward(getView(), request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("FacultyCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));
        FacultyModel model = new FacultyModel();
        long id = DataUtility.getLong(request.getParameter("id"));

        if (OP_SAVE.equalsIgnoreCase(op)) {
            FacultyBean bean = (FacultyBean) populateBean(request);
            try {
                long pk = model.add(bean);
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Faculty added successfully", request);
                log.info("Faculty added successfully with id: " + pk);
            } catch (DuplicateRecordException e) {
                log.warn("Duplicate faculty email detected", e);
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Email already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException while saving faculty", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }

        } else if (OP_UPDATE.equalsIgnoreCase(op)) {
            FacultyBean bean = (FacultyBean) populateBean(request);
            try {
                if (id > 0) {
                    model.update(bean);
                    log.info("Faculty updated for id: " + id);
                }
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Faculty updated successfully", request);
            } catch (DuplicateRecordException e) {
                log.warn("Duplicate faculty email on update", e);
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Email already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException while updating faculty", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }

        } else if (OP_CANCEL.equalsIgnoreCase(op)) {
            log.info("Cancel operation: redirecting to faculty list");
            ServletUtility.redirect(ORSView.FACULTY_LIST_CTL, request, response);
            return;

        } else if (OP_RESET.equalsIgnoreCase(op)) {
            log.info("Reset operation: redirecting to faculty form");
            ServletUtility.redirect(ORSView.FACULTY_CTL, request, response);
            return;
        }

        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Returns the JSP view path for the faculty form.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        return ORSView.FACULTY_VIEW;
    }
}
