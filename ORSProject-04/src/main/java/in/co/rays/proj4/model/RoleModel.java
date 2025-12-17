package in.co.rays.proj4.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import in.co.rays.proj4.bean.RoleBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DatabaseException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.util.JDBCDataSource;

/**
 * RoleModel provides CRUD operations and search/list utilities for RoleBean.
 *
 * <p>
 * This class uses JDBC to interact with the {@code st_role} table and throws
 * application-specific exceptions defined in the project.
 * </p>
 *
 * @author Chaitanya Bhatt
 * @version 1.0
 */
public class RoleModel {

    private static Logger log = Logger.getLogger(RoleModel.class);

    /**
     * Returns next primary key value for st_role table.
     *
     * @return next primary key (Integer)
     * @throws DatabaseException if a database access error occurs
     */
    public Integer nextPk() throws DatabaseException {
        log.debug("RoleModel.nextPk() started");
        Connection conn = null;
        int pk = 0;

        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("select max(id) from st_role");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                pk = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            log.error("Exception in getting PK", e);
            throw new DatabaseException("Exception : Exception in getting PK");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("RoleModel.nextPk() ended with pk=" + (pk + 1));
        return pk + 1;
    }

    /**
     * Adds a new role record into database.
     */
    public long add(RoleBean bean) throws DatabaseException, ApplicationException, DuplicateRecordException {
        log.debug("RoleModel.add() started with Role Name: " + bean.getName());
        Connection conn = null;
        int pk = 0;

        RoleBean existBRole = findByName(bean.getName());
        if (existBRole != null) {
            log.warn("Role already exists: " + bean.getName());
            throw new DuplicateRecordException("Role already exists");
        }

        try {
            pk = nextPk();
            conn = JDBCDataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement("insert into st_role values (?, ?, ?, ?, ?, ?, ?)");
            pstmt.setInt(1, pk);
            pstmt.setString(2, bean.getName());
            pstmt.setString(3, bean.getDescription());
            pstmt.setString(4, bean.getCreatedBy());
            pstmt.setString(5, bean.getModifiedBy());
            pstmt.setTimestamp(6, bean.getCreatedDatetime());
            pstmt.setTimestamp(7, bean.getModifiedDatetime());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("Role added successfully with ID: " + pk);
        } catch (SQLException e) {
            log.error("Exception in adding Role", e);
            try {
                conn.rollback();
                log.info("Rollback successful in RoleModel.add()");
            } catch (SQLException ex) {
                log.error("Rollback failed in RoleModel.add()", ex);
                throw new ApplicationException("Exception: add rollback exception " + ex.getMessage());
            }
            throw new ApplicationException("Exception: Exception in add Role");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("RoleModel.add() ended");
        return pk;
    }

    /**
     * Deletes a role record from database.
     */
    public void delete(RoleBean bean) throws ApplicationException {
        log.debug("RoleModel.delete() started with ID: " + bean.getId());
        Connection conn = null;
        try {
            conn = JDBCDataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement("delete from st_role where id = ?");
            pstmt.setLong(1, bean.getId());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("Role deleted successfully with ID: " + bean.getId());
        } catch (SQLException e) {
            log.error("Exception in deleting Role", e);
            try {
                conn.rollback();
                log.info("Rollback successful in RoleModel.delete()");
            } catch (SQLException ex) {
                log.error("Rollback failed in RoleModel.delete()", ex);
                throw new ApplicationException("Exception : Delete rollback exception " + ex.getMessage());
            }
            throw new ApplicationException("Exception : Exception in delete Role");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("RoleModel.delete() ended");
    }

    /**
     * Updates an existing role record.
     */
    public void update(RoleBean bean) throws ApplicationException, DuplicateRecordException {
        log.debug("RoleModel.update() started with ID: " + bean.getId());
        Connection conn = null;
        RoleBean existBRole = findByName(bean.getName());

        if (existBRole != null && existBRole.getId() != bean.getId()) {
            log.warn("Role name already exists for another record: " + bean.getName());
            throw new DuplicateRecordException("Role already exists");
        }

        try {
            conn = JDBCDataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(
                    "update st_role set name = ?, description = ?, created_by = ?, modified_by = ?, created_datetime = ?, modified_datetime = ? where id = ?");
            pstmt.setString(1, bean.getName());
            pstmt.setString(2, bean.getDescription());
            pstmt.setString(3, bean.getCreatedBy());
            pstmt.setString(4, bean.getModifiedBy());
            pstmt.setTimestamp(5, bean.getCreatedDatetime());
            pstmt.setTimestamp(6, bean.getModifiedDatetime());
            pstmt.setLong(7, bean.getId());
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("Role updated successfully with ID: " + bean.getId());
        } catch (Exception e) {
            log.error("Exception in updating Role", e);
            try {
                conn.rollback();
                log.info("Rollback successful in RoleModel.update()");
            } catch (Exception ex) {
                log.error("Rollback failed in RoleModel.update()", ex);
                throw new ApplicationException("Exception : Delete rollback exception " + ex.getMessage());
            }
            throw new ApplicationException("Exception in updating Role");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("RoleModel.update() ended");
    }

    /**
     * Finds a role by primary key.
     */
    public RoleBean findByPk(long pk) throws ApplicationException {
        log.debug("RoleModel.findByPk() started with PK: " + pk);
        Connection conn = null;
        RoleBean bean = null;
        StringBuffer sql = new StringBuffer("select * from st_role where id = ?");
        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, pk);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new RoleBean();
                bean.setId(rs.getLong(1));
                bean.setName(rs.getString(2));
                bean.setDescription(rs.getString(3));
                bean.setCreatedBy(rs.getString(4));
                bean.setModifiedBy(rs.getString(5));
                bean.setCreatedDatetime(rs.getTimestamp(6));
                bean.setModifiedDatetime(rs.getTimestamp(7));
            }
            pstmt.close();
            log.info("Role found with PK: " + pk);
        } catch (Exception e) {
            log.error("Exception in finding Role by PK", e);
            throw new ApplicationException("Exception : Exception in getting User by pk");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("RoleModel.findByPk() ended");
        return bean;
    }

    /**
     * Finds a role by name.
     */
    public RoleBean findByName(String name) throws ApplicationException {
        log.debug("RoleModel.findByName() started with Name: " + name);
        Connection conn = null;
        RoleBean bean = null;
        StringBuffer sql = new StringBuffer("select * from st_role where name = ?");
        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new RoleBean();
                bean.setId(rs.getLong(1));
                bean.setName(rs.getString(2));
                bean.setDescription(rs.getString(3));
                bean.setCreatedBy(rs.getString(4));
                bean.setModifiedBy(rs.getString(5));
                bean.setCreatedDatetime(rs.getTimestamp(6));
                bean.setModifiedDatetime(rs.getTimestamp(7));
            }
            rs.close();
            pstmt.close();
            log.info("Role found with Name: " + name);
        } catch (Exception e) {
            log.error("Exception in finding Role by Name", e);
            throw new ApplicationException("Exception : Exception in getting User by name");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("RoleModel.findByName() ended");
        return bean;
    }

