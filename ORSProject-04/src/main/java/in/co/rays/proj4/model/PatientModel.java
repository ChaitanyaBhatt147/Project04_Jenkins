package in.co.rays.proj4.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import in.co.rays.proj4.bean.PatientBean;
import in.co.rays.proj4.exception.ApplicationException;
import in.co.rays.proj4.exception.DatabaseException;
import in.co.rays.proj4.exception.DuplicateRecordException;
import in.co.rays.proj4.util.JDBCDataSource;

import org.apache.log4j.Logger;

public class PatientModel {

    private static Logger log = Logger.getLogger(PatientModel.class);

    public static Integer nextPk() throws DatabaseException {
        log.debug("Entering nextPk()");
        Connection conn = null;
        int pk = 0;

        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("SELECT MAX(id) FROM st_patient");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                pk = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            log.debug("Next PK retrieved: " + (pk + 1));
        } catch (Exception e) {
            log.error("DatabaseException in nextPk()", e);
            throw new DatabaseException("Exception in getting PK");
        } finally {
            JDBCDataSource.closeConnection(conn);
            log.debug("Connection closed in nextPk()");
        }

        return pk + 1;
    }

    public void add(PatientBean bean) throws ApplicationException, DuplicateRecordException {
        log.debug("Entering add() with PatientBean: " + bean);
        Connection conn = null;

        PatientBean existing = findByName(bean.getName());
        if (existing != null && existing.getId() != bean.getId()) {
            log.warn("Duplicate patient name detected: " + bean.getName());
            throw new DuplicateRecordException("Patient Name already exists");
        }

        try {
            conn = JDBCDataSource.getConnection();
            int pk = nextPk();
            conn.setAutoCommit(false);

            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO st_patient VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            pstmt.setLong(1, pk);
            pstmt.setString(2, bean.getName());
            pstmt.setDate(3, new java.sql.Date(bean.getDateOfVisit().getTime()));
            pstmt.setString(4, bean.getMobile());
            pstmt.setString(5, bean.getDisease());
            pstmt.setString(6, bean.getCreatedBy());
            pstmt.setString(7, bean.getModifiedBy());
            pstmt.setTimestamp(8, bean.getCreatedDatetime());
            pstmt.setTimestamp(9, bean.getModifiedDatetime());

            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("Patient added successfully with PK: " + pk);
        } catch (Exception e) {
            log.error("Exception in add()", e);
            try {
                if (conn != null) {
                    conn.rollback();
                    log.debug("Transaction rollback in add()");
                }
            } catch (Exception ex) {
                log.error("Rollback failed in add()", ex);
                throw new ApplicationException("Add rollback exception: " + ex.getMessage());
            }
            throw new ApplicationException("Exception in adding Patient");
        } finally {
            JDBCDataSource.closeConnection(conn);
            log.debug("Connection closed in add()");
        }
    }

    public void update(PatientBean bean) throws ApplicationException, DuplicateRecordException {
        log.debug("Entering update() with PatientBean: " + bean);
        Connection conn = null;

        PatientBean existing = findByName(bean.getName());
        if (existing != null && existing.getId() != bean.getId()) {
            log.warn("Duplicate patient name detected during update: " + bean.getName());
            throw new DuplicateRecordException("Patient Name already exists");
        }

        try {
            conn = JDBCDataSource.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE st_patient SET name=?, dateofvisit=?, mobileNo=?, disease=?, "
                            + "created_by=?, modified_by=?, created_datetime=?, modified_datetime=? WHERE id=?");
            pstmt.setString(1, bean.getName());
            pstmt.setDate(2, new java.sql.Date(bean.getDateOfVisit().getTime()));
            pstmt.setString(3, bean.getMobile());
            pstmt.setString(4, bean.getDisease());
            pstmt.setString(5, bean.getCreatedBy());
            pstmt.setString(6, bean.getModifiedBy());
            pstmt.setTimestamp(7, bean.getCreatedDatetime());
            pstmt.setTimestamp(8, bean.getModifiedDatetime());
            pstmt.setLong(9, bean.getId());

            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("Patient updated successfully with ID: " + bean.getId());
        } catch (Exception e) {
            log.error("Exception in update()", e);
            try {
                if (conn != null) {
                    conn.rollback();
                    log.debug("Transaction rollback in update()");
                }
            } catch (Exception ex) {
                log.error("Rollback failed in update()", ex);
                throw new ApplicationException("Update rollback exception: " + ex.getMessage());
            }
            throw new ApplicationException("Exception in updating Patient");
        } finally {
            JDBCDataSource.closeConnection(conn);
            log.debug("Connection closed in update()");
        }
    }

    public void delete(long id) throws ApplicationException {
        log.debug("Entering delete() for ID: " + id);
        Connection conn = null;
        try {
            conn = JDBCDataSource.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM st_patient WHERE id=?");
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            conn.commit();
            pstmt.close();
            log.info("Patient deleted successfully with ID: " + id);
        } catch (Exception e) {
            log.error("Exception in delete()", e);
            try {
                if (conn != null) {
                    conn.rollback();
                    log.debug("Transaction rollback in delete()");
                }
            } catch (Exception ex) {
                log.error("Rollback failed in delete()", ex);
                throw new ApplicationException("Delete rollback exception: " + ex.getMessage());
            }
            throw new ApplicationException("Exception in deleting Patient");
        } finally {
            JDBCDataSource.closeConnection(conn);
            log.debug("Connection closed in delete()");
        }
    }

    public PatientBean findByPk(long id) throws ApplicationException {
        log.debug("Entering findByPk() with ID: " + id);
        PatientBean bean = null;
        Connection conn = null;
        String sql = "SELECT * FROM st_patient WHERE id=?";

        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bean = mapResultSetToBean(rs);
            }
            rs.close();
            pstmt.close();
            log.debug("findByPk() result: " + bean);
        } catch (Exception e) {
            log.error("Exception in findByPk()", e);
            throw new ApplicationException("Exception in getting Patient by PK");
        } finally {
            JDBCDataSource.closeConnection(conn);
            log.debug("Connection closed in findByPk()");
        }

        return bean;
    }

    public PatientBean findByName(String name) throws ApplicationException {
        log.debug("Entering findByName() with name: " + name);
        PatientBean bean = null;
        Connection conn = null;
        String sql = "SELECT * FROM st_patient WHERE name=?";

        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bean = mapResultSetToBean(rs);
            }
            rs.close();
            pstmt.close();
            log.debug("findByName() result: " + bean);
        } catch (Exception e) {
            log.error("Exception in findByName()", e);
            throw new ApplicationException("Exception in getting Patient by Name");
        } finally {
            JDBCDataSource.closeConnection(conn);
            log.debug("Connection closed in findByName()");
        }

        return bean;
    }

    public List<PatientBean> list() throws ApplicationException {
        log.debug("Entering list()");
        List<PatientBean> list = search(null, 0, 0);
        log.debug("list() returned " + list.size() + " records");
        return list;
    }

    public List<PatientBean> search(PatientBean bean, int pageNo, int pageSize) throws ApplicationException {
        log.debug("Entering search() with PatientBean: " + bean + ", pageNo: " + pageNo + ", pageSize: " + pageSize);
        Connection conn = null;
        StringBuilder sql = new StringBuilder("SELECT * FROM st_patient WHERE 1=1 ");

        if (bean != null) {
            if( bean.getId() > 0) {
                sql.append(" AND id=").append(bean.getId());
            }
            if (bean.getName() != null && !bean.getName().isEmpty()) {
                sql.append(" AND name LIKE '").append(bean.getName()).append("%'");
            }
            if (bean.getDateOfVisit() != null) {
                sql.append(" AND date_of_visit LIKE '")
                        .append(new java.sql.Date(bean.getDateOfVisit().getTime())).append("%'");
            }
            if (bean.getDisease() != null && !bean.getDisease().isEmpty()) {
                sql.append(" AND disease LIKE '").append(bean.getDisease()).append("%'");
            }
        }

        if (pageSize > 0) {
            pageNo = (pageNo - 1) * pageSize;
            sql.append(" LIMIT ").append(pageNo).append(",").append(pageSize);
        }

        List<PatientBean> list = new ArrayList<>();

        try {
            conn = JDBCDataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToBean(rs));
            }
            rs.close();
            pstmt.close();
            log.debug("search() returned " + list.size() + " records");
        } catch (Exception e) {
            log.error("Exception in search()", e);
            throw new ApplicationException("Exception in searching Patient");
        } finally {
            JDBCDataSource.closeConnection(conn);
            log.debug("Connection closed in search()");
        }

        return list;
    }

    private PatientBean mapResultSetToBean(ResultSet rs) throws Exception {
        PatientBean bean = new PatientBean();
        bean.setId(rs.getLong(1));
        bean.setName(rs.getString(2));
        bean.setDateOfVisit(rs.getDate(3));
        bean.setMobile(rs.getString(4));
        bean.setDisease(rs.getString(5));
        bean.setCreatedBy(rs.getString(6));
        bean.setModifiedBy(rs.getString(7));
        bean.setCreatedDatetime(rs.getTimestamp(8));
        bean.setModifiedDatetime(rs.getTimestamp(9));
        return bean;
    }
}
