package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.TimetableBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.model.CourseModel;
import in.co.rays.proj4.model.SubjectModel;
import in.co.rays.proj4.model.TimetableModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * TimetableListCtl handles listing, searching, pagination and bulk actions for
 * Timetable entities. It preloads subject and course lists for the view,
 * populates {@link TimetableBean} from request parameters, delegates search/delete
 * operations to {@link TimetableModel}, and prepares pagination metadata for the view.
 * <p>
 * Supported operations include Search, Next, Previous, New, Delete, Reset and Back.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.TimetableModel
 * @see in.co.rays.proj4.bean.TimetableBean
 */
@WebServlet(name = "TimetableListCtl", urlPatterns = { "/ctl/TimetableListCtl" })
public class TimetableListCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(TimetableListCtl.class);

    /**
     * Preloads subject and course lists into request attributes for rendering
     * dropdowns on the timetable list page.
     *
     * @param request the {@link HttpServletRequest}
     */
    @Override
    protected void preload(HttpServletRequest request) {
        log.debug("TimetableListCtl preload() called");

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
     * Populates a {@link TimetableBean} from request parameters for use in search
     * or other operations.
     *
     * @param request the {@link HttpServletRequest} containing parameters
     * @return populated {@link BaseBean} (actually a {@link TimetableBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {
        log.debug("TimetableListCtl populateBean() called");

        TimetableBean bean = new TimetableBean();

        bean.setCourseId(DataUtility.getLong(request.getParameter("courseId")));
        bean.setSubjectId(DataUtility.getLong(request.getParameter("subjectId")));
        bean.setExamDate(DataUtility.getDate(request.getParameter("examDate")));

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

        log.info("TimetableListCtl doGet() started");

        int pageNo = 1;
        int pageSize = DataUtility.getInt(PropertyReader.getValue("page.size"));

        TimetableBean bean = (TimetableBean) populateBean(request);
        TimetableModel model = new TimetableModel();

        try {
            List<TimetableBean> list = model.search(bean, pageNo, pageSize);
            List<TimetableBean> next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.isEmpty()) {
                ServletUtility.setErrorMessage("No record found", request);
                log.warn("No records found in doGet()");
            } else {
                log.info("Found " + list.size() + " records in doGet()");
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

        log.info("TimetableListCtl doPost() started");

        List list = null;
        List next = null;

        int pageNo = DataUtility.getInt(request.getParameter("pageNo"));
        int pageSize = DataUtility.getInt(request.getParameter("pageSize"));

        pageNo = (pageNo == 0) ? 1 : pageNo;
        pageSize = (pageSize == 0) ? DataUtility.getInt(PropertyReader.getValue("page.size")) : pageSize;

        TimetableBean bean = (TimetableBean) populateBean(request);
        TimetableModel model = new TimetableModel();

        String op = DataUtility.getString(request.getParameter("operation"));
        String[] ids = request.getParameterValues("ids");

        try {

            if (OP_SEARCH.equalsIgnoreCase(op) || "Next".equalsIgnoreCase(op) || "Previous".equalsIgnoreCase(op)) {

                if (OP_SEARCH.equalsIgnoreCase(op)) {
                    pageNo = 1;
                    log.debug("Operation: SEARCH");
                } else if (OP_NEXT.equalsIgnoreCase(op)) {
                    pageNo++;
                    log.debug("Operation: NEXT, pageNo=" + pageNo);
                } else if (OP_PREVIOUS.equalsIgnoreCase(op) && pageNo > 1) {
                    pageNo--;
                    log.debug("Operation: PREVIOUS, pageNo=" + pageNo);
                }

            } else if (OP_NEW.equalsIgnoreCase(op)) {
                log.info("Operation: NEW, redirecting to TIMETABLE_CTL");
                ServletUtility.redirect(ORSView.TIMETABLE_CTL, request, response);
                return;

            } else if (OP_DELETE.equalsIgnoreCase(op)) {
                pageNo = 1;
                if (ids != null && ids.length > 0) {
                    log.info("Operation: DELETE, number of ids=" + ids.length);
                    TimetableBean deletebean = new TimetableBean();
                    for (String id : ids) {
                        deletebean.setId(DataUtility.getInt(id));
                        model.delete(deletebean);
                        ServletUtility.setSuccessMessage("Data is deleted successfully", request);
                        log.info("Deleted Timetable with id=" + id);
                    }
                } else {
                    ServletUtility.setErrorMessage("Select at least one record", request);
                    log.warn("Delete operation failed: no ids selected");
                }

            } else if (OP_RESET.equalsIgnoreCase(op)) {
                log.info("Operation: RESET, redirecting to TIMETABLE_LIST_CTL");
                ServletUtility.redirect(ORSView.TIMETABLE_LIST_CTL, request, response);
                return;

            } else if (OP_BACK.equalsIgnoreCase(op)) {
                log.info("Operation: BACK, redirecting to TIMETABLE_LIST_CTL");
                ServletUtility.redirect(ORSView.TIMETABLE_LIST_CTL, request, response);
                return;
            }

            list = model.search(bean, pageNo, pageSize);
            next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.size() == 0) {
                ServletUtility.setErrorMessage("No record found ", request);
                log.warn("No records found in doPost()");
            } else {
                log.info("Found " + list.size() + " records in doPost()");
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
     * Returns the JSP view path for the timetable list.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning Timetable List view page");
        return ORSView.TIMETABLE_LIST_VIEW;
    }
}
