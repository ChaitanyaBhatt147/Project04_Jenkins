package in.co.rays.proj4.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.CollegeBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DatabaseException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.util.JDBCDataSource;

/**
 * CollegeModel provides CRUD and search operations for {@link CollegeBean}
 * against the database table {@code st_college}.
 * <p>
 * It uses {@link JDBCDataSource} to obtain and close connections and throws
 * application-specific checked exceptions to signal error conditions.
 * </p>
 * 
 * @author Chaitanya Bhatt
 * @version 1.0
 */
public class CollegeModel {

    /** Log4j Logger */
    private static final Logger log = Logger.getLogger(CollegeModel.class);

    /**
     * Returns the next primary key value for the st_college table.
     *
     * @return next primary key value
     * @throws DatabaseException if a database error occurs while retrieving the maximum id
     */
    public Integer nextPk() throws DatabaseException {
        log.debug("CollegeModel nextPk() called");
        Connection conn = null;
        int pk = 0;

        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("select max(id) from st_college");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pk = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            log.error("Exception in nextPk", e);
            throw new DatabaseException("Exception : Exception in getting PK");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("Next PK generated: " + (pk + 1));
        return pk + 1;
    }

    /**
     * Adds a new College record to the database.
     *
     * @param bean {@link CollegeBean} containing college data to add
     * @return the primary key of the newly inserted college
     * @throws ApplicationException       if a general application/database error occurs
     * @throws DuplicateRecordException   if a college with same name already exists
     */
    public long add(CollegeBean bean) throws ApplicationException, DuplicateRecordException {
        log.debug("CollegeModel add() called for College Name: " + bean.getName());
        Connection conn = null;
        int pk = 0;

        CollegeBean duplicateCollegeName = findByName(bean.getName());

        if (duplicateCollegeName != null) {
            log.warn("Duplicate College Name found: " + bean.getName());
            throw new DuplicateRecordException("College Name already exists");
        }

        try {
            conn = JDBCDataSource.getConnection();
            pk = nextPk();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn
                    .prepareStatement("insert into st_college values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            pstmt.setInt(1, pk);
            pstmt.setString(2, bean.getName());
            pstmt.setString(3, bean.getAddress());
            pstmt.setString(4, bean.getState());
            pstmt.setString(5, bean.getCity());
            pstmt.setString(6, bean.getPhoneNo());
            pstmt.setString(7, bean.getCreatedBy());
            pstmt.setString(8, bean.getModifiedBy());
            pstmt.setTimestamp(9, bean.getCreatedDatetime());
            pstmt.setTimestamp(10, bean.getModifiedDatetime());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("College added successfully: " + bean.getName() + ", ID: " + pk);
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception ex) {
                log.error("Rollback exception in add()", ex);
                throw new ApplicationException("Exception : add rollback exception " + ex.getMessage());
            }
            log.error("Exception in add College", e);
            throw new ApplicationException("Exception : Exception in add College");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        return pk;
    }

    /**
     * Updates an existing College record.
     *
     * @param bean {@link CollegeBean} containing updated college data
     * @throws ApplicationException     if a general application/database error occurs
     * @throws DuplicateRecordException if another college with same name exists
     */
    public void update(CollegeBean bean) throws ApplicationException, DuplicateRecordException {
        log.debug("CollegeModel update() called for ID: " + bean.getId());

        Connection conn = null;
        CollegeBean beanExist = findByName(bean.getName());

        if (beanExist != null && beanExist.getId() != bean.getId()) {
            log.warn("Duplicate College Name found for update: " + bean.getName());
            throw new DuplicateRecordException("College is already exist");
        }

        try {
            conn = JDBCDataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(
                    "update st_college set name = ?, address = ?, state = ?, city = ?, phone_no = ?, created_by = ?, modified_by = ?, created_datetime = ?, modified_datetime = ? where id = ?");
            pstmt.setString(1, bean.getName());
            pstmt.setString(2, bean.getAddress());
            pstmt.setString(3, bean.getState());
            pstmt.setString(4, bean.getCity());
            pstmt.setString(5, bean.getPhoneNo());
            pstmt.setString(6, bean.getCreatedBy());
            pstmt.setString(7, bean.getModifiedBy());
            pstmt.setTimestamp(8, bean.getCreatedDatetime());
            pstmt.setTimestamp(9, bean.getModifiedDatetime());
            pstmt.setLong(10, bean.getId());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("College updated successfully: " + bean.getName() + ", ID: " + bean.getId());
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception ex) {
                log.error("Rollback exception in update()", ex);
                throw new ApplicationException("Exception : Delete rollback exception " + ex.getMessage());
            }
            log.error("Exception in updating College", e);
            throw new ApplicationException("Exception in updating College ");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
    }

    /**
     * Deletes a College record.
     *
     * @param bean {@link CollegeBean} whose id identifies the college to delete
     * @throws ApplicationException if a general application/database error occurs
     */
    public void delete(CollegeBean bean) throws ApplicationException {
        log.debug("CollegeModel delete() called for ID: " + bean.getId());
        Connection conn = null;
        try {
            conn = JDBCDataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement("delete from st_college where id = ?");
            pstmt.setLong(1, bean.getId());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("College deleted successfully, ID: " + bean.getId());
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception ex) {
                log.error("Rollback exception in delete()", ex);
                throw new ApplicationException("Exception : Delete rollback exception " + ex.getMessage());
            }
            log.error("Exception in deleting College", e);
            throw new ApplicationException("Exception : Exception in delete college");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
    }

    /**
     * Finds a College record by primary key.
     *
     * @param pk primary key (id) of the college
     * @return {@link CollegeBean} if found; {@code null} otherwise
     * @throws ApplicationException if a general application/database error occurs
     */
    public CollegeBean findByPk(long pk) throws ApplicationException {
        log.debug("CollegeModel findByPk() called for ID: " + pk);
        StringBuffer sql = new StringBuffer("select * from st_college where id = ?");

        CollegeBean bean = null;
        Connection conn = null;

        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, pk);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new CollegeBean();
                bean.setId(rs.getLong(1));
                bean.setName(rs.getString(2));
                bean.setAddress(rs.getString(3));
                bean.setState(rs.getString(4));
                bean.setCity(rs.getString(5));
                bean.setPhoneNo(rs.getString(6));
                bean.setCreatedBy(rs.getString(7));
                bean.setModifiedBy(rs.getString(8));
                bean.setCreatedDatetime(rs.getTimestamp(9));
                bean.setModifiedDatetime(rs.getTimestamp(10));
            }
            rs.close();
            pstmt.close();
            log.info("College found by PK: " + pk);
        } catch (Exception e) {
            log.error("Exception in findByPk()", e);
            throw new ApplicationException("Exception : Exception in getting College by pk");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        return bean;
    }

    /**
     * Finds a College record by its name.
     *
     * @param name name of the college
     * @return {@link CollegeBean} if found; {@code null} otherwise
     * @throws ApplicationException if a general application/database error occurs
     */
    public CollegeBean findByName(String name) throws ApplicationException {
        log.debug("CollegeModel findByName() called for Name: " + name);

        StringBuffer sql = new StringBuffer("select * from st_college where name = ?");

        CollegeBean bean = null;
        Connection conn = null;

        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new CollegeBean();
                bean.setId(rs.getLong(1));
                bean.setName(rs.getString(2));
                bean.setAddress(rs.getString(3));
                bean.setState(rs.getString(4));
                bean.setCity(rs.getString(5));
                bean.setPhoneNo(rs.getString(6));
                bean.setCreatedBy(rs.getString(7));
                bean.setModifiedBy(rs.getString(8));
                bean.setCreatedDatetime(rs.getTimestamp(9));
                bean.setModifiedDatetime(rs.getTimestamp(10));
            }
            rs.close();
            pstmt.close();
            log.info("College found by Name: " + name);
        } catch (Exception e) {
            log.error("Exception in findByName()", e);
            throw new ApplicationException("Exception : Exception in getting College by Name");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        return bean;
    }

    /**
     * Returns a list of all colleges. This is a convenience wrapper around
     * {@link #search(CollegeBean, int, int)} with no filter and no pagination.
     *
     * @return list of all {@link CollegeBean}
     * @throws ApplicationException if a general application/database error occurs
     */
    public List<CollegeBean> list() throws ApplicationException {
        log.debug("CollegeModel list() called");
        return search(null, 0, 0);
    }

    /**
     * Searches for colleges matching the criteria provided in {@code bean}.
     * If {@code pageSize} &gt; 0, results are paginated.
     *
     * @param bean     filter criteria; if {@code null} returns all records
     * @param pageNo   page number (1-based) when paginating; ignored if pageSize is 0
     * @param pageSize number of records per page; pass 0 to disable pagination
     * @return list of matching {@link CollegeBean}
     * @throws ApplicationException if a general application/database error occurs
     */
    public List<CollegeBean> search(CollegeBean bean, int pageNo, int pageSize) throws ApplicationException {
        log.debug("CollegeModel search() called with pageNo=" + pageNo + ", pageSize=" + pageSize);
        StringBuffer sql = new StringBuffer("select * from st_college where 1 = 1");

        if (bean != null) {
            if (bean.getId() > 0) {
                sql.append(" and id = " + bean.getId());
            }
            if (bean.getName() != null && bean.getName().length() > 0) {
                sql.append(" and name like '" + bean.getName() + "%'");
            }
            if (bean.getAddress() != null && bean.getAddress().length() > 0) {
                sql.append(" and address like '" + bean.getAddress() + "%'");
            }
            if (bean.getState() != null && bean.getState().length() > 0) {
                sql.append(" and state like '" + bean.getState() + "%'");
            }
            if (bean.getCity() != null && bean.getCity().length() > 0) {
                sql.append(" and city like '" + bean.getCity() + "%'");
            }
            if (bean.getPhoneNo() != null && bean.getPhoneNo().length() > 0) {
                sql.append(" and phone_no = " + bean.getPhoneNo());
            }
        }

        if (pageSize > 0) {
            pageNo = (pageNo - 1) * pageSize;
            sql.append(" limit " + pageNo + ", " + pageSize);
        }

        ArrayList<CollegeBean> list = new ArrayList<CollegeBean>();
        Connection conn = null;

        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new CollegeBean();
                bean.setId(rs.getLong(1));
                bean.setName(rs.getString(2));
                bean.setAddress(rs.getString(3));
                bean.setState(rs.getString(4));
                bean.setCity(rs.getString(5));
                bean.setPhoneNo(rs.getString(6));
                bean.setCreatedBy(rs.getString(7));
                bean.setModifiedBy(rs.getString(8));
                bean.setCreatedDatetime(rs.getTimestamp(9));
                bean.setModifiedDatetime(rs.getTimestamp(10));
                list.add(bean);
            }
            rs.close();
            pstmt.close();
            log.info("College search completed, results found: " + list.size());
        } catch (Exception e) {
            log.error("Exception in search()", e);
            throw new ApplicationException("Exception : Exception in search college");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        return list;
    }
}
