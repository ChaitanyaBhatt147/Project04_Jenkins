package in.co.rays.proj4.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import in.co.rays.proj4.bean.CourseBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DatabaseException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.util.JDBCDataSource;

import org.apache.log4j.Logger; // Added import

/**
 * CourseModel provides CRUD and search operations for {@link CourseBean}
 * against the database table {@code st_course}.
 * <p>
 * It uses {@link JDBCDataSource} to obtain and close connections and throws
 * application-specific checked exceptions to signal error conditions.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 */
public class CourseModel {

    private static Logger log = Logger.getLogger(CourseModel.class); // Logger declaration

    /**
     * Returns the next primary key value for the st_course table.
     *
     * @return next primary key value
     * @throws DatabaseException if a database error occurs while retrieving the maximum id
     */
    public Integer nextPk() throws DatabaseException {
        log.debug("CourseModel.nextPk() start");
        Connection conn = null;
        int pk = 0;
        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("select max(id) from st_course");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pk = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            log.error("Exception in getting PK", e);
            throw new DatabaseException("Exception : Exception in getting PK");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("CourseModel.nextPk() end with pk=" + (pk + 1));
        return pk + 1;
    }

    /**
     * Adds a new Course record to the database.
     * <p>
     * Before insertion it checks for duplicate course name and throws
     * {@link DuplicateRecordException} if a record with same name exists.
     * </p>
     *
     * @param bean {@link CourseBean} containing course data to add
     * @return the primary key of the newly inserted course
     * @throws ApplicationException     if a general application/database error occurs
     * @throws DuplicateRecordException if a course with same name already exists
     */
    public long add(CourseBean bean) throws ApplicationException, DuplicateRecordException {
        log.debug("CourseModel.add() start with bean=" + bean.getName());
        Connection conn = null;
        int pk = 0;

        CourseBean duplicateCourse = findByName(bean.getName());

        if (duplicateCourse != null) {
            log.warn("Duplicate course name: " + bean.getName());
            throw new DuplicateRecordException("Course Name already exists");
        }

        try {
            conn = JDBCDataSource.getConnection();
            pk = nextPk();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement("insert into st_course values(?, ?, ?, ?, ?, ?, ?, ?)");
            pstmt.setInt(1, pk);
            pstmt.setString(2, bean.getName());
            pstmt.setString(3, bean.getDuration());
            pstmt.setString(4, bean.getDescription());
            pstmt.setString(5, bean.getCreatedBy());
            pstmt.setString(6, bean.getModifiedBy());
            pstmt.setTimestamp(7, bean.getCreatedDatetime());
            pstmt.setTimestamp(8, bean.getModifiedDatetime());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("Course added successfully with pk=" + pk);
        } catch (Exception e) {
            log.error("Exception in adding Course", e);
            try {
                if (conn != null) {
                    conn.rollback();
                    log.info("Rollback successful");
                }
            } catch (Exception ex) {
                log.error("Rollback failed", ex);
                throw new ApplicationException("Exception : add rollback exception " + ex.getMessage());
            }
            throw new ApplicationException("Exception : Exception in add Course");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("CourseModel.add() end");
        return pk;
    }

    /**
     * Updates an existing Course record.
     * <p>
     * It checks for duplicate course name (other than the current record) and
     * throws {@link DuplicateRecordException} if a different record with same
     * name exists.
     * </p>
     *
     * @param bean {@link CourseBean} containing updated course data
     * @throws ApplicationException     if a general application/database error occurs
     * @throws DuplicateRecordException if another course with same name exists
     */
    public void update(CourseBean bean) throws ApplicationException, DuplicateRecordException {
        log.debug("CourseModel.update() start with id=" + bean.getId());
        Connection conn = null;

        CourseBean duplicateCourse = findByName(bean.getName());
        if (duplicateCourse != null && duplicateCourse.getId() != bean.getId()) {
            log.warn("Duplicate course name for update: " + bean.getName());
            throw new DuplicateRecordException("Course already exists");
        }
        try {
            conn = JDBCDataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(
                    "update st_course set name = ?, duration = ?, description = ?, created_by = ?, modified_by = ?, created_datetime = ?, modified_datetime = ? where id = ?");
            pstmt.setString(1, bean.getName());
            pstmt.setString(2, bean.getDuration());
            pstmt.setString(3, bean.getDescription());
            pstmt.setString(4, bean.getCreatedBy());
            pstmt.setString(5, bean.getModifiedBy());
            pstmt.setTimestamp(6, bean.getCreatedDatetime());
            pstmt.setTimestamp(7, bean.getModifiedDatetime());
            pstmt.setLong(8, bean.getId());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("Course updated successfully with id=" + bean.getId());
        } catch (Exception e) {
            log.error("Exception in updating Course", e);
            try {
                if (conn != null) {
                    conn.rollback();
                    log.info("Rollback successful for update");
                }
            } catch (Exception ex) {
                log.error("Rollback failed for update", ex);
                throw new ApplicationException("Exception : Delete rollback exception " + ex.getMessage());
            }
            throw new ApplicationException("Exception in updating Course ");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("CourseModel.update() end");
    }

    /**
     * Deletes a Course record.
     *
     * @param bean {@link CourseBean} whose id identifies the course to delete
     * @throws ApplicationException if a general application/database error occurs
     */
    public void delete(CourseBean bean) throws ApplicationException {
        log.debug("CourseModel.delete() start with id=" + bean.getId());
        Connection conn = null;
        try {
            conn = JDBCDataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement("delete from st_course where id = ?");
            pstmt.setLong(1, bean.getId());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("Course deleted successfully with id=" + bean.getId());
        } catch (Exception e) {
            log.error("Exception in deleting Course", e);
            try {
                if (conn != null) {
                    conn.rollback();
                    log.info("Rollback successful for delete");
                }
            } catch (Exception ex) {
                log.error("Rollback failed for delete", ex);
                throw new ApplicationException("Exception : Delete rollback exception " + ex.getMessage());
            }
            throw new ApplicationException("Exception : Exception in delete Course");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("CourseModel.delete() end");
    }

    /**
     * Finds a Course record by primary key.
     *
     * @param pk primary key (id) of the course
     * @return {@link CourseBean} if found; {@code null} otherwise
     * @throws ApplicationException if a general application/database error occurs
     */
    public CourseBean findByPk(long pk) throws ApplicationException {
        log.debug("CourseModel.findByPk() start with pk=" + pk);
        StringBuffer sql = new StringBuffer("select * from st_course where id = ?");
        CourseBean bean = null;
        Connection conn = null;
        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, pk);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new CourseBean();
                bean.setId(rs.getLong(1));
                bean.setName(rs.getString(2));
                bean.setDuration(rs.getString(3));
                bean.setDescription(rs.getString(4));
                bean.setCreatedBy(rs.getString(5));
                bean.setModifiedBy(rs.getString(6));
                bean.setCreatedDatetime(rs.getTimestamp(7));
                bean.setModifiedDatetime(rs.getTimestamp(8));
            }
            rs.close();
            pstmt.close();
            log.info("Course found by pk=" + pk);
        } catch (Exception e) {
            log.error("Exception in finding Course by pk", e);
            throw new ApplicationException("Exception : Exception in getting Course by pk");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("CourseModel.findByPk() end");
        return bean;
    }

    /**
     * Finds a Course record by its name.
     *
     * @param name name of the course
     * @return {@link CourseBean} if found; {@code null} otherwise
     * @throws ApplicationException if a general application/database error occurs
     */
    public CourseBean findByName(String name) throws ApplicationException {
        log.debug("CourseModel.findByName() start with name=" + name);
        StringBuffer sql = new StringBuffer("select * from st_course where name = ?");
        CourseBean bean = null;
        Connection conn = null;
        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new CourseBean();
                bean.setId(rs.getLong(1));
                bean.setName(rs.getString(2));
                bean.setDuration(rs.getString(3));
                bean.setDescription(rs.getString(4));
                bean.setCreatedBy(rs.getString(5));
                bean.setModifiedBy(rs.getString(6));
                bean.setCreatedDatetime(rs.getTimestamp(7));
                bean.setModifiedDatetime(rs.getTimestamp(8));
            }
            rs.close();
            pstmt.close();
            log.info("Course found by name=" + name);
        } catch (Exception e) {
            log.error("Exception in finding Course by name", e);
            throw new ApplicationException("Exception : Exception in getting Course by Course Name");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("CourseModel.findByName() end");
        return bean;
    }

