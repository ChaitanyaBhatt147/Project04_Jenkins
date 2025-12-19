package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.UserBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.model.RoleModel;
import in.co.rays.proj4.model.UserModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * UserListCtl handles listing, searching, pagination and bulk actions for User
 * entities. It preloads role list for the view, populates a {@link UserBean}
 * from request parameters, delegates search/delete operations to
 * {@link UserModel}, and prepares pagination metadata for the user list view.
 * <p>
 * Supported operations include Search, Next, Previous, New, Delete, Reset and Back.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.UserModel
 * @see in.co.rays.proj4.model.RoleModel
 * @see in.co.rays.proj4.bean.UserBean
 */
@WebServlet(name = "UserListCtl", urlPatterns = { "/ctl/UserListCtl" })
public class UserListCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(UserListCtl.class);

    /**
     * Preloads role list and sets it as request attribute "roleList" for the user
     * list view so role filters/dropdowns can be populated.
     *
     * @param request the {@link HttpServletRequest}
     */
    @Override
    protected void preload(HttpServletRequest request) {
        log.debug("UserListCtl preload() called");
        
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Female", "Female");
        map.put("Male", "Male");
        request.setAttribute("map", map);

        RoleModel roleModel = new RoleModel();
        try {
            List roleList = roleModel.list();
            request.setAttribute("roleList", roleList);
            log.info("Preloaded role list, size=" + roleList.size());
        } catch (ApplicationException e) {
            log.error("ApplicationException in preload()", e);
            e.printStackTrace();
        }
    }

    /**
     * Populates a {@link UserBean} from request parameters for use in searching or
     * filtering the user list.
     *
     * @param request the {@link HttpServletRequest} containing parameters
     * @return populated {@link BaseBean} (actually a {@link UserBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {
        log.debug("UserListCtl populateBean() called");

        UserBean bean = new UserBean();
        bean.setFirstName(DataUtility.getString(request.getParameter("firstName")));
        bean.setLogin(DataUtility.getString(request.getParameter("login")));
        bean.setRoleId(DataUtility.getLong(request.getParameter("roleId")));
        bean.setDob(DataUtility.getDate(request.getParameter("dob")));

        return bean;
    }

    /**
     * Handles HTTP GET requests. Performs an initial search (page 1) and forwards
     * the result list to the view. If no records are found, an error message is set.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("UserListCtl doGet() started");

        int pageNo = 1;
        int pageSize = DataUtility.getInt(PropertyReader.getValue("page.size"));

        UserBean bean = (UserBean) populateBean(request);
        UserModel model = new UserModel();

        try {
            List<UserBean> list = model.search(bean, pageNo, pageSize);
            List<UserBean> next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.isEmpty()) {
                ServletUtility.setErrorMessage("No record found", request);
                log.info("No record found during initial search");
            }

            ServletUtility.setList(list, request);
            ServletUtility.setPageNo(pageNo, request);
            ServletUtility.setPageSize(pageSize, request);
            ServletUtility.setBean(bean, request);
            request.setAttribute("nextListSize", next.size());

            ServletUtility.forward(getView(), request, response);
            log.info("doGet() forwarded to view: " + getView());

        } catch (ApplicationException e) {
            log.error("ApplicationException in doGet()", e);
            e.printStackTrace();
            ServletUtility.handleException(e, request, response);
        }
    }

    /**
     * Handles HTTP POST requests for search, pagination, new, delete, reset and back
     * operations. After performing the requested operation it forwards the updated
     * list and pagination metadata to the view.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("UserListCtl doPost() started");

        List list = null;
        List next = null;

        int pageNo = DataUtility.getInt(request.getParameter("pageNo"));
        int pageSize = DataUtility.getInt(request.getParameter("pageSize"));

        pageNo = (pageNo == 0) ? 1 : pageNo;
        pageSize = (pageSize == 0) ? DataUtility.getInt(PropertyReader.getValue("page.size")) : pageSize;

        UserBean bean = (UserBean) populateBean(request);
        UserModel model = new UserModel();

        String op = DataUtility.getString(request.getParameter("operation"));
        String[] ids = request.getParameterValues("ids");

        try {

            if (OP_SEARCH.equalsIgnoreCase(op) || "Next".equalsIgnoreCase(op) || "Previous".equalsIgnoreCase(op)) {

                if (OP_SEARCH.equalsIgnoreCase(op)) {
                    pageNo = 1;
                    log.debug("Search operation, resetting pageNo to 1");
                } else if (OP_NEXT.equalsIgnoreCase(op)) {
                    pageNo++;
                    log.debug("Next operation, incrementing pageNo to " + pageNo);
                } else if (OP_PREVIOUS.equalsIgnoreCase(op) && pageNo > 1) {
                    pageNo--;
                    log.debug("Previous operation, decrementing pageNo to " + pageNo);
                }

            } else if (OP_NEW.equalsIgnoreCase(op)) {
                log.info("New operation, redirecting to USER_CTL");
                ServletUtility.redirect(ORSView.USER_CTL, request, response);
                return;

            } else if (OP_DELETE.equalsIgnoreCase(op)) {
                pageNo = 1;
                if (ids != null && ids.length > 0) {
                    UserBean deletebean = new UserBean();
                    for (String id : ids) {
                        deletebean.setId(DataUtility.getInt(id));
                        model.delete(deletebean);
                        ServletUtility.setSuccessMessage("User deleted successfully", request);
                        log.info("Deleted user with id=" + id);
                    }
                } else {
                    ServletUtility.setErrorMessage("Select at least one record", request);
                    log.warn("Delete operation attempted with no selection");
                }

            } else if (OP_RESET.equalsIgnoreCase(op)) {
                log.info("Reset operation, redirecting to USER_LIST_CTL");
                ServletUtility.redirect(ORSView.USER_LIST_CTL, request, response);
                return;

            } else if (OP_BACK.equalsIgnoreCase(op)) {
                log.info("Back operation, redirecting to USER_LIST_CTL");
                ServletUtility.redirect(ORSView.USER_LIST_CTL, request, response);
                return;
            }

            list = model.search(bean, pageNo, pageSize);
            next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.size() == 0) {
                ServletUtility.setErrorMessage("No record found ", request);
                log.info("No record found on page " + pageNo);
            }

            ServletUtility.setList(list, request);
            ServletUtility.setPageNo(pageNo, request);
            ServletUtility.setPageSize(pageSize, request);
            ServletUtility.setBean(bean, request);
            request.setAttribute("nextListSize", next.size());

            ServletUtility.forward(getView(), request, response);
            log.info("doPost() forwarded to view: " + getView());

        } catch (ApplicationException e) {
            log.error("ApplicationException in doPost()", e);
            e.printStackTrace();
            ServletUtility.handleException(e, request, response);
        }
    }

    /**
     * Returns the JSP view path for the user list.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning UserList view page");
        return ORSView.USER_LIST_VIEW;
    }
}
