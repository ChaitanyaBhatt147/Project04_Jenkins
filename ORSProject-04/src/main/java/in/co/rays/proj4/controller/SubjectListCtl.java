package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.SubjectBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.model.CourseModel;
import in.co.rays.proj4.model.SubjectModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * SubjectListCtl handles listing, searching, pagination and bulk actions for
 * Subject entities. It preloads subject and course lists for the view,
 * populates {@link SubjectBean} from request parameters, delegates search/delete
 * operations to {@link SubjectModel}, and prepares pagination metadata for the view.
 * <p>
 * Supported operations include Search, Next, Previous, New, Delete, Reset and Back.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.SubjectModel
 * @see in.co.rays.proj4.bean.SubjectBean
 */
@WebServlet(name = "SubjectListCtl", urlPatterns = { "/ctl/SubjectListCtl" })
public class SubjectListCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(SubjectListCtl.class);

    /**
     * Preloads subject and course lists and sets them as request attributes
     * ("subjectList", "courseList") for dropdowns or auxiliary displays in the view.
     *
     * @param request the {@link HttpServletRequest}
     */
    @Override
    protected void preload(HttpServletRequest request) {
        log.debug("SubjectListCtl preload() called");

        SubjectModel subjectModel = new SubjectModel();
        CourseModel courseModel = new CourseModel();

        try {
            List subjectList = subjectModel.list();
            request.setAttribute("subjectList", subjectList);
            log.info("Preloaded subject list, size=" + subjectList.size());

            List courseList = courseModel.list();
            request.setAttribute("courseList", courseList);
            log.info("Preloaded course list, size=" + courseList.size());

        } catch (ApplicationException e) {
            log.error("ApplicationException in preload()", e);
            e.printStackTrace();
        }
    }

    /**
     * Populates a {@link SubjectBean} from request parameters for use in search
     * or other operations.
     *
     * @param request the {@link HttpServletRequest} containing parameters
     * @return populated {@link BaseBean} (actually a {@link SubjectBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {
        log.debug("SubjectListCtl populateBean() called");

        SubjectBean bean = new SubjectBean();

        bean.setName(DataUtility.getString(request.getParameter("name")));
        bean.setCourseName(DataUtility.getString(request.getParameter("courseName")));
        bean.setDescription(DataUtility.getString(request.getParameter("description")));
        bean.setCourseId(DataUtility.getLong(request.getParameter("courseId")));
        bean.setId(DataUtility.getLong(request.getParameter("subjectId")));

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

        log.info("SubjectListCtl doGet() started");

        int pageNo = 1;
        int pageSize = DataUtility.getInt(PropertyReader.getValue("page.size"));

        SubjectBean bean = (SubjectBean) populateBean(request);
        SubjectModel model = new SubjectModel();

        try {
            List<SubjectBean> list = model.search(bean, pageNo, pageSize);
            List<SubjectBean> next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.isEmpty()) {
                ServletUtility.setErrorMessage("No record found", request);
                log.warn("No records found in doGet()");
            } else {
                log.info("Fetched " + list.size() + " records for pageNo=" + pageNo);
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
            return;
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

        log.info("SubjectListCtl doPost() started");

        List list = null;
        List next = null;

        int pageNo = DataUtility.getInt(request.getParameter("pageNo"));
        int pageSize = DataUtility.getInt(request.getParameter("pageSize"));

        pageNo = (pageNo == 0) ? 1 : pageNo;
        pageSize = (pageSize == 0) ? DataUtility.getInt(PropertyReader.getValue("page.size")) : pageSize;

        SubjectBean bean = (SubjectBean) populateBean(request);
        SubjectModel model = new SubjectModel();

        String op = DataUtility.getString(request.getParameter("operation"));
        String[] ids = request.getParameterValues("ids");

        try {

            if (OP_SEARCH.equalsIgnoreCase(op) || "Next".equalsIgnoreCase(op) || "Previous".equalsIgnoreCase(op)) {

                if (OP_SEARCH.equalsIgnoreCase(op)) {
                    pageNo = 1;
                } else if (OP_NEXT.equalsIgnoreCase(op)) {
                    pageNo++;
                } else if (OP_PREVIOUS.equalsIgnoreCase(op) && pageNo > 1) {
                    pageNo--;
                }
                log.debug("Pagination operation: " + op + ", pageNo=" + pageNo);
            } else if (OP_NEW.equalsIgnoreCase(op)) {
                log.info("New operation, redirecting to SUBJECT_CTL");
                ServletUtility.redirect(ORSView.SUBJECT_CTL, request, response);
                return;

            } else if (OP_DELETE.equalsIgnoreCase(op)) {
                pageNo = 1;
                if (ids != null && ids.length > 0) {
                    SubjectBean deletebean = new SubjectBean();
                    for (String id : ids) {
                        deletebean.setId(DataUtility.getInt(id));
                        model.delete(deletebean);
                        ServletUtility.setSuccessMessage("Data is deleted successfully", request);
                        log.info("Deleted Subject with id=" + id);
                    }
                } else {
                    ServletUtility.setErrorMessage("Select at least one record", request);
                    log.warn("Delete operation failed: No record selected");
                }

            } else if (OP_RESET.equalsIgnoreCase(op)) {
                log.info("Reset operation, redirecting to SUBJECT_LIST_CTL");
                ServletUtility.redirect(ORSView.SUBJECT_LIST_CTL, request, response);
                return;

            } else if (OP_BACK.equalsIgnoreCase(op)) {
                log.info("Back operation, redirecting to SUBJECT_LIST_CTL");
                ServletUtility.redirect(ORSView.SUBJECT_LIST_CTL, request, response);
                return;
            }

            list = model.search(bean, pageNo, pageSize);
            next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.size() == 0) {
                ServletUtility.setErrorMessage("No record found ", request);
                log.warn("No records found in doPost()");
            } else {
                log.info("Fetched " + list.size() + " records for pageNo=" + pageNo + " in doPost()");
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
            return;
        }
    }

    /**
     * Returns the JSP view path for the subject list.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning Subject List view page");
        return ORSView.SUBJECT_LIST_VIEW;
    }
}
