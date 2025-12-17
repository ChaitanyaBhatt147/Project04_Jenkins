package in.co.rays.proj4.bean;

import java.util.Date;

public class PrescriptionBean extends BaseBean{

	private String name;

	private Date date;

	private Integer capacity;

	private String disease;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDateOfVisit() {
		return date;
	}

	public void setDateOfVisit(Date dateOfVisit) {
		this.date = dateOfVisit;
	}

	public Integer getMobile() {
		return capacity;
	}

	public void setMobile(Integer capacity) {
		this.capacity = capacity;
	}

	public String getDisease() {
		return disease;
	}

	public void setDisease(String disease) {
		this.disease = disease;
	}

	@Override
	public String getValue() {
		return disease;
	}

	@Override
	public String toString() {
		return "PatientBean [name=" + name + ", date=" + date + ", capacity=" + capacity + ", disease="
				+ disease + "]";
	}

	@Override
	public String getKey() {
		return disease;
	}


}
