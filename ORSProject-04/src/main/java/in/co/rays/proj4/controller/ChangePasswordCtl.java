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
import in.co.rays.proj4.exception.RecordNotFoundException;
import in.co.rays.proj4.model.UserModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * Controller to handle change-password requests.
 * <p>
 * This servlet provides validation for changing password, populates a UserBean
 * from request parameters, and delegates password update operations to
 * {@link in.co.rays.proj4.model.UserModel}. It also supports redirecting to
 * the user's profile page.
 * </p>
 * 
 * author Chaitanya Bhatt
 * @version 1.0
 */
@WebServlet(name = "ChangePasswordCtl", urlPatterns = { "/ctl/ChangePasswordCtl" })
public class ChangePasswordCtl extends BaseCtl {

    private static final Logger log = Logger.getLogger(ChangePasswordCtl.class);

    /** Operation constant for "Change My Profile" action. */
    public static final String OP_CHANGE_MY_PROFILE = "Change My Profile";

    /**
     * Validates the change-password form.
     * <p>
     * Validation rules:
     * <ul>
     *   <li>If operation equals {@link #OP_CHANGE_MY_PROFILE} validation is skipped.</li>
     *   <li>oldPassword must be present.</li>
     *   <li>newPassword must be present, 8-12 characters and satisfy password complexity.</li>
     *   <li>newPassword must differ from oldPassword.</li>
     *   <li>confirmPassword must match newPassword.</li>
     * </ul>
     * </p>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return {@code true} when validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {

        log.debug("ChangePasswordCtl validate() started");

        boolean pass = true;

        String op = request.getParameter("operation");
        log.debug("Operation received: " + op);

        if (OP_CHANGE_MY_PROFILE.equalsIgnoreCase(op)) {
            log.info("Validation skipped for Change My Profile operation");
            return pass;
        }

        if (DataValidator.isNull(request.getParameter("oldPassword"))) {
            log.warn("Old password is missing");
            request.setAttribute("oldPassword", PropertyReader.getValue("error.require", "Old Password"));
            pass = false;
        } else if (request.getParameter("oldPassword").equals(request.getParameter("newPassword"))) {
            log.warn("Old password and new password are same");
            request.setAttribute("newPassword", "Old and New passwords should be different");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("newPassword"))) {
            log.warn("New password is missing");
            request.setAttribute("newPassword", PropertyReader.getValue("error.require", "New Password"));
            pass = false;
        } else if (!DataValidator.isPasswordLength(request.getParameter("newPassword"))) {
            log.warn("New password length validation failed");
            request.setAttribute("newPassword", "Password should be 8 to 12 characters");
            pass = false;
        } else if (!DataValidator.isPassword(request.getParameter("newPassword"))) {
            log.warn("New password complexity validation failed");
            request.setAttribute("newPassword", "Must contain uppercase, lowercase, digit & special character");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("confirmPassword"))) {
            log.warn("Confirm password is missing");
            request.setAttribute("confirmPassword", PropertyReader.getValue("error.require", "Confirm Password"));
            pass = false;
        }

        if (!request.getParameter("newPassword").equals(request.getParameter("confirmPassword"))
                && !"".equals(request.getParameter("confirmPassword"))) {
            log.warn("New password and confirm password do not match");
            request.setAttribute("confirmPassword", "New and confirm passwords not matched");
            pass = false;
        }

        log.debug("ChangePasswordCtl validate() completed with status: " + pass);
        return pass;
    }

    /**
     * Populates a {@link UserBean} from request parameters.
     * <p>
     * Only relevant password fields are set here; audit fields are populated via
     * {@link #populateDTO(BaseBean, HttpServletRequest)}.
     * </p>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return populated {@link BaseBean} (actually a {@link UserBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("ChangePasswordCtl populateBean() started");

        UserBean bean = new UserBean();

        bean.setPassword(DataUtility.getString(request.getParameter("oldPassword")));
        bean.setConfirmPassword(DataUtility.getString(request.getParameter("confirmPassword")));

        populateDTO(bean, request);

        log.debug("ChangePasswordCtl populateBean() completed");
        return bean;
    }

    /**
     * Handles HTTP GET requests by forwarding to the change-password view.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("ChangePasswordCtl doGet() called");
        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Handles HTTP POST requests for changing the password or redirecting to
     * the profile page.
     * <p>
     * If operation equals {@link BaseCtl#OP_SAVE} this method attempts to change
     * the user's password via {@link UserModel#changePassword(long, String, String)}.
     * On success, the updated user is loaded into session and a success message is set.
     * If the user selects "Change My Profile" the method redirects to the profile controller.
     * </p>
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("ChangePasswordCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));
        String newPassword = (String) request.getParameter("newPassword");

        log.debug("Operation: " + op);

        UserBean bean = (UserBean) populateBean(request);
        UserModel model = new UserModel();

        HttpSession session = request.getSession(true);
        UserBean user = (UserBean) session.getAttribute("user");
        long id = user.getId();

        if (OP_SAVE.equalsIgnoreCase(op)) {
            try {
                log.info("Attempting password change for userId: " + id);
                boolean flag = model.changePassword(id, bean.getPassword(), newPassword);
                if (flag == true) {
                    log.info("Password changed successfully");
                    bean = model.findByLogin(user.getLogin());
                    session.setAttribute("user", bean);
                    ServletUtility.setBean(bean, request);
                    ServletUtility.setSuccessMessage("Password has been changed Successfully", request);
                }
            } catch (RecordNotFoundException e) {
                log.warn("Invalid old password entered", e);
                ServletUtility.setErrorMessage("Old Password is Invalid", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException occurred while changing password", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        } else if (OP_CHANGE_MY_PROFILE.equalsIgnoreCase(op)) {
            log.info("Redirecting to My Profile controller");
            ServletUtility.redirect(ORSView.MY_PROFILE_CTL, request, response);
            return;
        }

        log.info("Forwarding to Change Password view");
        ServletUtility.forward(ORSView.CHANGE_PASSWORD_VIEW, request, response);
    }

    /**
     * Returns the view path for the change-password page.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        return ORSView.CHANGE_PASSWORD_VIEW;
    }
}
