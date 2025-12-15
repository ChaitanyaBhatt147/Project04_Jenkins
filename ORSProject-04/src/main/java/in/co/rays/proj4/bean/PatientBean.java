package in.co.rays.proj4.bean;

import java.util.Date;

/**
 * <p>
 * PatientBean represents a Patient entity in the application.
 * It is a JavaBean used to transfer patient-related data between
 * different layers such as Controller, Model, and View.
 * </p>
 *
 * <p>
 * This bean extends {@link BaseBean} and is also used for dropdown
 * list rendering by implementing key-value methods.
 * </p>
 *
 * <p>
 * Attributes include:
 * </p>
 * <ul>
 *   <li>Patient Name</li>
 *   <li>Date of Visit</li>
 *   <li>Mobile Number</li>
 *   <li>Disease</li>
 * </ul>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 */
public class PatientBean extends BaseBean {

	/**
	 * Name of the patient
	 */
	private String name;

	/**
	 * Date when the patient visited
	 */
	private Date dateOfVisit;

	/**
	 * Mobile number of the patient
	 */
	private String mobile;

	/**
	 * Disease diagnosed for the patient
	 */
	private String disease;

	/**
	 * Returns patient name.
	 *
	 * @return patient name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets patient name.
	 *
	 * @param name patient name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns date of visit.
	 *
	 * @return date of visit
	 */
	public Date getDateOfVisit() {
		return dateOfVisit;
	}

	/**
	 * Sets date of visit.
	 *
	 * @param dateOfVisit visit date
	 */
	public void setDateOfVisit(Date dateOfVisit) {
		this.dateOfVisit = dateOfVisit;
	}

	/**
	 * Returns mobile number.
	 *
	 * @return mobile number
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * Sets mobile number.
	 *
	 * @param mobile mobile number
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * Returns disease name.
	 *
	 * @return disease
	 */
	public String getDisease() {
		return disease;
	}

	/**
	 * Sets disease name.
	 *
	 * @param disease disease name
	 */
	public void setDisease(String disease) {
		this.disease = disease;
	}

	/**
	 * Returns value used in dropdown lists.
	 *
	 * @return disease name
	 */
	@Override
	public String getValue() {
		return disease;
	}

	/**
	 * Returns string representation of PatientBean.
	 *
	 * @return string format of patient details
	 */
	@Override
	public String toString() {
		return "PatientBean [name=" + name + ", dateOfVisit=" + dateOfVisit + ", mobile=" + mobile + ", disease="
				+ disease + "]";
	}

	/**
	 * Returns key used in dropdown lists.
	 *
	 * @return disease name
	 */
	@Override
	public String getKey() {
		return disease;
	}
}