    /**
     * Returns a list of all courses. This is a convenience wrapper around
     * {@link #search(CourseBean, int, int)} with no filter and no pagination.
     *
     * @return list of all {@link CourseBean}
     * @throws ApplicationException if a general application/database error occurs
     */
    public List<CourseBean> list() throws ApplicationException {
        log.debug("CourseModel.list() start");
        List<CourseBean> list = search(null, 0, 0);
        log.debug("CourseModel.list() end with " + list.size() + " records");
        return list;
    }

    /**
     * Searches for courses matching the criteria provided in {@code bean}.
     * If {@code pageSize} &gt; 0, results are paginated.
     *
     * @param bean     filter criteria; if {@code null} returns all records
     * @param pageNo   page number (1-based) when paginating; ignored if pageSize is 0
     * @param pageSize number of records per page; pass 0 to disable pagination
     * @return list of matching {@link CourseBean}
     * @throws ApplicationException if a general application/database error occurs
     */
    public List<CourseBean> search(CourseBean bean, int pageNo, int pageSize) throws ApplicationException {
        log.debug("CourseModel.search() start with pageNo=" + pageNo + " pageSize=" + pageSize);
        StringBuffer sql = new StringBuffer("select * from st_course where 1=1");

        if (bean != null) {
            if (bean.getId() > 0) {
                sql.append(" and id = " + bean.getId());
            }
            if (bean.getName() != null && bean.getName().length() > 0) {
                sql.append(" and name like '%" + bean.getName() + "%'");
            }
            if (bean.getDuration() != null && bean.getDuration().length() > 0) {
                sql.append(" and duration like '%" + bean.getDuration() + "%'");
            }
            if (bean.getDescription() != null && bean.getDescription().length() > 0) {
                sql.append(" and description like '%" + bean.getDescription() + "%'");
            }
        }

        if (pageSize > 0) {
            pageNo = (pageNo - 1) * pageSize;
            sql.append(" limit " + pageNo + ", " + pageSize);
        }

        ArrayList<CourseBean> list = new ArrayList<CourseBean>();
        Connection conn = null;
        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new CourseBean();
                bean.setId(rs.getLong(1));
                bean.setName(rs.getString(2));
                bean.setDuration(rs.getString(3));
                bean.setDescription(rs.getString(4));
                bean.setCreatedBy(rs.getString(5));
                bean.setModifiedBy(rs.getString(6));
                bean.setCreatedDatetime(rs.getTimestamp(7));
                bean.setModifiedDatetime(rs.getTimestamp(8));
                list.add(bean);
            }
            rs.close();
            pstmt.close();
            log.info("Course search successful. Records found: " + list.size());
        } catch (Exception e) {
            log.error("Exception in searching Course", e);
            throw new ApplicationException("Exception : Exception in search Course");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("CourseModel.search() end");
        return list;
    }
}
