package in.co.rays.proj4.controller;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.BaseBean;
import in.co.rays.proj4.bean.PatientBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.model.PatientModel;
import in.co.rays.proj4.util.DataUtility;
import in.co.rays.proj4.util.DataValidator;
import in.co.rays.proj4.util.PropertyReader;
import in.co.rays.proj4.util.ServletUtility;

@WebServlet(name = "PatientCtl", urlPatterns = { "/ctl/PatientCtl" })
public class PatientCtl extends BaseCtl {

	/** Log4j Logger */
	private static final Logger log = Logger.getLogger(PatientCtl.class);

	protected void preload(HttpServletRequest request) {
		log.debug("PatientCtl preload() started");

		HashMap<String, String> diseaseMap = new HashMap<String, String>();
		diseaseMap.put("Diabetes", "Diabetes");
		diseaseMap.put("Hypertension", "Hypertension");
		diseaseMap.put("Asthma", "Asthma");
		diseaseMap.put("Tuberculosis", "Tuberculosis");
		diseaseMap.put("Malaria", "Malaria");
		diseaseMap.put("Alzheimer's", "Alzheimer's");
		diseaseMap.put("Parkinson's", "Parkinson's");
		diseaseMap.put("Hepatitis", "Hepatitis");
		diseaseMap.put("Cholera", "Cholera");
		diseaseMap.put("Ebola", "Ebola");

		request.setAttribute("diseaseMap", diseaseMap);

		log.debug("PatientCtl preload() completed");
	}

	@Override
	protected boolean validate(HttpServletRequest request) {

		log.debug("PatientCtl validate() started");

		boolean pass = true;

		if (DataValidator.isNull(request.getParameter("name"))) {
			log.warn("Patient name is required");
			request.setAttribute("name", PropertyReader.getValue("error.require", "Name"));
			pass = false;
		} else if (!DataValidator.isName(request.getParameter("name"))) {
			log.warn("Invalid patient name");
			request.setAttribute("name", "Invalid Name");
			pass = false;
		}

		if (DataValidator.isNull(request.getParameter("dateOfVisit"))) {
			log.warn("Date of visit is required");
			request.setAttribute("dateOfVisit", PropertyReader.getValue("error.require", "Date of Birth"));
			pass = false;
		} else if (!DataValidator.isDate(request.getParameter("dateOfVisit"))) {
			log.warn("Invalid date of visit");
			request.setAttribute("dateOfVisit", PropertyReader.getValue("error.date", "Date of Visit"));
			pass = false;
		}

		if (DataValidator.isNull(request.getParameter("mobile"))) {
			log.warn("Mobile number is required");
			request.setAttribute("mobile", PropertyReader.getValue("error.require", "MobileNo"));
			pass = false;
		} else if (!DataValidator.isPhoneLength(request.getParameter("mobile"))) {
			log.warn("Invalid mobile number length");
			request.setAttribute("mobile", "Mobile No must have 10 digits");
			pass = false;
		} else if (!DataValidator.isPhoneNo(request.getParameter("mobile"))) {
			log.warn("Invalid mobile number");
			request.setAttribute("mobile", "Invalid Mobile No");
			pass = false;
		}

		if (DataValidator.isNull(request.getParameter("disease"))) {
			log.warn("Disease is required");
			request.setAttribute("disease", PropertyReader.getValue("error.require", "Disease"));
			pass = false;
		}

		log.debug("PatientCtl validate() completed with status: " + pass);
		return pass;
	}

	@Override
	protected BaseBean populateBean(HttpServletRequest request) {

		log.debug("PatientCtl populateBean() started");

		PatientBean bean = new PatientBean();

		bean.setId(DataUtility.getLong(request.getParameter("id")));
		bean.setName(DataUtility.getString(request.getParameter("name")));
		bean.setDateOfVisit(DataUtility.getDate(request.getParameter("dateOfVisit")));
		bean.setMobile(DataUtility.getString(request.getParameter("mobile")));
		bean.setDisease(DataUtility.getString(request.getParameter("disease")));

		populateDTO(bean, request);

		log.debug("PatientCtl populateBean() completed");
		return bean;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		log.info("PatientCtl doGet() started");

		long id = DataUtility.getLong(req.getParameter("id"));
		PatientModel model = new PatientModel();

		if (id > 0) {
			try {
				log.debug("Fetching patient record for id: " + id);
				PatientBean bean = model.findByPk(id);
				ServletUtility.setBean(bean, req);
			} catch (ApplicationException e) {
				log.error("Exception in PatientCtl doGet()", e);
				e.printStackTrace();
				ServletUtility.handleException(e, req, resp);
				return;
			}
		}

		ServletUtility.forward(getView(), req, resp);
		log.info("PatientCtl doGet() ended");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		log.info("PatientCtl doPost() started");

		String op = DataUtility.getString(req.getParameter("operation"));
		long id = DataUtility.getLong(req.getParameter("id"));
		PatientModel model = new PatientModel();

		log.debug("Operation: " + op);
		System.out.println("in patientctl dopost:" + op);

		if (OP_SAVE.equalsIgnoreCase(op)) {

			PatientBean bean = (PatientBean) populateBean(req);
			System.out.println("bean : " + bean);

			try {
				log.info("Adding new patient");
				model.add(bean);
				ServletUtility.setBean(bean, req);
				ServletUtility.setSuccessMessage("Patient added successfully", req);
			} catch (DuplicateRecordException e) {
				log.warn("Duplicate patient record");
				ServletUtility.setBean(bean, req);
				ServletUtility.setErrorMessage("Patient already exists", req);
			} catch (ApplicationException e) {
				log.error("ApplicationException while adding patient", e);
				e.printStackTrace();
				ServletUtility.handleException(e, req, resp);
				return;
			}

		} else if (OP_RESET.equalsIgnoreCase(op)) {

			log.info("Reset operation triggered");
			ServletUtility.redirect(ORSView.PATIENT_CTL, req, resp);
			return;

		} else if (OP_UPDATE.equalsIgnoreCase(op)) {

			PatientBean bean = (PatientBean) populateBean(req);

			try {
				if (id > 0) {
					log.info("Updating patient id: " + id);
					model.update(bean);
				}
				ServletUtility.setBean(bean, req);
				ServletUtility.setSuccessMessage("Patient updated successfully", req);
			} catch (DuplicateRecordException e) {
				log.warn("Duplicate patient record on update");
				ServletUtility.setBean(bean, req);
				ServletUtility.setErrorMessage("Patient already exists", req);
			} catch (ApplicationException e) {
				log.error("ApplicationException while updating patient", e);
				e.printStackTrace();
				ServletUtility.handleException(e, req, resp);
				return;
			}

		} else if (OP_CANCEL.equalsIgnoreCase(op)) {

			log.info("Cancel operation triggered");
			ServletUtility.redirect(ORSView.PATIENT_LIST_CTL, req, resp);
			return;
		}

		ServletUtility.forward(getView(), req, resp);
		log.info("PatientCtl doPost() ended");
	}

	@Override
	protected String getView() {
		log.debug("Returning Patient View");
		return ORSView.PATIENT_VIEW;
	}
}
