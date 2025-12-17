package in.co.rays.proj4.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.UserBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.model.UserModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * MyProfileCtl manages viewing and updating the profile of the currently
 * logged-in user. It validates profile inputs, populates a {@link UserBean}
 * from request parameters, and delegates update operations to {@link UserModel}.
 * It also supports navigation to change-password functionality.
 * <p>
 * Typical operations:
 * <ul>
 *   <li>Save — update profile details</li>
 *   <li>Change Password — redirect to change password controller</li>
 * </ul>
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.UserModel
 * @see in.co.rays.proj4.bean.UserBean
 */
@WebServlet(name = "MyProfileCtl", urlPatterns = { "/ctl/MyProfileCtl" })
public class MyProfileCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(MyProfileCtl.class);

    /** Operation constant to change password. */
    public static final String OP_CHANGE_MY_PASSWORD = "Change Password";

    /**
     * Validates profile form parameters.
     * <ul>
     *   <li>Skips validation for Change Password operation or when operation is null.</li>
     *   <li>firstName and lastName are required and must be valid names.</li>
     *   <li>gender is required.</li>
     *   <li>mobileNo is required, must be 10 digits and a valid phone number.</li>
     *   <li>dob is required.</li>
     * </ul>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return {@code true} if validation succeeds; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {

        log.debug("MyProfileCtl validate() started");

        boolean pass = true;

        String op = DataUtility.getString(request.getParameter("operation"));
        log.debug("Operation for validation: " + op);

        if (OP_CHANGE_MY_PASSWORD.equalsIgnoreCase(op) || op == null) {
            log.debug("Validation skipped for Change Password or null operation");
            return pass;
        }

        if (DataValidator.isNull(request.getParameter("firstName"))) {
            log.warn("First Name is required");
            request.setAttribute("firstName", PropertyReader.getValue("error.require", "First Name"));
            pass = false;
        } else if (!DataValidator.isName(request.getParameter("firstName"))) {
            log.warn("Invalid First Name");
            request.setAttribute("firstName", "Invalid First Name");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("lastName"))) {
            log.warn("Last Name is required");
            request.setAttribute("lastName", PropertyReader.getValue("error.require", "Last Name"));
            pass = false;
        } else if (!DataValidator.isName(request.getParameter("lastName"))) {
            log.warn("Invalid Last Name");
            request.setAttribute("lastName", "Invalid Last Name");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("gender"))) {
            log.warn("Gender is required");
            request.setAttribute("gender", PropertyReader.getValue("error.require", "Gender"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("mobileNo"))) {
            log.warn("Mobile No is required");
            request.setAttribute("mobileNo", PropertyReader.getValue("error.require", "MobileNo"));
            pass = false;
        } else if (!DataValidator.isPhoneLength(request.getParameter("mobileNo"))) {
            log.warn("Mobile No length invalid");
            request.setAttribute("mobileNo", "Mobile No must have 10 digits");
            pass = false;
        } else if (!DataValidator.isPhoneNo(request.getParameter("mobileNo"))) {
            log.warn("Invalid Mobile No");
            request.setAttribute("mobileNo", "Invalid Mobile No");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("dob"))) {
            log.warn("Date of Birth is required");
            request.setAttribute("dob", PropertyReader.getValue("error.require", "Date Of Birth"));
            pass = false;
        }

        log.debug("MyProfileCtl validate() ended with status: " + pass);
        return pass;
    }

    /**
     * Populates a {@link UserBean} from request parameters and sets audit fields
     * via {@link #populateDTO(BaseBean, HttpServletRequest)}.
     *
     * @param request the {@link HttpServletRequest} containing form data
     * @return populated {@link BaseBean} (actually a {@link UserBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("MyProfileCtl populateBean() started");

        UserBean bean = new UserBean();

        bean.setId(DataUtility.getLong(request.getParameter("id")));
        bean.setLogin(DataUtility.getString(request.getParameter("login")));
        bean.setFirstName(DataUtility.getString(request.getParameter("firstName")));
        bean.setLastName(DataUtility.getString(request.getParameter("lastName")));
        bean.setMobileNo(DataUtility.getString(request.getParameter("mobileNo")));
        bean.setGender(DataUtility.getString(request.getParameter("gender")));
        bean.setDob(DataUtility.getDate(request.getParameter("dob")));

        populateDTO(bean, request);

        log.debug("MyProfileCtl populateBean() ended");
        return bean;
    }

    /**
     * Handles HTTP GET requests. Loads the current user's profile from session
     * and retrieves the up-to-date data from {@link UserModel} for display.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("MyProfileCtl doGet() started");

        HttpSession session = request.getSession(true);
        UserBean user = (UserBean) session.getAttribute("user");
        long id = user.getId();

        UserModel model = new UserModel();

        if (id > 0) {
            try {
                log.debug("Fetching user profile for id: " + id);
                UserBean bean = model.findByPk(id);
                ServletUtility.setBean(bean, request);
            } catch (ApplicationException e) {
                log.error("Exception in MyProfileCtl doGet()", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        }

        ServletUtility.forward(getView(), request, response);
        log.info("MyProfileCtl doGet() ended");
    }

    /**
     * Handles HTTP POST requests for updating profile or redirecting to change
     * password. On successful update the session user is also updated.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("MyProfileCtl doPost() started");

        HttpSession session = request.getSession(true);

        UserBean user = (UserBean) session.getAttribute("user");
        long id = user.getId();

        String op = DataUtility.getString(request.getParameter("operation"));
        log.debug("Operation received: " + op);

        UserModel model = new UserModel();

        if (OP_SAVE.equalsIgnoreCase(op)) {
            UserBean bean = (UserBean) populateBean(request);
            try {
                if (id > 0) {
                    log.info("Updating user profile for id: " + id);
                    user.setFirstName(bean.getFirstName());
                    user.setLastName(bean.getLastName());
                    user.setGender(bean.getGender());
                    user.setMobileNo(bean.getMobileNo());
                    user.setDob(bean.getDob());
                    model.update(user);
                }
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Profile has been updated Successfully. ", request);
            } catch (DuplicateRecordException e) {
                log.warn("Duplicate record exception while updating profile");
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Login id already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException in MyProfileCtl doPost()", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        } else if (OP_CHANGE_MY_PASSWORD.equalsIgnoreCase(op)) {
            log.info("Redirecting to Change Password Controller");
            ServletUtility.redirect(ORSView.CHANGE_PASSWORD_CTL, request, response);
            return;
        }

        ServletUtility.forward(getView(), request, response);
        log.info("MyProfileCtl doPost() ended");
    }

    /**
     * Returns the JSP view path for the My Profile page.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning My Profile View");
        return ORSView.MY_PROFILE_VIEW;
    }
}
