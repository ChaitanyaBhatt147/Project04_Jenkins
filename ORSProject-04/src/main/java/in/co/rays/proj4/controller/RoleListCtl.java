package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.RoleBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.model.RoleModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * RoleListCtl handles listing, searching, pagination and bulk actions for Role
 * entities. It preloads role list data for the view, populates {@link RoleBean}
 * from request parameters, delegates search/delete operations to
 * {@link RoleModel}, and prepares pagination metadata for the view.
 * <p>
 * Supported operations include Search, Next, Previous, New, Delete, Reset and Back.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.RoleModel
 * @see in.co.rays.proj4.bean.RoleBean
 */
@WebServlet(name = "RoleListCtl", urlPatterns = { "/ctl/RoleListCtl" })
public class RoleListCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(RoleListCtl.class);

    /**
     * Preloads the list of roles and sets it as request attribute "roleList".
     * This is used to populate dropdowns or auxiliary lists in the view.
     *
     * @param request the {@link HttpServletRequest}
     */
    @Override
    protected void preload(HttpServletRequest request) {

        log.debug("RoleListCtl preload() started");

        RoleModel roleModel = new RoleModel();

        try {
            List roleList = roleModel.list();
            request.setAttribute("roleList", roleList);
            log.info("Role list preloaded successfully. Total roles: " + roleList.size());
        } catch (ApplicationException e) {
            log.error("Error while preloading role list", e);
            e.printStackTrace();
        }
    }

    /**
     * Populates a {@link RoleBean} from request parameters for search or other operations.
     *
     * @param request the {@link HttpServletRequest} containing parameters
     * @return populated {@link BaseBean} (actually a {@link RoleBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("RoleListCtl populateBean() called");

        RoleBean bean = new RoleBean();

        bean.setName(DataUtility.getString(request.getParameter("name")));
        bean.setId(DataUtility.getLong(request.getParameter("roleId")));

        return bean;
    }

    /**
     * Handles HTTP GET requests. Performs an initial search and forwards the result
     * list to the view. If no records are found, an error message is set.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("RoleListCtl doGet() started");

        int pageNo = 1;
        int pageSize = DataUtility.getInt(PropertyReader.getValue("page.size"));

        log.debug("PageNo: " + pageNo + ", PageSize: " + pageSize);

        RoleBean bean = (RoleBean) populateBean(request);
        RoleModel model = new RoleModel();

        try {
            List<RoleBean> list = model.search(bean, pageNo, pageSize);
            List<RoleBean> next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.isEmpty()) {
                log.warn("No role records found");
                ServletUtility.setErrorMessage("No record found", request);
            }

            ServletUtility.setList(list, request);
            ServletUtility.setPageNo(pageNo, request);
            ServletUtility.setPageSize(pageSize, request);
            ServletUtility.setBean(bean, request);
            request.setAttribute("nextListSize", next.size());

            ServletUtility.forward(getView(), request, response);

        } catch (ApplicationException e) {
            log.error("ApplicationException in RoleListCtl doGet()", e);
            e.printStackTrace();
            ServletUtility.handleException(e, request, response);
            return;
        }

        log.info("RoleListCtl doGet() completed");
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

        log.info("RoleListCtl doPost() started");

        List list = null;
        List next = null;

        int pageNo = DataUtility.getInt(request.getParameter("pageNo"));
        int pageSize = DataUtility.getInt(request.getParameter("pageSize"));

        pageNo = (pageNo == 0) ? 1 : pageNo;
        pageSize = (pageSize == 0)
                ? DataUtility.getInt(PropertyReader.getValue("page.size"))
                : pageSize;

        log.debug("PageNo: " + pageNo + ", PageSize: " + pageSize);

        RoleBean bean = (RoleBean) populateBean(request);
        RoleModel model = new RoleModel();

        String op = DataUtility.getString(request.getParameter("operation"));
        String[] ids = request.getParameterValues("ids");

        log.debug("Operation: " + op);

        try {

            if (OP_SEARCH.equalsIgnoreCase(op)
                    || "Next".equalsIgnoreCase(op)
                    || "Previous".equalsIgnoreCase(op)) {

                if (OP_SEARCH.equalsIgnoreCase(op)) {
                    pageNo = 1;
                } else if (OP_NEXT.equalsIgnoreCase(op)) {
                    pageNo++;
                } else if (OP_PREVIOUS.equalsIgnoreCase(op) && pageNo > 1) {
                    pageNo--;
                }

            } else if (OP_NEW.equalsIgnoreCase(op)) {

                log.info("Redirecting to User Controller");
                ServletUtility.redirect(ORSView.USER_CTL, request, response);
                return;

            } else if (OP_DELETE.equalsIgnoreCase(op)) {

                log.info("Delete operation triggered");
                pageNo = 1;

                if (ids != null && ids.length > 0) {
                    RoleBean deletebean = new RoleBean();
                    for (String id : ids) {
                        deletebean.setId(DataUtility.getInt(id));
                        model.delete(deletebean);
                        log.info("Deleted role id: " + id);
                        ServletUtility.setSuccessMessage("Data is deleted successfully", request);
                    }
                } else {
                    log.warn("Delete requested without selecting records");
                    ServletUtility.setErrorMessage("Select at least one record", request);
                }

            } else if (OP_RESET.equalsIgnoreCase(op)) {

                log.info("Reset operation triggered");
                ServletUtility.redirect(ORSView.USER_LIST_CTL, request, response);
                return;

            } else if (OP_BACK.equalsIgnoreCase(op)) {

                log.info("Back operation triggered");
                ServletUtility.redirect(ORSView.USER_LIST_CTL, request, response);
                return;
            }

            list = model.search(bean, pageNo, pageSize);
            next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.size() == 0) {
                log.warn("No role records found after operation");
                ServletUtility.setErrorMessage("No record found ", request);
            }

            ServletUtility.setList(list, request);
            ServletUtility.setPageNo(pageNo, request);
            ServletUtility.setPageSize(pageSize, request);
            ServletUtility.setBean(bean, request);
            request.setAttribute("nextListSize", next.size());

            ServletUtility.forward(getView(), request, response);

        } catch (ApplicationException e) {
            log.error("ApplicationException in RoleListCtl doPost()", e);
            e.printStackTrace();
            ServletUtility.handleException(e, request, response);
            return;
        }

        log.info("RoleListCtl doPost() completed");
    }

    /**
     * Returns the JSP view path for the role list.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning Role List View");
        return ORSView.ROLE_LIST_VIEW;
    }
}
