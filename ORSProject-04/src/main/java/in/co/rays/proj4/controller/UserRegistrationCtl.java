package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.RoleBean;
import in.co.rays.proj4.bean.UserBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.model.UserModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * UserRegistrationCtl handles new user registration (Sign Up).
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Validate registration form input</li>
 *   <li>Populate {@link UserBean} from request parameters</li>
 *   <li>Call {@link UserModel#registerUser(UserBean)} to persist a new user</li>
 *   <li>Show success/error messages and forward/redirect to appropriate views</li>
 * </ul>
 * </p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.UserModel
 * @see in.co.rays.proj4.bean.UserBean
 */
@WebServlet(name = "UserRegistrationCtl", urlPatterns = { "/UserRegistrationCtl" })
public class UserRegistrationCtl extends BaseCtl {

    public static final String OP_SIGN_UP = "Sign Up";

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(UserRegistrationCtl.class);

    protected void preload(HttpServletRequest request) {
        log.debug("UserRegistrationCtl preload() called");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Female", "Female");
        map.put("Male", "Male");
        request.setAttribute("map", map);
        log.info("Preloaded gender map with 2 entries");
    }

    /**
     * Validates the registration form parameters.
     * <ul>
     *   <li>firstName and lastName must be present and valid names</li>
     *   <li>login must be present and a valid email</li>
     *   <li>password must meet length and strength requirements</li>
     *   <li>confirmPassword must match password</li>
     *   <li>gender and dob must be present (dob must be a valid date)</li>
     *   <li>mobileNo must be present, 10 digits and a valid phone number</li>
     * </ul>
     *
     * @param request the current {@link HttpServletRequest}
     * @return {@code true} if validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {
        log.debug("UserRegistrationCtl validate() called");
        boolean pass = true;

        if (DataValidator.isNull(request.getParameter("firstName"))) {
            request.setAttribute("firstName", PropertyReader.getValue("error.require", "First Name"));
            pass = false;
            log.warn("Validation failed: firstName is null");
        } else if (!DataValidator.isName(request.getParameter("firstName"))) {
            request.setAttribute("firstName", "Invalid First Name");
            pass = false;
            log.warn("Validation failed: firstName invalid");
        }

        if (DataValidator.isNull(request.getParameter("lastName"))) {
            request.setAttribute("lastName", PropertyReader.getValue("error.require", "Last Name"));
            pass = false;
            log.warn("Validation failed: lastName is null");
        } else if (!DataValidator.isName(request.getParameter("lastName"))) {
            request.setAttribute("lastName", "Invalid Last Name");
            pass = false;
            log.warn("Validation failed: lastName invalid");
        }

        if (DataValidator.isNull(request.getParameter("login"))) {
            request.setAttribute("login", PropertyReader.getValue("error.require", "Login Id"));
            pass = false;
            log.warn("Validation failed: login is null");
        } else if (!DataValidator.isEmail(request.getParameter("login"))) {
            request.setAttribute("login", PropertyReader.getValue("error.email", "Login"));
            pass = false;
            log.warn("Validation failed: login not a valid email");
        }

        if (DataValidator.isNull(request.getParameter("password"))) {
            request.setAttribute("password", PropertyReader.getValue("error.require", "Password"));
            pass = false;
            log.warn("Validation failed: password is null");
        } else if (!DataValidator.isPasswordLength(request.getParameter("password"))) {
            request.setAttribute("password", "Password should be 8 to 12 characters");
            pass = false;
            log.warn("Validation failed: password length invalid");
        } else if (!DataValidator.isPassword(request.getParameter("password"))) {
            request.setAttribute("password", "Must contain uppercase, lowercase, digit & special character");
            pass = false;
            log.warn("Validation failed: password strength invalid");
        }

        if (DataValidator.isNull(request.getParameter("confirmPassword"))) {
            request.setAttribute("confirmPassword", PropertyReader.getValue("error.require", "Confirm Password"));
            pass = false;
            log.warn("Validation failed: confirmPassword is null");
        }

        if (DataValidator.isNull(request.getParameter("gender"))) {
            request.setAttribute("gender", PropertyReader.getValue("error.require", "Gender"));
            pass = false;
            log.warn("Validation failed: gender is null");
        }

        if (DataValidator.isNull(request.getParameter("dob"))) {
            request.setAttribute("dob", PropertyReader.getValue("error.require", "Date of Birth"));
            pass = false;
            log.warn("Validation failed: dob is null");
        } else if (!DataValidator.isDate(request.getParameter("dob"))) {
            request.setAttribute("dob", PropertyReader.getValue("error.date", "Date of Birth"));
            pass = false;
            log.warn("Validation failed: dob invalid date");
        }

        if (!request.getParameter("password").equals(request.getParameter("confirmPassword"))
                && !"".equals(request.getParameter("confirmPassword"))) {
            request.setAttribute("confirmPassword", "Password and Confirm Password must be Same!");
            pass = false;
            log.warn("Validation failed: password and confirmPassword mismatch");
        }

        if (DataValidator.isNull(request.getParameter("mobileNo"))) {
            request.setAttribute("mobileNo", PropertyReader.getValue("error.require", "Mobile No"));
            pass = false;
            log.warn("Validation failed: mobileNo is null");
        } else if (!DataValidator.isPhoneLength(request.getParameter("mobileNo"))) {
            request.setAttribute("mobileNo", "Mobile No must have 10 digits");
            pass = false;
            log.warn("Validation failed: mobileNo length invalid");
        } else if (!DataValidator.isPhoneNo(request.getParameter("mobileNo"))) {
            request.setAttribute("mobileNo", "Invalid Mobile No");
            pass = false;
            log.warn("Validation failed: mobileNo invalid");
        }

        return pass;
    }

    /**
     * Populates a {@link UserBean} from registration form parameters.
     * The role is set to {@link RoleBean#STUDENT} for self-registration.
     *
     * @param request the current {@link HttpServletRequest}
     * @return populated {@link BaseBean} (actually {@link UserBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {
        log.debug("UserRegistrationCtl populateBean() called");
        UserBean bean = new UserBean();

        bean.setFirstName(DataUtility.getString(request.getParameter("firstName")));
        bean.setLastName(DataUtility.getString(request.getParameter("lastName")));
        bean.setLogin(DataUtility.getString(request.getParameter("login")));
        bean.setPassword(DataUtility.getString(request.getParameter("password")));
        bean.setConfirmPassword(DataUtility.getString(request.getParameter("confirmPassword")));
        bean.setGender(DataUtility.getString(request.getParameter("gender")));
        bean.setDob(DataUtility.getDate(request.getParameter("dob")));
        bean.setMobileNo(DataUtility.getString(request.getParameter("mobileNo")));
        bean.setRoleId(RoleBean.STUDENT);
        populateDTO(bean, request);

        log.info("Populated UserBean for registration: " + bean.getLogin());
        return bean;
    }

    /**
     * Forwards GET requests to the registration view.
     *
     * @param request  the current {@link HttpServletRequest}
     * @param response the current {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("UserRegistrationCtl doGet() called, forwarding to view");
        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Handles registration form submission. On Sign Up operation attempts to
     * register the user via {@link UserModel#registerUser(UserBean)} and sets
     * appropriate success or error messages. Also supports Reset operation.
     *
     * @param request  the current {@link HttpServletRequest}
     * @param response the current {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("UserRegistrationCtl doPost() called");
        String op = DataUtility.getString(request.getParameter("operation"));
        if (OP_SIGN_UP.equalsIgnoreCase(op)) {
            log.info("Sign Up operation triggered");
            UserModel model = new UserModel();
            UserBean bean = (UserBean) populateBean(request);
            try {
                model.registerUser(bean);
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Registration successful!", request);
                log.info("User registered successfully: " + bean.getLogin());
            } catch (ApplicationException e) {
                log.error("ApplicationException in doPost() during registration", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            } catch (DuplicateRecordException e) {
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Login id already exists", request);
                log.warn("Duplicate login during registration: " + bean.getLogin());
            }
        } else if (OP_RESET.equalsIgnoreCase(op)) {
            log.info("Reset operation triggered, redirecting to registration page");
            ServletUtility.redirect(ORSView.USER_REGISTRATION_CTL, request, response);
            return;
        }
        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Returns the JSP view path for user registration.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning UserRegistration view page");
        return ORSView.USER_REGISTRATION_VIEW;
    }
}
