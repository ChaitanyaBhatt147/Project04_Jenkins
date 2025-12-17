package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.PatientBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.model.PatientModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

@WebServlet(name = "PatientListCtl", urlPatterns = { "/ctl/PatientListCtl" })
public class PatientListCtl extends BaseCtl {

	/** Log4j Logger */
	private static final Logger log = Logger.getLogger(PatientListCtl.class);

	@Override
	protected void preload(HttpServletRequest request) {
		log.debug("PatientListCtl preload() started");

		PatientModel model = new PatientModel();
		try {
			Iterator it = model.list().iterator();
			HashMap<String, String> diseaseMap = new HashMap<>();
			while (it.hasNext()) {
				PatientBean bean = (PatientBean) it.next();
				diseaseMap.put(bean.getDisease(), bean.getDisease());
			}
			request.setAttribute("diseaseMap", diseaseMap);
			log.debug("Disease map loaded successfully");
		} catch (ApplicationException e) {
			log.error("Error in PatientListCtl preload()", e);
			e.printStackTrace();
		}

		log.debug("PatientListCtl preload() completed");
	}

	@Override
	protected BaseBean populateBean(HttpServletRequest request) {
		log.debug("PatientListCtl populateBean() started");

		PatientBean bean = new PatientBean();
		bean.setId(DataUtility.getLong(request.getParameter("id")));
		bean.setName(DataUtility.getString(request.getParameter("name")));
		bean.setDateOfVisit(DataUtility.getDate(request.getParameter("dateOfVisit")));
		bean.setMobile(DataUtility.getString(request.getParameter("mobile")));
		bean.setDisease(DataUtility.getString(request.getParameter("disease")));

		log.debug("PatientListCtl populateBean() completed");
		return bean;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		log.info("PatientListCtl doGet() started");

		int pageNo = 1;
		int pageSize = DataUtility.getInt(PropertyReader.getValue("page.size"));

		PatientBean bean = (PatientBean) populateBean(req);
		PatientModel model = new PatientModel();

		try {
			log.debug("Searching patients with pagination");
			List<PatientBean> list = model.search(bean, pageNo, pageSize);
			List<PatientBean> next = model.search(bean, pageNo + 1, pageSize);

			if (list == null || list.isEmpty()) {
				log.warn("No patient records found");
				ServletUtility.setErrorMessage("No record found", req);
			}

			ServletUtility.setList(list, req);
			ServletUtility.setPageNo(pageNo, req);
			ServletUtility.setPageSize(pageSize, req);
			ServletUtility.setBean(bean, req);
			req.setAttribute("nextListSize", next.size());

			ServletUtility.forward(getView(), req, resp);

		} catch (ApplicationException e) {
			log.error("Exception in PatientListCtl doGet()", e);
			e.printStackTrace();
			ServletUtility.handleException(e, req, resp);
		}

		log.info("PatientListCtl doGet() ended");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		log.info("PatientListCtl doPost() started");

		int pageNo = DataUtility.getInt(req.getParameter("pageNo"));
		int pageSize = DataUtility.getInt(PropertyReader.getValue("pageSize"));

		pageNo = (pageNo == 0) ? 1 : pageNo;
		pageSize = (pageSize == 0)
				? DataUtility.getInt(PropertyReader.getValue("page.size"))
				: pageSize;

		PatientBean bean = (PatientBean) populateBean(req);
		PatientModel model = new PatientModel();

		String op = DataUtility.getString(req.getParameter("operation"));
		String[] ids = req.getParameterValues("ids");

		log.debug("Operation: " + op);

		try {
			if (OP_SEARCH.equalsIgnoreCase(op) || OP_NEXT.equalsIgnoreCase(op)
					|| OP_PREVIOUS.equalsIgnoreCase(op)) {

				if (OP_SEARCH.equalsIgnoreCase(op)) {
					pageNo = 1;
				} else if (OP_NEXT.equalsIgnoreCase(op)) {
					pageNo++;
				} else if (OP_PREVIOUS.equalsIgnoreCase(op)) {
					pageNo--;
				}

			} else if (OP_NEW.equalsIgnoreCase(op)) {
				log.info("Redirecting to PatientCtl");
				ServletUtility.redirect(ORSView.PATIENT_CTL, req, resp);
				return;

			} else if (OP_DELETE.equalsIgnoreCase(op)) {
				pageNo = 1;
				if (ids != null && ids.length > 0) {
					log.info("Deleting selected patients");
					PatientBean deleteBean = new PatientBean();
					for (String id : ids) {
						deleteBean.setId(DataUtility.getInt(id));
						model.delete(deleteBean.getId());
						ServletUtility.setSuccessMessage("Patient deleted successfully", req);
					}
				} else {
					log.warn("Delete operation without selecting IDs");
					ServletUtility.setErrorMessage("Select at least 1 id.", req);
				}

			} else if (OP_RESET.equalsIgnoreCase(op) || OP_BACK.equalsIgnoreCase(op)) {
				log.info("Reset/Back operation");
				ServletUtility.redirect(ORSView.PATIENT_LIST_CTL, req, resp);
				return;
			}

			List list = model.search(bean, pageNo, pageSize);
			List next = model.search(bean, pageNo + 1, pageSize);

			if (list == null || list.size() == 0) {
				log.warn("No records found after operation");
				ServletUtility.setErrorMessage("No record found", req);
			}

			ServletUtility.setBean(bean, req);
			ServletUtility.setList(list, req);
			ServletUtility.setPageNo(pageNo, req);
			ServletUtility.setPageSize(pageSize, req);
			req.setAttribute("nextListSize", next.size());

		} catch (ApplicationException e) {
			log.error("Exception in PatientListCtl doPost()", e);
			e.printStackTrace();
			ServletUtility.handleException(e, req, resp);
			return;
		}

		ServletUtility.forward(getView(), req, resp);
		log.info("PatientListCtl doPost() ended");
	}

	@Override
	protected String getView() {
		log.debug("Returning Patient List View");
		return ORSView.PATIENT_LIST_VIEW;
	}
}
