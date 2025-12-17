package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.MarksheetBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.model.MarksheetModel;
import in.co.rays.proj4.model.StudentModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * MarksheetCtl is a controller servlet that handles add, update and view
 * operations for marksheets. It validates form inputs (roll number and marks),
 * preloads student list for dropdowns, populates {@link MarksheetBean} from the
 * request and delegates persistence operations to {@link MarksheetModel}.
 * <p>
 * Supported operations include Save, Update, Cancel and Reset.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.MarksheetModel
 * @see in.co.rays.proj4.bean.MarksheetBean
 */
@WebServlet(name = "MarksheetCtl", urlPatterns = { "/ctl/MarksheetCtl" })
public class MarksheetCtl extends BaseCtl {

    private static final Logger log = Logger.getLogger(MarksheetCtl.class);

    /**
     * Preloads the list of students and sets it as request attribute
     * "studentList" for dropdown rendering.
     *
     * @param request the {@link HttpServletRequest}
     */
    @Override
    protected void preload(HttpServletRequest request) {

        log.debug("MarksheetCtl preload() started");

        StudentModel studentModel = new StudentModel();
        try {
            List studentList = studentModel.list();
            request.setAttribute("studentList", studentList);
            log.debug("Student list loaded successfully");
        } catch (ApplicationException e) {
            log.error("Error while loading student list", e);
            e.printStackTrace();
        }
    }