    /**
     * Returns all roles.
     */
    public List<RoleBean> list() throws ApplicationException {
        log.debug("RoleModel.list() started");
        List<RoleBean> list = search(null, 0, 0);
        log.debug("RoleModel.list() ended with " + list.size() + " records");
        return list;
    }

    /**
     * Searches roles based on provided filter bean and supports pagination.
     */
    public List<RoleBean> search(RoleBean bean, int pageNo, int pageSize) throws ApplicationException {
        log.debug("RoleModel.search() started");
        StringBuffer sql = new StringBuffer("select * from st_role where 1=1");
        if (bean != null) {
            if (bean.getId() > 0) {
                sql.append(" and id = " + bean.getId());
            }
            if (bean.getName() != null && bean.getName().length() > 0) {
                sql.append(" and name like '%" + bean.getName() + "%'");
            }
            if (bean.getDescription() != null && bean.getDescription().length() > 0) {
                sql.append(" and description like '%" + bean.getDescription() + "%'");
            }
        }
        if (pageSize > 0) {
            pageNo = (pageNo - 1) * pageSize;
            sql.append(" limit " + pageNo + ", " + pageSize);
        }
        Connection conn = null;
        ArrayList<RoleBean> list = new ArrayList<RoleBean>();
        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bean = new RoleBean();
                bean.setId(rs.getLong(1));
                bean.setName(rs.getString(2));
                bean.setDescription(rs.getString(3));
                bean.setCreatedBy(rs.getString(4));
                bean.setModifiedBy(rs.getString(5));
                bean.setCreatedDatetime(rs.getTimestamp(6));
                bean.setModifiedDatetime(rs.getTimestamp(7));
                list.add(bean);
            }
            rs.close();
            pstmt.close();
            log.info("RoleModel.search() found " + list.size() + " records");
        } catch (Exception e) {
            log.error("Exception in searching Role", e);
            throw new ApplicationException("Exception : Exception in search Role");
        } finally {
            JDBCDataSource.closeConnection(conn);
        }
        log.debug("RoleModel.search() ended");
        return list;
    }
}
