package in.co.rays.proj4.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import in.co.rays.proj4.util.ServletUtility;

@WebServlet("/ErrorCtl")
public class ErrorCtl extends BaseCtl {

	private static final Logger log = Logger.getLogger(ErrorCtl.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		log.info("ErrorCtl doGet() called");
		ServletUtility.forward(getView(), request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		log.info("ErrorCtl doPost() called");
		ServletUtility.forward(getView(), request, response);
	}

	@Override
	protected String getView() {
		log.debug("ErrorCtl getView() returning ERROR_VIEW");
		// TODO Auto-generated method stub
		return ORSView.ERROR_VIEW;
	}
}
