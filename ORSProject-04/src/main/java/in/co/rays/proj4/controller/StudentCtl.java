package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.StudentBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.model.CollegeModel;
import in.co.rays.proj4.model.StudentModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * StudentCtl is a controller servlet responsible for handling student related
 * operations such as add, update, view and navigation. It validates student
 * form input, preloads college list for dropdowns, populates {@link StudentBean}
 * from request parameters and delegates persistence operations to
 * {@link StudentModel}.
 * <p>
 * Supported operations include Save, Update, Cancel and Reset.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.StudentModel
 * @see in.co.rays.proj4.bean.StudentBean
 */
@WebServlet(name = "StudentCtl", urlPatterns = { "/ctl/StudentCtl" })
public class StudentCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(StudentCtl.class);

    /**
     * Preloads the list of colleges and sets it as request attribute
     * "collegeList" so that the student form can render a college dropdown.
     *
     * @param request the {@link HttpServletRequest}
     */
    @Override
    protected void preload(HttpServletRequest request) {

        log.debug("StudentCtl preload() started");

        CollegeModel collegeModel = new CollegeModel();
        try {
            List collegeList = collegeModel.list();
            request.setAttribute("collegeList", collegeList);
            log.info("College list preloaded successfully. Total colleges: " + collegeList.size());
        } catch (ApplicationException e) {
            log.error("Error while preloading college list", e);
            e.printStackTrace();
        }
    }

    /**
     * Validates student form fields.
     * <ul>
     *   <li>firstName and lastName are required and must be valid names.</li>
     *   <li>mobileNo is required, must be 10 digits and a valid phone number.</li>
     *   <li>gender is required.</li>
     *   <li>email is required and must be a valid email address.</li>
     *   <li>collegeId is required.</li>
     *   <li>dob is required and must be a valid date.</li>
     * </ul>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return {@code true} if validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {

        log.debug("StudentCtl validate() started");

        boolean pass = true;

        if (DataValidator.isNull(request.getParameter("firstName"))) {
            log.warn("Validation failed: First Name is required");
            request.setAttribute("firstName", PropertyReader.getValue("error.require", "First Name"));
            pass = false;
        } else if (!DataValidator.isName(request.getParameter("firstName"))) {
            log.warn("Validation failed: Invalid First Name");
            request.setAttribute("firstName", "Invalid First Name");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("lastName"))) {
            log.warn("Validation failed: Last Name is required");
            request.setAttribute("lastName", PropertyReader.getValue("error.require", "Last Name"));
            pass = false;
        } else if (!DataValidator.isName(request.getParameter("lastName"))) {
            log.warn("Validation failed: Invalid Last Name");
            request.setAttribute("lastName", "Invalid Last Name");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("mobileNo"))) {
            log.warn("Validation failed: Mobile No is required");
            request.setAttribute("mobileNo", PropertyReader.getValue("error.require", "Mobile No"));
            pass = false;
        } else if (!DataValidator.isPhoneLength(request.getParameter("mobileNo"))) {
            log.warn("Validation failed: Mobile No length invalid");
            request.setAttribute("mobileNo", "Mobile No must have 10 digits");
            pass = false;
        } else if (!DataValidator.isPhoneNo(request.getParameter("mobileNo"))) {
            log.warn("Validation failed: Invalid Mobile No");
            request.setAttribute("mobileNo", "Invalid Mobile No");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("gender"))) {
            log.warn("Validation failed: Gender is required");
            request.setAttribute("gender", PropertyReader.getValue("error.require", "Gender"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("email"))) {
            log.warn("Validation failed: Email is required");
            request.setAttribute("email", PropertyReader.getValue("error.require", "Email "));
            pass = false;
        } else if (!DataValidator.isEmail(request.getParameter("email"))) {
            log.warn("Validation failed: Invalid Email");
            request.setAttribute("email", PropertyReader.getValue("error.email", "Email "));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("collegeId"))) {
            log.warn("Validation failed: College is required");
            request.setAttribute("collegeId", PropertyReader.getValue("error.require", "College Name"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("dob"))) {
            log.warn("Validation failed: Date of Birth is required");
            request.setAttribute("dob", PropertyReader.getValue("error.require", "Date of Birth"));
            pass = false;
        } else if (!DataValidator.isDate(request.getParameter("dob"))) {
            log.warn("Validation failed: Invalid Date of Birth");
            request.setAttribute("dob", PropertyReader.getValue("error.date", "Date of Birth"));
            pass = false;
        }

        return pass;
    }

    /**
     * Populates a {@link StudentBean} from request parameters and fills audit
     * information via {@link #populateDTO(BaseBean, HttpServletRequest)}.
     *
     * @param request the {@link HttpServletRequest} containing form data
     * @return populated {@link BaseBean} (actually a {@link StudentBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("StudentCtl populateBean() called");

        StudentBean bean = new StudentBean();

        bean.setId(DataUtility.getLong(request.getParameter("id")));
        bean.setFirstName(DataUtility.getString(request.getParameter("firstName")));
        bean.setLastName(DataUtility.getString(request.getParameter("lastName")));
        bean.setDob(DataUtility.getDate(request.getParameter("dob")));
        bean.setGender(DataUtility.getString(request.getParameter("gender")));
        bean.setMobileNo(DataUtility.getString(request.getParameter("mobileNo")));
        bean.setEmail(DataUtility.getString(request.getParameter("email")));
        bean.setCollegeId(DataUtility.getLong(request.getParameter("collegeId")));

        populateDTO(bean, request);

        return bean;
    }

    /**
     * Handles HTTP GET requests. If an 'id' parameter is provided (> 0), loads
     * the corresponding student record and places it on the request for editing/view.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("StudentCtl doGet() started");

        long id = DataUtility.getLong(request.getParameter("id"));
        StudentModel model = new StudentModel();

        if (id > 0) {
            try {
                StudentBean bean = model.findByPk(id);
                ServletUtility.setBean(bean, request);
                log.info("Student loaded with id: " + id);
            } catch (ApplicationException e) {
                log.error("Error fetching student record", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        }

        ServletUtility.forward(getView(), request, response);
        log.info("StudentCtl doGet() completed");
    }

    /**
     * Handles HTTP POST requests for saving and updating student records.
     * <ul>
     *   <li>OP_SAVE: Adds a new student (handles {@link DuplicateRecordException}).</li>
     *   <li>OP_UPDATE: Updates an existing student.</li>
     *   <li>OP_CANCEL: Redirects to student list controller.</li>
     *   <li>OP_RESET: Redirects back to student form.</li>
     * </ul>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("StudentCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));
        StudentModel model = new StudentModel();
        long id = DataUtility.getLong(request.getParameter("id"));

        log.debug("Operation: " + op);

        if (OP_SAVE.equalsIgnoreCase(op)) {

            StudentBean bean = (StudentBean) populateBean(request);

            try {
                long pk = model.add(bean);
                log.info("Student added successfully with PK: " + pk);
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Student added successfully", request);

            } catch (DuplicateRecordException e) {
                log.warn("Duplicate email while adding student");
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Email already exists", request);

            } catch (ApplicationException e) {
                log.error("ApplicationException while adding student", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }

        } else if (OP_UPDATE.equalsIgnoreCase(op)) {

            StudentBean bean = (StudentBean) populateBean(request);

            try {
                if (id > 0) {
                    model.update(bean);
                    log.info("Student updated with id: " + id);
                }
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Student updated successfully", request);

            } catch (DuplicateRecordException e) {
                log.warn("Duplicate email while updating student");
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Email already exists", request);

            } catch (ApplicationException e) {
                log.error("ApplicationException while updating student", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }

        } else if (OP_CANCEL.equalsIgnoreCase(op)) {

            log.info("Cancel operation triggered");
            ServletUtility.redirect(ORSView.STUDENT_LIST_CTL, request, response);
            return;

        } else if (OP_RESET.equalsIgnoreCase(op)) {

            log.info("Reset operation triggered");
            ServletUtility.redirect(ORSView.STUDENT_CTL, request, response);
            return;
        }

        ServletUtility.forward(getView(), request, response);
        log.info("StudentCtl doPost() completed");
    }

    /**
     * Returns the JSP view path for the student form.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning Student View");
        return ORSView.STUDENT_VIEW;
    }
}
