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
import in.co.rays.proj4.model.StudentModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * StudentListCtl handles listing, searching, pagination and bulk actions for
 * Student entities. It populates {@link StudentBean} from request parameters,
 * delegates search/delete operations to {@link StudentModel}, and prepares
 * data and pagination metadata for the student list view.
 * <p>
 * Supported operations include Search, Next, Previous, New, Delete, Reset and Back.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.StudentModel
 * @see in.co.rays.proj4.bean.StudentBean
 */
@WebServlet(name = "StudentListCtl", urlPatterns = { "/ctl/StudentListCtl" })
public class StudentListCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(StudentListCtl.class);

    /**
     * Populates a {@link StudentBean} from request parameters for use in search
     * or other operations.
     *
     * @param request the {@link HttpServletRequest} containing parameters
     * @return populated {@link BaseBean} (actually a {@link StudentBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("StudentListCtl populateBean() called");

        StudentBean bean = new StudentBean();

        bean.setFirstName(DataUtility.getString(request.getParameter("firstName")));
        bean.setLastName(DataUtility.getString(request.getParameter("lastName")));
        bean.setEmail(DataUtility.getString(request.getParameter("email")));

        return bean;
    }

    /**
     * Handles HTTP GET requests. Performs an initial search and forwards the
     * result list to the view. If no records are found, an error message is set.
     *
     * @param request  the {@link HttpServletRequest}
     * @param response the {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("StudentListCtl doGet() started");

        int pageNo = 1;
        int pageSize = DataUtility.getInt(PropertyReader.getValue("page.size"));

        StudentBean bean = (StudentBean) populateBean(request);
        StudentModel model = new StudentModel();

        try {
            log.debug("Searching students for pageNo=" + pageNo + ", pageSize=" + pageSize);

            List<StudentBean> list = model.search(bean, pageNo, pageSize);
            List<StudentBean> next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.isEmpty()) {
                log.warn("No student records found");
                ServletUtility.setErrorMessage("No record found", request);
            } else {
                log.info("Student records fetched: " + list.size());
            }

            ServletUtility.setList(list, request);
            ServletUtility.setPageNo(pageNo, request);
            ServletUtility.setPageSize(pageSize, request);
            ServletUtility.setBean(bean, request);
            request.setAttribute("nextListSize", next.size());

            ServletUtility.forward(getView(), request, response);

        } catch (ApplicationException e) {
            log.error("ApplicationException in StudentListCtl doGet()", e);
            e.printStackTrace();
            ServletUtility.handleException(e, request, response);
            return;
        }

        log.info("StudentListCtl doGet() completed");
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

        log.info("StudentListCtl doPost() started");

        List list = null;
        List next = null;

        int pageNo = DataUtility.getInt(request.getParameter("pageNo"));
        int pageSize = DataUtility.getInt(request.getParameter("pageSize"));

        pageNo = (pageNo == 0) ? 1 : pageNo;
        pageSize = (pageSize == 0)
                ? DataUtility.getInt(PropertyReader.getValue("page.size"))
                : pageSize;

        StudentBean bean = (StudentBean) populateBean(request);
        StudentModel model = new StudentModel();

        String op = DataUtility.getString(request.getParameter("operation"));
        String[] ids = request.getParameterValues("ids");

        log.debug("Operation received: " + op);

        try {

            if (OP_SEARCH.equalsIgnoreCase(op) || "Next".equalsIgnoreCase(op)
                    || "Previous".equalsIgnoreCase(op)) {

                if (OP_SEARCH.equalsIgnoreCase(op)) {
                    log.debug("Search operation");
                    pageNo = 1;
                } else if (OP_NEXT.equalsIgnoreCase(op)) {
                    log.debug("Next page operation");
                    pageNo++;
                } else if (OP_PREVIOUS.equalsIgnoreCase(op) && pageNo > 1) {
                    log.debug("Previous page operation");
                    pageNo--;
                }

            } else if (OP_NEW.equalsIgnoreCase(op)) {

                log.info("Redirecting to StudentCtl");
                ServletUtility.redirect(ORSView.STUDENT_CTL, request, response);
                return;

            } else if (OP_DELETE.equalsIgnoreCase(op)) {

                log.info("Delete operation triggered");
                pageNo = 1;

                if (ids != null && ids.length > 0) {
                    StudentBean deletebean = new StudentBean();
                    for (String id : ids) {
                        deletebean.setId(DataUtility.getInt(id));
                        model.delete(deletebean);
                        log.info("Student deleted with id: " + id);
                        ServletUtility.setSuccessMessage("Student is deleted successfully", request);
                    }
                } else {
                    log.warn("Delete attempted without selecting records");
                    ServletUtility.setErrorMessage("Select at least one record", request);
                }

            } else if (OP_RESET.equalsIgnoreCase(op)) {

                log.info("Reset operation triggered");
                ServletUtility.redirect(ORSView.STUDENT_LIST_CTL, request, response);
                return;

            } else if (OP_BACK.equalsIgnoreCase(op)) {

                log.info("Back operation triggered");
                ServletUtility.redirect(ORSView.STUDENT_LIST_CTL, request, response);
                return;
            }

            list = model.search(bean, pageNo, pageSize);
            next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.size() == 0) {
                log.warn("No student records found after operation");
                ServletUtility.setErrorMessage("No record found ", request);
            }

            ServletUtility.setList(list, request);
            ServletUtility.setPageNo(pageNo, request);
            ServletUtility.setPageSize(pageSize, request);
            ServletUtility.setBean(bean, request);
            request.setAttribute("nextListSize", next.size());

            ServletUtility.forward(getView(), request, response);

        } catch (ApplicationException e) {
            log.error("ApplicationException in StudentListCtl doPost()", e);
            e.printStackTrace();
            ServletUtility.handleException(e, request, response);
            return;
        }

        log.info("StudentListCtl doPost() completed");
    }

    /**
     * Returns the JSP view path for the student list.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning Student List View");
        return ORSView.STUDENT_LIST_VIEW;
    }
}
