
<%@page import="java.util.HashMap"%>
<%@page import="in.co.rays.proj4.model.PatientModel"%>
<%@page import="in.co.rays.proj4.bean.PatientBean"%>
<%@page import="in.co.rays.proj4.controller.PatientListCtl"%>
<%@page import="in.co.rays.proj4.bean.RoleBean"%>
<%@page import="in.co.rays.proj4.controller.ORSView"%>
<%@page import="in.co.rays.proj4.util.HTMLUtility"%>
<%@page import="in.co.rays.proj4.model.RoleModel"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="in.co.rays.proj4.util.DataUtility"%>
<%@page import="in.co.rays.proj4.bean.BaseBean"%>
<%@page import="in.co.rays.proj4.controller.UserListCtl"%>
<%@page import="in.co.rays.proj4.util.ServletUtility"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>

<html>
<head>
<title>Patient List</title>
<link rel="icon" type="image/png"
	href="<%=ORSView.APP_CONTEXT%>/img/logo.png" sizes="16x16" />
</head>
<body>
	<%@include file="Header.jsp"%>

	<jsp:useBean id="bean" class="in.co.rays.proj4.bean.PatientBean"
		scope="request"></jsp:useBean>

	<div align="center">
		<h1 align="center" style="margin-bottom: -15; color: navy;">Patient
			List</h1>

		<div style="height: 15px; margin-bottom: 12px">
			<h3>
				<font color="red"><%=ServletUtility.getErrorMessage(request)%></font>
			</h3>
			<h3>
				<font color="green"><%=ServletUtility.getSuccessMessage(request)%></font>
			</h3>
		</div>

		<form action="<%=ORSView.PATIENT_LIST_CTL%>" method="post">
			<%
				int pageNo = ServletUtility.getPageNo(request);
				int pageSize = ServletUtility.getPageSize(request);
				int index = ((pageNo - 1) * pageSize) + 1;
				int nextListSize = DataUtility.getInt(request.getAttribute("nextListSize").toString());

				HashMap<String,String> diseaseMap = (HashMap<String,String>) request.getAttribute("diseaseMap");
				List<PatientBean> list = (List<PatientBean>) ServletUtility.getList(request);
				Iterator<PatientBean> it = list.iterator();

				if (list.size() != 0) {
			%>

			<input type="hidden" name="pageNo" value="<%=pageNo%>"> <input
				type="hidden" name="pageSize" value="<%=pageSize%>">

			<table style="width: 100%">
				<tr>
					<td align="center"><label><b>Name :</b></label> <input
						type="text" name="name" placeholder="Enter  Name"
						value="<%=ServletUtility.getParameter("name", request)%>">&emsp;

						<label><b>Date Of Visit:</b></label> <input type="text" id="udate"
						name="dateOfVisit" placeholder="Enter Date Of Visit"
						value="<%=ServletUtility.getParameter("dateOfVisit", request)%>">&emsp;

                        <label><b>Disease : </b></label> <%=HTMLUtility.getList("disease", String.valueOf(bean.getDisease()), diseaseMap)%>&emsp;

                        
						<input type="submit" name="operation" value="<%=PatientListCtl.OP_SEARCH%>"> &nbsp; 
						<input type="submit" name="operation" value="<%=PatientListCtl.OP_RESET%>"></td>
				</tr>
			</table>
			<br>

			<table border="1" style="width: 100%; border: groove;">
				<tr style="background-color: #e1e6f1e3;">
					<th width="5%"><input type="checkbox" id="selectall" /></th>
					<th width="5%">S.No</th>
					<th width="13%">Name</th>
					<th width="13%">Date Of Visit</th>
					<th width="10%">Mobile No</th>
					<th width="8%">Disease</th>
					<th width="5%">Edit</th>
				</tr>

				<%
					while (it.hasNext()) {
							bean = (PatientBean) it.next();

				%>

				<tr>
					<td style="text-align: center;">
					<input type="checkbox"	class="case" name="ids" value="<%=bean.getId()%>">
						
					</td>
					<td ><%=index++%></td>
					<td ><%=bean.getName()%></td>
					<td ><%=bean.getDateOfVisit()%></td>
					<td ><%=bean.getMobile()%></td>
					<td ><%=bean.getDisease()%></td>

					<td style="text-align: center;"><a href="PatientCtl?id=<%=bean.getId()%>">Edit</a>
					</td>
				</tr>

				<%
					}
				%>
			</table>

			<table style="width: 100%">
				<tr>
					<td style="width: 25%">
					<input type="submit" name="operation" value="<%=PatientListCtl.OP_PREVIOUS%>"
						<%=pageNo > 1 ? "" : "disabled"%>>
					</td>
					<td align="center" style="width: 25%"><input type="submit" name="operation" value="<%=PatientListCtl.OP_NEW%>"></td>
					
					<td align="center" style="width: 25%"><input type="submit" name="operation" value="<%=PatientListCtl.OP_DELETE%>"></td>
					<td style="width: 25%" align="right"><input type="submit" name="operation" value="<%=PatientListCtl.OP_NEXT%>"
						<%=nextListSize != 0 ? "" : "disabled"%>>
					</td>
				</tr>
			</table>

			<%
				} else {
			%>

			<table>
				<tr>
					<td align="right"><input type="submit" name="operation"
						value="<%=PatientListCtl.OP_BACK%>"></td>
				</tr>
			</table>

			<%
				}
			%>
		</form>
	</div>
	<%@ include file="Footer.jsp"%>
</body>
</html>
