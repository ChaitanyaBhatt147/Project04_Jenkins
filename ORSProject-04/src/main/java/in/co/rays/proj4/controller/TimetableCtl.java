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
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.model.CourseModel;
import in.co.rays.proj4.model.SubjectModel;
import in.co.rays.proj4.model.TimetableModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

/**
 * TimetableCtl is a controller servlet that manages timetable CRUD operations.
 * It preloads course and subject lists for the form, validates timetable input
 * (including date checks), populates {@link TimetableBean} from request
 * parameters and delegates persistence and uniqueness checks to
 * {@link TimetableModel}.
 * <p>
 * Save and Update operations include checks to prevent duplicate timetable
 * entries (same course/date/subject/semester/time as applicable).
 * </p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 * @see in.co.rays.proj4.model.TimetableModel
 * @see in.co.rays.proj4.bean.TimetableBean
 */
@WebServlet(name = "TimetableCtl", urlPatterns = { "/ctl/TimetableCtl" })
public class TimetableCtl extends BaseCtl {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(TimetableCtl.class);

    /**
     * Preloads subject and course lists into request attributes for rendering
     * dropdowns on the timetable form.
     *
     * @param request current {@link HttpServletRequest}
     */
    @Override
    protected void preload(HttpServletRequest request) {
        log.debug("TimetableCtl preload() called");

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
     * Validates timetable form parameters.
     * <ul>
     *   <li>semester is required.</li>
     *   <li>examDate is required, must be a valid date and not Sunday.</li>
     *   <li>examTime is required.</li>
     *   <li>description is required.</li>
     *   <li>courseId and subjectId are required.</li>
     * </ul>
     *
     * @param request current {@link HttpServletRequest}
     * @return {@code true} if validation passes; {@code false} otherwise
     */
    @Override
    protected boolean validate(HttpServletRequest request) {
        log.debug("TimetableCtl validate() called");

        boolean pass = true;

        if (DataValidator.isNull(request.getParameter("semester"))) {
            request.setAttribute("semester", PropertyReader.getValue("error.require", "Semester"));
            log.warn("Validation failed: Semester is required");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("examDate"))) {
            request.setAttribute("examDate", PropertyReader.getValue("error.require", "Date of Exam"));
            log.warn("Validation failed: Exam date is required");
            pass = false;
        } else if (!DataValidator.isDate(request.getParameter("examDate"))) {
            request.setAttribute("examDate", PropertyReader.getValue("error.date", "Date of Exam"));
            log.warn("Validation failed: Exam date is invalid");
            pass = false;
        } else if (DataValidator.isSunday(request.getParameter("examDate"))) {
            request.setAttribute("examDate", "Exam should not be on Sunday");
            log.warn("Validation failed: Exam date is Sunday");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("examTime"))) {
            request.setAttribute("examTime", PropertyReader.getValue("error.require", "Exam Time"));
            log.warn("Validation failed: Exam time is required");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("description"))) {
            request.setAttribute("description", PropertyReader.getValue("error.require", "Description"));
            log.warn("Validation failed: Description is required");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("courseId"))) {
            request.setAttribute("courseId", PropertyReader.getValue("error.require", "Course Name"));
            log.warn("Validation failed: CourseId is required");
            pass = false;
        }

        if (DataValidator.isNull(request.getParameter("subjectId"))) {
            request.setAttribute("subjectId", PropertyReader.getValue("error.require", "Subject Name"));
            log.warn("Validation failed: SubjectId is required");
            pass = false;
        }

        return pass;
    }

    /**
     * Populates a {@link TimetableBean} from request parameters and sets audit
     * fields via {@link #populateDTO(BaseBean, HttpServletRequest)}.
     *
     * @param request current {@link HttpServletRequest}
     * @return populated {@link BaseBean} (actually a {@link TimetableBean})
     */
    @Override
    protected BaseBean populateBean(HttpServletRequest request) {
        log.debug("TimetableCtl populateBean() called");

        TimetableBean bean = new TimetableBean();

        bean.setId(DataUtility.getLong(request.getParameter("id")));
        bean.setSemester(DataUtility.getString(request.getParameter("semester")));
        bean.setDescription(DataUtility.getString(request.getParameter("description")));
        bean.setExamTime(DataUtility.getString(request.getParameter("examTime")));
        bean.setExamDate(DataUtility.getDate(request.getParameter("examDate")));
        bean.setCourseId(DataUtility.getLong(request.getParameter("courseId")));
        bean.setSubjectId(DataUtility.getLong(request.getParameter("subjectId")));

        populateDTO(bean, request);

        return bean;
    }

    /**
     * Handles HTTP GET requests. If an 'id' parameter is present (>0), loads
     * the corresponding timetable for editing/view and forwards to view.
     *
     * @param request  current {@link HttpServletRequest}
     * @param response current {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.info("TimetableCtl doGet() started");

        long id = DataUtility.getLong(request.getParameter("id"));

        TimetableModel model = new TimetableModel();

        if (id > 0) {
            try {
                TimetableBean bean = model.findByPk(id);
                ServletUtility.setBean(bean, request);
                log.info("Loaded Timetable for id=" + id);
            } catch (ApplicationException e) {
                log.error("ApplicationException in doGet()", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        }
        ServletUtility.forward(getView(), request, response);
        log.info("doGet() forwarded to view: " + getView());
    }

    /**
     * Handles HTTP POST requests for Save, Update, Cancel and Reset operations.
     * <ul>
     *   <li>Save — checks for duplicate timetable entries (by course/date/subject/semester)
     *       and adds if unique.</li>
     *   <li>Update — checks for duplicate by exam time (and other identifying fields)
     *       and updates if unique.</li>
     *   <li>Cancel / Reset — redirect to list or form respectively.</li>
     * </ul>
     *
     * @param request  current {@link HttpServletRequest}
     * @param response current {@link HttpServletResponse}
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.info("TimetableCtl doPost() started");

        String op = DataUtility.getString(request.getParameter("operation"));

        TimetableModel model = new TimetableModel();

        long id = DataUtility.getLong(request.getParameter("id"));

        if (OP_SAVE.equalsIgnoreCase(op)) {
            log.debug("Operation: SAVE");

            TimetableBean bean = (TimetableBean) populateBean(request);

            TimetableBean bean1;
            TimetableBean bean2;
            TimetableBean bean3;

            try {
                bean1 = model.checkByCourseName(bean.getCourseId(), bean.getExamDate());
                bean2 = model.checkBySubjectName(bean.getCourseId(), bean.getSubjectId(), bean.getExamDate());
                bean3 = model.checkBySemester(bean.getCourseId(), bean.getSubjectId(), bean.getSemester(),
                        bean.getExamDate());

                if (bean1 == null && bean2 == null && bean3 == null) {
                    long pk = model.add(bean);
                    ServletUtility.setBean(bean, request);
                    ServletUtility.setSuccessMessage("Timetable added successfully", request);
                    log.info("Timetable added successfully with id=" + pk);
                } else {
                    ServletUtility.setBean(bean, request);
                    ServletUtility.setErrorMessage("Timetable already exist!", request);
                    log.warn("Duplicate Timetable entry found during SAVE operation");
                }
            } catch (DuplicateRecordException e) {
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Timetable already exist!", request);
                log.warn("DuplicateRecordException during SAVE", e);
            } catch (ApplicationException e) {
                log.error("ApplicationException during SAVE", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }

        } else if (OP_UPDATE.equalsIgnoreCase(op)) {
            log.debug("Operation: UPDATE");

            TimetableBean bean = (TimetableBean) populateBean(request);

            TimetableBean bean4;

            try {
                bean4 = model.checkByExamTime(bean.getCourseId(), bean.getSubjectId(), bean.getSemester(),
                        bean.getExamDate(), bean.getExamTime(), bean.getDescription());

                if (id > 0 && bean4 == null) {
                    model.update(bean);
                    ServletUtility.setBean(bean, request);
                    ServletUtility.setSuccessMessage("Timetable updated successfully", request);
                    log.info("Timetable updated successfully for id=" + id);
                } else {
                    ServletUtility.setBean(bean, request);
                    ServletUtility.setErrorMessage("Timetable already exist!", request);
                    log.warn("Duplicate Timetable entry found during UPDATE operation");
                }
            } catch (DuplicateRecordException e) {
                ServletUtility.setBean(bean, request);
                ServletUtility.setErrorMessage("Timetable already exist!", request);
                log.warn("DuplicateRecordException during UPDATE", e);
            } catch (ApplicationException e) {
                log.error("ApplicationException during UPDATE", e);
                e.printStackTrace();
                ServletUtility.handleException(e, request, response);
                return;
            }
        } else if (OP_CANCEL.equalsIgnoreCase(op)) {
            log.info("Operation: CANCEL, redirecting to TIMETABLE_LIST_CTL");
            ServletUtility.redirect(ORSView.TIMETABLE_LIST_CTL, request, response);
            return;
        } else if (OP_RESET.equalsIgnoreCase(op)) {
            log.info("Operation: RESET, redirecting to TIMETABLE_CTL");
            ServletUtility.redirect(ORSView.TIMETABLE_CTL, request, response);
            return;
        }
        ServletUtility.forward(getView(), request, response);
        log.info("doPost() forwarded to view: " + getView());
    }

    /**
     * Returns the JSP view path for the timetable form.
     *
     * @return view page path as {@link String}
     */
    @Override
    protected String getView() {
        log.debug("Returning Timetable view page");
        return ORSView.TIMETABLE_VIEW;
    }
}
