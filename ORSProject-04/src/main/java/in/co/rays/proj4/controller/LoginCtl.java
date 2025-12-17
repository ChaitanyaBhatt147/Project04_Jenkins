package in.co.rays.proj4.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.RoleBean;
import in.co.rays.proj4.bean.UserBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.model.RoleModel;
import in.co.rays.proj4.model.UserModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * LoginCtl handles authentication-related requests including sign in, sign up
 * redirection and logout. It validates login form input, authenticates users via
 * {@link UserModel}, and stores user and role information in the session.
 *
 * <p>
 * Supported operations:
 * <ul>
 *   <li>{@link #OP_SIGN_IN} - Authenticate user and redirect to welcome page.</li>
 *   <li>{@link #OP_SIGN_UP} - Redirect to user registration controller.</li>
 *   <li>{@link #OP_LOG_OUT} - Invalidate session and display logout message.</li>
 * </ul>
 * </p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.UserModel
 * @see in.co.rays.proj4.model.RoleModel
 */
@WebServlet(name = "LoginCtl", urlPatterns = { "/LoginCtl" })
public class LoginCtl extends BaseCtl {

    private static final Logger log = Logger.getLogger(LoginCtl.class);

    public static final String OP_REGISTER = "Register";
    public static final String OP_SIGN_IN = "Sign In";
    public static final String OP_SIGN_UP = "Sign Up";
    public static final String OP_LOG_OUT = "Logout";

    /**
     * Validates login form input.
     * <ul>
     *   <li>Skips validation for Sign Up and Logout operations.</li>
     *   <li>login is required and must be a valid email.</li>
     *   <li>password is required.</li>
     * </ul>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return {@code true} if validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {

        log.debug("LoginCtl validate() started");

        boolean pass = true;

        String op = request.getParameter("operation");

        if (OP_SIGN_UP.equals(op) || OP_LOG_OUT.equals(op)) {
            log.debug("Validation skipped for operation: " + op);
            return pass;
        }

        if (DataValidator.isNull(request.getParameter("login"))) {
            log.warn("Login id is null");
            request.setAttribute("login", PropertyReader.getValue("error.require", "Login Id"));
            pass = false;
        } else if (!DataValidator.isEmail(request.getParameter("login"))) {
            log.warn("Invalid email format for login");
            request.setAttribute("login", PropertyReader.getValue("error.email", "Login "));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("password"))) {
            log.warn("Password is null");
            request.setAttribute("password", PropertyReader.getValue("error.require", "Password"));
            pass = false;
        }

        log.debug("LoginCtl validate() completed with status: " + pass);
        return pass;
    }

    /**
     * Populates a {@link UserBean} with credentials from the request.
     *
     * @param request the {@link HttpServletRequest} containing parameters
     * @return populated {@link BaseBean} (actually a {@link UserBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("LoginCtl populateBean() called");

        UserBean bean = new UserBean();
        bean.setId(DataUtility.getLong(request.getParameter("id")));
        bean.setLogin(DataUtility.getString(request.getParameter("login")));
        bean.setPassword(DataUtility.getString(request.getParameter("password")));

        return bean;
    }

    /**
     * Handles HTTP GET requests. If operation is {@link #OP_LOG_OUT}, invalidates
     * the session, sets a logout success message and forwards to the login view.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("LoginCtl doGet() called");

        HttpSession session = request.getSession();
        String op = DataUtility.getString(request.getParameter("operation"));

        if (OP_LOG_OUT.equals(op)) {
            log.info("Logout requested");
            session.invalidate();
            ServletUtility.setSuccessMessage("Logout Successful!", request);
            ServletUtility.forward(getView(), request, response);
            return;
        }

        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Handles HTTP POST requests for Sign In and Sign Up operations.
     * <ul>
     *   <li>OP_SIGN_IN: Authenticates user, stores {@link UserBean} and role in session,
     *       and redirects to welcome controller on success.</li>
     *   <li>OP_SIGN_UP: Redirects to user registration controller.</li>
     * </ul>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("LoginCtl doPost() started");

        HttpSession session = request.getSession();
        String op = DataUtility.getString(request.getParameter("operation"));

        UserModel model = new UserModel();
        RoleModel role = new RoleModel();

        if (OP_SIGN_IN.equalsIgnoreCase(op)) {

            log.info("Sign In operation initiated");

            UserBean bean = (UserBean) populateBean(request);

            try {
                bean = model.authenticate(bean.getLogin(), bean.getPassword());

                if (bean != null) {

                    log.info("Authentication successful for login: " + bean.getLogin());

                    session.setAttribute("user", bean);

                    RoleBean rolebean = role.findByPk(bean.getRoleId());
                    if (rolebean != null) {
                        session.setAttribute("role", rolebean.getName());
                        log.debug("User role set in session: " + rolebean.getName());
                    }

                    ServletUtility.redirect(ORSView.WELCOME_CTL, request, response);
                    return;

                } else {
                    log.warn("Authentication failed for login: " + bean.getLogin());
                    bean = (UserBean) populateBean(request);
                    ServletUtility.setBean(bean, request);
                    ServletUtility.setErrorMessage("Invalid LoginId And Password", request);
                }

            } catch (ApplicationException e) {
                log.error("ApplicationException during login", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }

        } else if (OP_SIGN_UP.equalsIgnoreCase(op)) {
            log.info("Sign Up requested – redirecting to registration");
            ServletUtility.redirect(ORSView.USER_REGISTRATION_CTL, request, response);
            return;
        }

        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Returns the JSP view path for the login page.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        return ORSView.LOGIN_VIEW;
    }
}
