package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.CollegeBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.model.CollegeModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * Controller that handles listing, searching, pagination and bulk actions for
 * College entities. It preloads data required by the view, populates {@link CollegeBean}
 * from request parameters, and delegates business operations to {@link CollegeModel}.
 * <p>
 * Supported operations include Search, Next, Previous, New, Delete, Reset and Back.
 * </p>
 * 
 * author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.CollegeModel
 * @see in.co.rays.proj4.bean.CollegeBean
 */
@WebServlet(name = "CollegeListCtl", urlPatterns = { "/ctl/CollegeListCtl" })
public class CollegeListCtl extends BaseCtl {

    private static final Logger log = Logger.getLogger(CollegeListCtl.class);

    /**
     * Preloads the list of colleges and sets it as request attribute "collegeList".
     * This method is called before forwarding to the view so that dropdowns or
     * auxiliary lists can be rendered.
     *
     * @param request the {@link HttpServletRequest}
     */
    @Override
    protected void preload(HttpServletRequest request) {

        log.debug("CollegeListCtl preload() started");

        CollegeModel collegeModel = new CollegeModel();

        try {
            List collegeList = collegeModel.list();
            request.setAttribute("collegeList", collegeList);
            log.debug("College list preloaded, size: " + collegeList.size());
        } catch (ApplicationException e) {
            log.error("Error while preloading college list", e);
            e.printStackTrace();
        }
    }

    /**
     * Populates a {@link CollegeBean} from request parameters for use in search
     * or other operations.
     *
     * @param request the {@link HttpServletRequest} containing parameters
     * @return populated {@link BaseBean} (actually a {@link CollegeBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("CollegeListCtl populateBean() called");

        CollegeBean bean = new CollegeBean();

        bean.setName(DataUtility.getString(request.getParameter("name")));
        bean.setCity(DataUtility.getString(request.getParameter("city")));
        bean.setId(DataUtility.getLong(request.getParameter("collegeId")));

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
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("CollegeListCtl doGet() started");

        int pageNo = 1;
        int pageSize = DataUtility.getInt(PropertyReader.getValue("page.size"));

        CollegeBean bean = (CollegeBean) populateBean(request);
        CollegeModel model = new CollegeModel();

        try {
            List<CollegeBean> list = model.search(bean, pageNo, pageSize);
            List<CollegeBean> next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.isEmpty()) {
                log.warn("No college records found");
                ServletUtility.setErrorMessage("No record found", request);
            }

            ServletUtility.setList(list, request);
            ServletUtility.setPageNo(pageNo, request);
            ServletUtility.setPageSize(pageSize, request);
            ServletUtility.setBean(bean, request);
            request.setAttribute("nextListSize", next.size());

            log.debug("Forwarding to college list view");
            ServletUtility.forward(getView(), request, response);

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

        log.info("CollegeListCtl doPost() started");

        List list = null;
        List next = null;

        int pageNo = DataUtility.getInt(request.getParameter("pageNo"));
        int pageSize = DataUtility.getInt(request.getParameter("pageSize"));

        pageNo = (pageNo == 0) ? 1 : pageNo;
        pageSize = (pageSize == 0) ? DataUtility.getInt(PropertyReader.getValue("page.size")) : pageSize;

        CollegeBean bean = (CollegeBean) populateBean(request);
        CollegeModel model = new CollegeModel();

        String op = DataUtility.getString(request.getParameter("operation"));
        String[] ids = request.getParameterValues("ids");

        log.debug("Operation received: " + op + ", PageNo: " + pageNo);

        try {

            if (OP_SEARCH.equalsIgnoreCase(op) || "Next".equalsIgnoreCase(op) || "Previous".equalsIgnoreCase(op)) {

                if (OP_SEARCH.equalsIgnoreCase(op)) {
                    pageNo = 1;
                    log.debug("Search operation triggered");
                } else if (OP_NEXT.equalsIgnoreCase(op)) {
                    pageNo++;
                    log.debug("Next page requested");
                } else if (OP_PREVIOUS.equalsIgnoreCase(op) && pageNo > 1) {
                    pageNo--;
                    log.debug("Previous page requested");
                }

            } else if (OP_NEW.equalsIgnoreCase(op)) {
                log.info("Redirecting to College form");
                ServletUtility.redirect(ORSView.COLLEGE_CTL, request, response);
                return;

            } else if (OP_DELETE.equalsIgnoreCase(op)) {
                pageNo = 1;
                if (ids != null && ids.length > 0) {
                    CollegeBean deletebean = new CollegeBean();
                    for (String id : ids) {
                        deletebean.setId(DataUtility.getInt(id));
                        model.delete(deletebean);
                        log.info("Deleted college id: " + id);
                        ServletUtility.setSuccessMessage("Data is deleted successfully", request);
                    }
                } else {
                    log.warn("Delete operation requested without selecting records");
                    ServletUtility.setErrorMessage("Select at least one record", request);
                }

            } else if (OP_RESET.equalsIgnoreCase(op)) {
                log.info("Reset operation triggered");
                ServletUtility.redirect(ORSView.COLLEGE_LIST_CTL, request, response);
                return;

            } else if (OP_BACK.equalsIgnoreCase(op)) {
                log.info("Back operation triggered");
                ServletUtility.redirect(ORSView.COLLEGE_LIST_CTL, request, response);
                return;
            }

            list = model.search(bean, pageNo, pageSize);
            next = model.search(bean, pageNo + 1, pageSize);

            if (list == null || list.size() == 0) {
                log.warn("No college records found after operation");
                ServletUtility.setErrorMessage("No record found ", request);
            }

            ServletUtility.setList(list, request);
            ServletUtility.setPageNo(pageNo, request);
            ServletUtility.setPageSize(pageSize, request);
            ServletUtility.setBean(bean, request);
            request.setAttribute("nextListSize", next.size());

            log.debug("Forwarding to college list view");
            ServletUtility.forward(getView(), request, response);

        } catch (ApplicationException e) {
            log.error("ApplicationException in doPost()", e);
            e.printStackTrace();
            ServletUtility.handleException(e, request, response);
            return;
        }
    }

    /**
     * Returns the JSP view path for the college list.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        return ORSView.COLLEGE_LIST_VIEW;
    }
}
