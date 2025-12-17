package in.co.rays.proj4.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.UserBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.RecordNotFoundException;
import in.co.rays.proj4.model.UserModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * ForgetPasswordCtl handles "forgot password" requests.
 * <p>
 * It validates the user's email (login), populates a {@link UserBean} from the
 * request, and delegates the password-recovery operation to {@link UserModel}.
 * On successful processing, a success message is set; if the email is not
 * registered, an error message is shown.
 * </p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 */
@WebServlet(name = "ForgetPasswordCtl", urlPatterns = { "/ForgetPasswordCtl" })
public class ForgetPasswordCtl extends BaseCtl {

    private static final Logger log = Logger.getLogger(ForgetPasswordCtl.class);

    /**
     * Validates the forget-password form.
     * <ul>
     *   <li>login (email) is required</li>
     *   <li>login must be a valid email format</li>
     * </ul>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return {@code true} if validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {

        log.debug("ForgetPasswordCtl validate() started");

        boolean pass = true;

        if (DataValidator.isNull(request.getParameter("login"))) {
            log.warn("Login (email) is null");
            request.setAttribute("login", PropertyReader.getValue("error.require", "Email Id"));
            pass = false;
        } else if (!DataValidator.isEmail(request.getParameter("login"))) {
            log.warn("Invalid email format for login");
            request.setAttribute("login", PropertyReader.getValue("error.email", "Login "));
            pass = false;
        }

        log.debug("ForgetPasswordCtl validate() completed with status: " + pass);
        return pass;
    }

    /**
     * Populates a {@link UserBean} with the login (email) parameter from the
     * request. This bean is then used to invoke the forget-password operation.
     *
     * @param request the {@link HttpServletRequest} containing parameters
     * @return populated {@link BaseBean} (actually a {@link UserBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("ForgetPasswordCtl populateBean() called");

        UserBean bean = new UserBean();

        bean.setLogin(DataUtility.getString(request.getParameter("login")));

        return bean;
    }

    /**
     * Handles HTTP GET requests by forwarding to the forget-password view.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("ForgetPasswordCtl doGet() called");
        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Handles HTTP POST requests. When the operation is {@link BaseCtl#OP_GO},
     * it attempts to send the forgotten password to the user's registered email
     * by calling {@link UserModel#forgetPassword(String)}. Appropriate success
     * or error messages are set on the request based on the outcome.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("ForgetPasswordCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));
        log.debug("Operation received: " + op);

        UserBean bean = (UserBean) populateBean(request);
        UserModel model = new UserModel();

        if (OP_GO.equalsIgnoreCase(op)) {
            try {
                log.info("Processing forget password for login: " + bean.getLogin());
                boolean flag = model.forgetPassword(bean.getLogin());
                if (flag) {
                    log.info("Forget password email sent successfully");
                    ServletUtility.setSuccessMessage("Password has been sent to your email id", request);
                }
            } catch (RecordNotFoundException e) {
                log.warn("Login not found for forget password", e);
                ServletUtility.setErrorMessage(e.getMessage(), request);
            } catch (ApplicationException e) {
                log.error("ApplicationException during forget password", e);
                e.printStackTrace();
                ServletUtility.setErrorMessage("Please check your internet connection..!!", request);
            }
            ServletUtility.forward(getView(), request, response);
        }
    }

    /**
     * Returns the JSP view path for the forget-password page.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        return ORSView.FORGET_PASSWORD_VIEW;
    }
}