    /**
     * Validates the marksheet form parameters.
     * <ul>
     *   <li>studentId is required.</li>
     *   <li>rollNo is required and must be a valid roll format.</li>
     *   <li>physics, chemistry and maths are required, must be integers and in 0-100 range.</li>
     * </ul>
     *
     * @param request the {@link HttpServletRequest} containing form parameters
     * @return {@code true} when validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {

        log.debug("MarksheetCtl validate() started");

        boolean pass = true;

        if (DataValidator.isNull(request.getParameter("studentId"))) {
            log.warn("Student ID is null");
            request.setAttribute("studentId", PropertyReader.getValue("error.require", "Student Name"));
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("rollNo"))) {
            log.warn("Roll number is null");
            request.setAttribute("rollNo", PropertyReader.getValue("error.require", "Roll Number"));
            pass = false;
        } else if (!DataValidator.isRollNo(request.getParameter("rollNo"))) {
            log.warn("Invalid roll number format");
            request.setAttribute("rollNo", "Roll No is invalid");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("physics"))) {
            log.warn("Physics marks are null");
            request.setAttribute("physics", PropertyReader.getValue("error.require", "Marks"));
            pass = false;
        } else if (DataValidator.isNotNull(request.getParameter("physics"))
                && !DataValidator.isInteger(request.getParameter("physics"))) {
            log.warn("Physics marks not integer");
            request.setAttribute("physics", PropertyReader.getValue("error.integer", "Marks"));
            pass = false;
        } else if (DataUtility.getInt(request.getParameter("physics")) > 100
                || DataUtility.getInt(request.getParameter("physics")) < 0) {
            log.warn("Physics marks out of range");
            request.setAttribute("physics", "Marks should be in 0 to 100");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("chemistry"))) {
            log.warn("Chemistry marks are null");
            request.setAttribute("chemistry", PropertyReader.getValue("error.require", "Marks"));
            pass = false;
        } else if (DataValidator.isNotNull(request.getParameter("chemistry"))
                && !DataValidator.isInteger(request.getParameter("chemistry"))) {
            log.warn("Chemistry marks not integer");
            request.setAttribute("chemistry", PropertyReader.getValue("error.integer", "Marks"));
            pass = false;
        } else if (DataUtility.getInt(request.getParameter("chemistry")) > 100
                || DataUtility.getInt(request.getParameter("chemistry")) < 0) {
            log.warn("Chemistry marks out of range");
            request.setAttribute("chemistry", "Marks should be in 0 to 100");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("maths"))) {
            log.warn("Maths marks are null");
            request.setAttribute("maths", PropertyReader.getValue("error.require", "Marks"));
            pass = false;
        } else if (DataValidator.isNotNull(request.getParameter("maths"))
                && !DataValidator.isInteger(request.getParameter("maths"))) {
            log.warn("Maths marks not integer");
            request.setAttribute("maths", PropertyReader.getValue("error.integer", "Marks"));
            pass = false;
        } else if (DataUtility.getInt(request.getParameter("maths")) > 100
                || DataUtility.getInt(request.getParameter("maths")) < 0) {
            log.warn("Maths marks out of range");
            request.setAttribute("maths", "Marks should be in 0 to 100");
            pass = false;
        }

        log.debug("MarksheetCtl validate() completed with status: " + pass);
        return pass;
    }

    /**
     * Populates a {@link MarksheetBean} from request parameters including optional
     * conversion of marks to Integer and sets audit fields by calling
     * {@link #populateDTO(BaseBean, HttpServletRequest)}.
     *
     * @param request the {@link HttpServletRequest} containing form data
     * @return populated {@link BaseBean} (actually a {@link MarksheetBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {

        log.debug("MarksheetCtl populateBean() started");

        MarksheetBean bean = new MarksheetBean();

        bean.setId(DataUtility.getLong(request.getParameter("id")));
        bean.setRollNo(DataUtility.getString(request.getParameter("rollNo")));
        bean.setName(DataUtility.getString(request.getParameter("name")));

        if (request.getParameter("physics") != null && request.getParameter("physics").length() != 0) {
            bean.setPhysics(DataUtility.getInt(request.getParameter("physics")));
        }
        if (request.getParameter("chemistry") != null && request.getParameter("chemistry").length() != 0) {
            bean.setChemistry(DataUtility.getInt(request.getParameter("chemistry")));
        }
        if (request.getParameter("maths") != null && request.getParameter("maths").length() != 0) {
            bean.setMaths(DataUtility.getInt(request.getParameter("maths")));
        }

        bean.setStudentId(DataUtility.getLong(request.getParameter("studentId")));

        populateDTO(bean, request);

        log.debug("MarksheetCtl populateBean() completed");
        return bean;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("MarksheetCtl doGet() started");

        long id = DataUtility.getLong(request.getParameter("id"));
        MarksheetModel model = new MarksheetModel();

        if (id > 0) {
            try {
                MarksheetBean bean = model.findByPk(id);
                ServletUtility.setBean(bean, request);
                log.info("Marksheet loaded for id: " + id);
            } catch (ApplicationException e) {
                log.error("Error while loading marksheet", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        }

        ServletUtility.forward(getView(), request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.info("MarksheetCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));
        MarksheetModel model = new MarksheetModel();
        long id = DataUtility.getLong(request.getParameter("id"));

        if (OP_SAVE.equalsIgnoreCase(op)) {
            MarksheetBean bean = (MarksheetBean) populateBean(request);
            try {
                long pk = model.add(bean);
                log.info("Marksheet added with id: " + pk);
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Marksheet added successfully", request);
            } catch (DuplicateRecordException e) {
                log.warn("Duplicate roll number detected", e);
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Roll No already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException while saving marksheet", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }

        } else if (OP_UPDATE.equalsIgnoreCase(op)) {
            MarksheetBean bean = (MarksheetBean) populateBean(request);
            try {
                if (id > 0) {
                    model.update(bean);
                    log.info("Marksheet updated for id: " + id);
                }
                ServletUtility.setBean(bean, request);
                ServletUtility.setSuccessMessage("Marksheet updated successfully", request);
            } catch (DuplicateRecordException e) {
                log.warn("Duplicate roll number on update", e);
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Roll No already exists", request);
            } catch (ApplicationException e) {
                log.error("ApplicationException while updating marksheet", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }

        } else if (OP_CANCEL.equalsIgnoreCase(op)) {
            log.info("Cancel operation triggered");
            ServletUtility.redirect(ORSView.MARKSHEET_LIST_CTL, request, response);
            return;

        } else if (OP_RESET.equalsIgnoreCase(op)) {
            log.info("Reset operation triggered");
            ServletUtility.redirect(ORSView.MARKSHEET_CTL, request, response);
            return;
        }

        ServletUtility.forward(getView(), request, response);
    }

    /**
     * Returns the JSP view path for marksheet form.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        return ORSView.MARKSHEET_VIEW;
    }
}
