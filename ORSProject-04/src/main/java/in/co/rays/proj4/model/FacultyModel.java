package in.co.rays.proj4.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import in.co.rays.proj4.bean.CollegeBean;
import in.co.rays.proj4.bean.CourseBean;
import in.co.rays.proj4.bean.FacultyBean;
import in.co.rays.proj4.bean.SubjectBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DatabaseException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.util.JDBCDataSource;

import org.apache.log4j.Logger;

/**
 * FacultyModel provides CRUD and search operations for {@link FacultyBean}
 * against the database table {@code st_faculty}.
 * <p>
 * It uses {@link JDBCDataSource} to obtain and close connections and throws
 * application-specific checked exceptions to signal error conditions.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 */
public class FacultyModel {

    private static Logger log = Logger.getLogger(FacultyModel.class);

    public Integer nextPk() throws DatabaseException {
        log.debug("Entering nextPk method");
        Connection conn = null;
        int pk = 0;
        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("select max(id) from st_faculty");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pk = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            log.debug("Next PK fetched: " + pk);
        } catch (Exception e) {
            log.error("DatabaseException in nextPk", e);
            throw new DatabaseException("Exception : Exception in getting PK");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("Exiting nextPk method");
        return pk + 1;
    }

    public long add(FacultyBean bean) throws ApplicationException, DuplicateRecordException {
        log.debug("Entering add method with FacultyBean: " + bean);
        Connection conn = null;
        int pk = 0;

        try {
            CollegeModel collegeModel = new CollegeModel();
            CollegeBean collegeBean = collegeModel.findByPk(bean.getCollegeId());
            bean.setCollegeName(collegeBean != null ? collegeBean.getName() : null);

            CourseModel courseModel = new CourseModel();
            CourseBean courseBean = courseModel.findByPk(bean.getCourseId());
            bean.setCourseName(courseBean != null ? courseBean.getName() : null);

            SubjectModel subjectModel = new SubjectModel();
            SubjectBean subjectBean = subjectModel.findByPk(bean.getSubjectId());
            bean.setSubjectName(subjectBean != null ? subjectBean.getName() : null);
        } catch (ApplicationException e) {
            log.error("Exception while resolving related names in add", e);
            throw new ApplicationException("Exception : Exception while resolving related names: " + e.getMessage());
        }

        FacultyBean existbean = findByEmail(bean.getEmail());

        if (existbean != null) {
            log.warn("Duplicate email found: " + bean.getEmail());
            throw new DuplicateRecordException("Email Id already exists");
        }

        try {
            conn = JDBCDataSource.getConnection();
            pk = nextPk();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(
                    "insert into st_faculty values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            pstmt.setInt(1, pk);
            pstmt.setString(2, bean.getFirstName());
            pstmt.setString(3, bean.getLastName());
            pstmt.setDate(4, new java.sql.Date(bean.getDob().getTime()));
            pstmt.setString(5, bean.getGender());
            pstmt.setString(6, bean.getMobileNo());
            pstmt.setString(7, bean.getEmail());
            pstmt.setLong(8, bean.getCollegeId());
            pstmt.setString(9, bean.getCollegeName());
            pstmt.setLong(10, bean.getCourseId());
            pstmt.setString(11, bean.getCourseName());
            pstmt.setLong(12, bean.getSubjectId());
            pstmt.setString(13, bean.getSubjectName());
            pstmt.setString(14, bean.getCreatedBy());
            pstmt.setString(15, bean.getModifiedBy());
            pstmt.setTimestamp(16, bean.getCreatedDatetime());
            pstmt.setTimestamp(17, bean.getModifiedDatetime());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.debug("Faculty added successfully with PK: " + pk);
        } catch (Exception e) {
            log.error("Exception in add Faculty", e);
            try {
                if (conn != null) {
                    conn.rollback();
                    log.debug("Transaction rollback in add method");
                }
            } catch (Exception ex) {
                log.error("Rollback exception in add", ex);
                throw new ApplicationException("Exception : add rollback exception " + ex.getMessage());
            }
            throw new ApplicationException("Exception : Exception in add Faculty");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("Exiting add method");
        return pk;
    }

    public void update(FacultyBean bean) throws ApplicationException, DuplicateRecordException {
        log.debug("Entering update method with FacultyBean: " + bean);
        Connection conn = null;

        try {
            CollegeModel collegeModel = new CollegeModel();
            CollegeBean collegeBean = collegeModel.findByPk(bean.getCollegeId());
            bean.setCollegeName(collegeBean != null ? collegeBean.getName() : null);

            CourseModel courseModel = new CourseModel();
            CourseBean courseBean = courseModel.findByPk(bean.getCourseId());
            bean.setCourseName(courseBean != null ? courseBean.getName() : null);

            SubjectModel subjectModel = new SubjectModel();
            SubjectBean subjectBean = subjectModel.findByPk(bean.getSubjectId());
            bean.setSubjectName(subjectBean != null ? subjectBean.getName() : null);
        } catch (ApplicationException e) {
            log.error("Exception while resolving related names in update", e);
            throw new ApplicationException("Exception : Exception while resolving related names: " + e.getMessage());
        }

        FacultyBean beanExist = findByEmail(bean.getEmail());
        if (beanExist != null && !(beanExist.getId() == bean.getId())) {
            log.warn("Duplicate email found during update: " + bean.getEmail());
            throw new DuplicateRecordException("EmailId is already exist");
        }
        try {
            conn = JDBCDataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(
                    "update st_faculty set first_name = ?, last_name = ?, dob = ?, gender = ?, mobile_no = ?, email = ?, college_id = ?, college_name = ?, course_id = ?, course_name = ?, subject_id = ?, subject_name = ?, created_by = ?, modified_by = ?, created_datetime = ?, modified_datetime = ? where id = ?");

            pstmt.setString(1, bean.getFirstName());
            pstmt.setString(2, bean.getLastName());
            pstmt.setDate(3, new java.sql.Date(bean.getDob().getTime()));
            pstmt.setString(4, bean.getGender());
            pstmt.setString(5, bean.getMobileNo());
            pstmt.setString(6, bean.getEmail());
            pstmt.setLong(7, bean.getCollegeId());
            pstmt.setString(8, bean.getCollegeName());
            pstmt.setLong(9, bean.getCourseId());
            pstmt.setString(10, bean.getCourseName());
            pstmt.setLong(11, bean.getSubjectId());
            pstmt.setString(12, bean.getSubjectName());
            pstmt.setString(13, bean.getCreatedBy());
            pstmt.setString(14, bean.getModifiedBy());
            pstmt.setTimestamp(15, bean.getCreatedDatetime());
            pstmt.setTimestamp(16, bean.getModifiedDatetime());
            pstmt.setLong(17, bean.getId());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.debug("Faculty updated successfully: " + bean.getId());
        } catch (Exception e) {
            log.error("Exception in update Faculty", e);
            try {
                if (conn != null) {
                    conn.rollback();
                    log.debug("Transaction rollback in update method");
                }
            } catch (Exception ex) {
                log.error("Rollback exception in update", ex);
                throw new ApplicationException("Exception : Delete rollback exception " + ex.getMessage());
            }
            throw new ApplicationException("Exception in updating Faculty ");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("Exiting update method");
    }

    public void delete(FacultyBean bean) throws ApplicationException {
        log.debug("Entering delete method for Faculty ID: " + bean.getId());
        Connection conn = null;
        try {
            conn = JDBCDataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement("delete from st_faculty where id = ?");
            pstmt.setLong(1, bean.getId());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.debug("Faculty deleted successfully: " + bean.getId());
        } catch (Exception e) {
            log.error("Exception in delete Faculty", e);
            try {
                if (conn != null) {
                    conn.rollback();
                    log.debug("Transaction rollback in delete method");
                }
            } catch (Exception ex) {
                log.error("Rollback exception in delete", ex);
                throw new ApplicationException("Exception : Delete rollback exception " + ex.getMessage());
            }
            throw new ApplicationException("Exception : Exception in delete Faculty");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("Exiting delete method");
    }

    public FacultyBean findByPk(long pk) throws ApplicationException {
        log.debug("Entering findByPk method with PK: " + pk);
        StringBuffer sql = new StringBuffer("select * from st_faculty where id = ?");
        FacultyBean bean = null;
        Connection conn = null;
        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, pk);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new FacultyBean();
                bean.setId(rs.getLong(1));
                bean.setFirstName(rs.getString(2));
                bean.setLastName(rs.getString(3));
                bean.setDob(rs.getDate(4));
                bean.setGender(rs.getString(5));
                bean.setMobileNo(rs.getString(6));
                bean.setEmail(rs.getString(7));
                bean.setCollegeId(rs.getLong(8));
                bean.setCollegeName(rs.getString(9));
                bean.setCourseId(rs.getLong(10));
                bean.setCourseName(rs.getString(11));
                bean.setSubjectId(rs.getLong(12));
                bean.setSubjectName(rs.getString(13));
                bean.setCreatedBy(rs.getString(14));
                bean.setModifiedBy(rs.getString(15));
                bean.setCreatedDatetime(rs.getTimestamp(16));
                bean.setModifiedDatetime(rs.getTimestamp(17));
            }
            rs.close();
            pstmt.close();
            log.debug("Faculty found by PK: " + pk + ", Faculty: " + bean);
        } catch (Exception e) {
            log.error("Exception in findByPk", e);
            throw new ApplicationException("Exception : Exception in getting Faculty by pk");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("Exiting findByPk method");
        return bean;
    }

    public FacultyBean findByEmail(String email) throws ApplicationException {
        log.debug("Entering findByEmail method with email: " + email);
        StringBuffer sql = new StringBuffer("select * from st_faculty where email = ?");
        FacultyBean bean = null;
        Connection conn = null;

        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new FacultyBean();
                bean.setId(rs.getLong(1));
                bean.setFirstName(rs.getString(2));
                bean.setLastName(rs.getString(3));
                bean.setDob(rs.getDate(4));
                bean.setGender(rs.getString(5));
                bean.setMobileNo(rs.getString(6));
                bean.setEmail(rs.getString(7));
                bean.setCollegeId(rs.getLong(8));
                bean.setCollegeName(rs.getString(9));
                bean.setCourseId(rs.getLong(10));
                bean.setCourseName(rs.getString(11));
                bean.setSubjectId(rs.getLong(12));
                bean.setSubjectName(rs.getString(13));
                bean.setCreatedBy(rs.getString(14));
                bean.setModifiedBy(rs.getString(15));
                bean.setCreatedDatetime(rs.getTimestamp(16));
                bean.setModifiedDatetime(rs.getTimestamp(17));
            }
            rs.close();
            pstmt.close();
            log.debug("Faculty found by email: " + email + ", Faculty: " + bean);
        } catch (Exception e) {
            log.error("Exception in findByEmail", e);
            throw new ApplicationException("Exception : Exception in getting Faculty by Email");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("Exiting findByEmail method");
        return bean;
    }

    public List<FacultyBean> list() throws ApplicationException {
        log.debug("Entering list method");
        List<FacultyBean> list = search(null, 0, 0);
        log.debug("Exiting list method with list size: " + list.size());
        return list;
    }

    public List<FacultyBean> search(FacultyBean bean, int pageNo, int pageSize) throws ApplicationException {
        log.debug("Entering search method with bean: " + bean + ", pageNo: " + pageNo + ", pageSize: " + pageSize);
        StringBuffer sql = new StringBuffer("select * from st_faculty where 1=1");

        if (bean != null) {
            if (bean.getId() > 0) {
                sql.append(" and id = " + bean.getId());
            }
            if (bean.getCollegeId() > 0) {
                sql.append(" and college_id = " + bean.getCollegeId());
            }
            if (bean.getSubjectId() > 0) {
                sql.append(" and subject_id = " + bean.getSubjectId());
            }
            if (bean.getCourseId() > 0) {
                sql.append(" and course_id = " + bean.getCourseId());
            }
            if (bean.getFirstName() != null && bean.getFirstName().length() > 0) {
                sql.append(" and first_name like '" + bean.getFirstName() + "%'");
            }
            if (bean.getLastName() != null && bean.getLastName().length() > 0) {
                sql.append(" and last_name like '" + bean.getLastName() + "%'");
            }
            if (bean.getGender() != null && bean.getGender().length() > 0) {
                sql.append(" and gender like '" + bean.getGender() + "%'");
            }
            if (bean.getDob() != null) {
                sql.append(" and dob = '" + new java.sql.Date(bean.getDob().getTime()) + "'");
            }
            if (bean.getEmail() != null && bean.getEmail().length() > 0) {
                sql.append(" and email like '" + bean.getEmail() + "%'");
            }
            if (bean.getMobileNo() != null && bean.getMobileNo().length() > 0) {
                sql.append(" and mobile_no = " + bean.getMobileNo());
            }
            if (bean.getCourseName() != null && bean.getCourseName().length() > 0) {
                sql.append(" and course_name like '" + bean.getCourseName() + "%'");
            }
            if (bean.getCollegeName() != null && bean.getCollegeName().length() > 0) {
                sql.append(" and college_name like '" + bean.getCollegeName() + "%'");
            }
            if (bean.getSubjectName() != null && bean.getSubjectName().length() > 0) {
                sql.append(" and subject_name like '" + bean.getSubjectName() + "%'");
            }
        }
        if (pageSize > 0) {
            pageNo = (pageNo - 1) * pageSize;
            sql.append(" limit " + pageNo + ", " + pageSize);
        }

        ArrayList<FacultyBean> list = new ArrayList<FacultyBean>();
        Connection conn = null;
        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new FacultyBean();
                bean.setId(rs.getLong(1));
                bean.setFirstName(rs.getString(2));
                bean.setLastName(rs.getString(3));
                bean.setDob(rs.getDate(4));
                bean.setGender(rs.getString(5));
                bean.setMobileNo(rs.getString(6));
                bean.setEmail(rs.getString(7));
                bean.setCollegeId(rs.getLong(8));
                bean.setCollegeName(rs.getString(9));
                bean.setCourseId(rs.getLong(10));
                bean.setCourseName(rs.getString(11));
                bean.setSubjectId(rs.getLong(12));
                bean.setSubjectName(rs.getString(13));
                bean.setCreatedBy(rs.getString(14));
                bean.setModifiedBy(rs.getString(15));
                bean.setCreatedDatetime(rs.getTimestamp(16));
                bean.setModifiedDatetime(rs.getTimestamp(17));
                list.add(bean);
            }
            rs.close();
            pstmt.close();
            log.debug("Search completed with result size: " + list.size());
        } catch (Exception e) {
            log.error("Exception in search Faculty", e);
            throw new ApplicationException("Exception : Exception in search Faculty");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("Exiting search method");
        return list;
    }
}
