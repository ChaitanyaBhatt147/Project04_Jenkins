<%@page import="in.co.rays.proj4.bean.RoleBean"%>
<%@page import="in.co.rays.proj4.controller.ORSView"%>
<%@page import="in.co.rays.proj4.controller.LoginCtl"%>
<%@page import="in.co.rays.proj4.bean.UserBean"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Insert title here</title>
<!-- Include jQuery -->
<script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
<!-- Include jQuery UI -->
<script src="https://code.jquery.com/ui/1.13.2/jquery-ui.min.js"></script>
<!-- Include jQuery UI CSS -->
<link rel="stylesheet"
	href="https://code.jquery.com/ui/1.13.2/themes/base/jquery-ui.css">
<script src="/ORSProject-04/js/checkbox.js"></script>
<script src="/ORSProject-04/js/datepicker.js"></script>
</head>
<body>
	<img src="<%=ORSView.APP_CONTEXT%>/img/customLogo.jpg" align="right" width="100" height="40">
	<%
    UserBean user = (UserBean) session.getAttribute("user");
    boolean loggedIn = user != null;
%>

<!-- Logged In View -->
<% if (loggedIn) { %>

    <h3>Hi, <%=user.getFirstName()%> (<%=session.getAttribute("role")%>)</h3>

    <!-- Common menus -->
    <a href="<%=ORSView.MY_PROFILE_CTL%>"><b>My Profile</b></a> |
    <a href="<%=ORSView.CHANGE_PASSWORD_CTL%>"><b>Change Password</b></a> |
    <a href="<%=ORSView.GET_MARKSHEET_CTL%>"><b>Get Marksheet</b></a> |
    <a href="<%=ORSView.MARKSHEET_MERIT_LIST_CTL%>"><b>Merit List</b></a> |

    <!-- Admin Only -->
    <% if (user.getRoleId() == RoleBean.ADMIN) { %>

        <a href="<%=ORSView.USER_CTL%>"><b>Add User</b></a> |
        <a href="<%=ORSView.USER_LIST_CTL%>"><b>User List</b></a> |
        <a href="<%=ORSView.ROLE_CTL%>"><b>Add Role</b></a> |
        <a href="<%=ORSView.ROLE_LIST_CTL%>"><b>Role List</b></a> |
        
        <a href="<%=ORSView.PATIENT_CTL%>" ><b>Add Patient</b></a>|
        <a href="<%=ORSView.PATIENT_LIST_CTL%>" ><b>Patient List</b></a>|

        <!-- College menus (Admin only since College role removed) -->
        <a href="<%=ORSView.COLLEGE_CTL%>"><b>Add College</b></a> |
        <a href="<%=ORSView.COLLEGE_LIST_CTL%>"><b>College List</b></a> |

    <% } %>

    <!-- Faculty + Admin Menus (College role replaced with Faculty) -->
    <% if (user.getRoleId() == RoleBean.FACULTY || user.getRoleId() == RoleBean.ADMIN) { %>

        <a href="<%=ORSView.STUDENT_CTL%>"><b>Add Student</b></a> |
        <a href="<%=ORSView.STUDENT_LIST_CTL%>"><b>Student List</b></a> |

        <a href="<%=ORSView.MARKSHEET_CTL%>"><b>Add Marksheet</b></a> |
        <a href="<%=ORSView.MARKSHEET_LIST_CTL%>"><b>Marksheet List</b></a> |

        <a href="<%=ORSView.COURSE_CTL%>"><b>Add Course</b></a> |
        <a href="<%=ORSView.COURSE_LIST_CTL%>"><b>Course List</b></a> |

        <a href="<%=ORSView.SUBJECT_CTL%>"><b>Add Subject</b></a> |
        <a href="<%=ORSView.SUBJECT_LIST_CTL%>"><b>Subject List</b></a> |

        <a href="<%=ORSView.FACULTY_CTL%>"><b>Add Faculty</b></a> |
        <a href="<%=ORSView.FACULTY_LIST_CTL%>"><b>Faculty List</b></a> |

        <a href="<%=ORSView.TIMETABLE_CTL%>"><b>Add Timetable</b></a> |
        <a href="<%=ORSView.TIMETABLE_LIST_CTL%>"><b>Timetable List</b></a> |

    <% } %>

    <!-- Admin Only -->
    <a href="<%=ORSView.JAVA_DOC%>" target="blank"><b>Java Doc</b></a> |

    <!-- Logout -->
    <a href="<%=ORSView.LOGIN_CTL%>?operation=<%=LoginCtl.OP_LOG_OUT%>"><b>Logout</b></a>

<% } else { %>

<!-- Guest View -->
    <h3>Hi, Guest</h3>
    <a href="WelcomeCtl"><b>Welcome</b></a> |
    <a href="LoginCtl"><b>Login</b></a>

<% } %>
	<hr>
</body>
</html>
