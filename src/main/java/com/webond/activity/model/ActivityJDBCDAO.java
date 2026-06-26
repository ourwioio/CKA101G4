package com.webond.activity.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityJDBCDAO implements ActivityDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/group_activity_db?serverTimezone=Asia/Taipei";
    private static final String USER = "root";
    private static final String PASSWORD = "123456"; 

    private static final String INSERT_STMT = 
        "INSERT INTO ACTIVITY (ACTIVITY_TYPE_ID, MEMBER_ID, ACTIVITY_TITLE, ACTIVITY_DESCRIPTION, ACTIVITY_PRICE, MIN_PARTICIPANTS, MAX_PARTICIPANTS, ATTENDEES_COUNT, REGISTRATION_STARTTIME, REGISTRATION_DEADLINE, ACTIVITY_STATUS, END_TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String GET_ALL_STMT = 
        "SELECT * FROM ACTIVITY ORDER BY ACTIVITY_ID DESC";
    private static final String GET_ONE_STMT = 
        "SELECT * FROM ACTIVITY WHERE ACTIVITY_ID = ?";
    private static final String UPDATE_STMT = 
        "UPDATE ACTIVITY SET ACTIVITY_TYPE_ID=?, MEMBER_ID=?, ACTIVITY_TITLE=?, ACTIVITY_DESCRIPTION=?, ACTIVITY_PRICE=?, MIN_PARTICIPANTS=?, MAX_PARTICIPANTS=?, ATTENDEES_COUNT=?, REGISTRATION_STARTTIME=?, REGISTRATION_DEADLINE=?, ACTIVITY_STATUS=?, END_TIME=? WHERE ACTIVITY_ID = ?";
    private static final String DELETE_STMT = 
        "DELETE FROM ACTIVITY WHERE ACTIVITY_ID = ?";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insert(ActivityVO activityVo) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(INSERT_STMT)) {
            
            pstmt.setInt(1, activityVo.getActivityTypeId());
            pstmt.setInt(2, activityVo.getMemberId());
            pstmt.setString(3, activityVo.getActivityTitle());
            pstmt.setString(4, activityVo.getActivityDescription());
            pstmt.setInt(5, activityVo.getActivityPrice());
            pstmt.setInt(6, activityVo.getMinParticipants());
            pstmt.setInt(7, activityVo.getMaxParticipants());
            pstmt.setInt(8, activityVo.getAttendeesCount());
            pstmt.setTimestamp(9, activityVo.getRegistrationStartTime());
            pstmt.setTimestamp(10, activityVo.getRegistrationDeadline());
            pstmt.setByte(11, activityVo.getActivityStatus());
            pstmt.setTimestamp(12, activityVo.getEndTime());

            pstmt.executeUpdate();
        } catch (SQLException se) {
            throw new RuntimeException("A database error occurred. " + se.getMessage());
        }
    }

    @Override
    public void update(ActivityVO activityVo) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(UPDATE_STMT)) {

            pstmt.setInt(1, activityVo.getActivityTypeId());
            pstmt.setInt(2, activityVo.getMemberId());
            pstmt.setString(3, activityVo.getActivityTitle());
            pstmt.setString(4, activityVo.getActivityDescription());
            pstmt.setInt(5, activityVo.getActivityPrice());
            pstmt.setInt(6, activityVo.getMinParticipants());
            pstmt.setInt(7, activityVo.getMaxParticipants());
            pstmt.setInt(8, activityVo.getAttendeesCount());
            pstmt.setTimestamp(9, activityVo.getRegistrationStartTime());
            pstmt.setTimestamp(10, activityVo.getRegistrationDeadline());
            pstmt.setByte(11, activityVo.getActivityStatus());
            pstmt.setTimestamp(12, activityVo.getEndTime());
            pstmt.setInt(13, activityVo.getActivityId());

            pstmt.executeUpdate();
        } catch (SQLException se) {
            throw new RuntimeException("A database error occurred. " + se.getMessage());
        }
    }

    @Override
    public void delete(Integer activityId) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(DELETE_STMT)) {

            pstmt.setInt(1, activityId);
            pstmt.executeUpdate();
        } catch (SQLException se) {
            throw new RuntimeException("A database error occurred. " + se.getMessage());
        }
    }

    @Override
    public ActivityVO findByPrimaryKey(Integer activityId) {
        ActivityVO activityVo = null;
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(GET_ONE_STMT)) {

            pstmt.setInt(1, activityId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    activityVo = new ActivityVO();
                    activityVo.setActivityId(rs.getInt("ACTIVITY_ID"));
                    activityVo.setActivityTypeId(rs.getInt("ACTIVITY_TYPE_ID"));
                    activityVo.setMemberId(rs.getInt("MEMBER_ID"));
                    activityVo.setActivityTitle(rs.getString("ACTIVITY_TITLE"));
                    activityVo.setActivityDescription(rs.getString("ACTIVITY_DESCRIPTION"));
                    activityVo.setActivityPrice(rs.getInt("ACTIVITY_PRICE"));
                    activityVo.setMinParticipants(rs.getInt("MIN_PARTICIPANTS"));
                    activityVo.setMaxParticipants(rs.getInt("MAX_PARTICIPANTS"));
                    activityVo.setAttendeesCount(rs.getInt("ATTENDEES_COUNT"));
                    activityVo.setRegistrationStartTime(rs.getTimestamp("REGISTRATION_STARTTIME"));
                    activityVo.setRegistrationDeadline(rs.getTimestamp("REGISTRATION_DEADLINE"));
                    activityVo.setActivityStatus(rs.getByte("ACTIVITY_STATUS"));
                    activityVo.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                    activityVo.setEndTime(rs.getTimestamp("END_TIME"));
                }
            }
        } catch (SQLException se) {
            throw new RuntimeException("A database error occurred. " + se.getMessage());
        }
        return activityVo;
    }

    @Override
    public List<ActivityVO> getAll() {
        List<ActivityVO> list = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(GET_ALL_STMT);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ActivityVO activityVo = new ActivityVO();
                activityVo.setActivityId(rs.getInt("ACTIVITY_ID"));
                activityVo.setActivityTypeId(rs.getInt("ACTIVITY_TYPE_ID"));
                activityVo.setMemberId(rs.getInt("MEMBER_ID"));
                activityVo.setActivityTitle(rs.getString("ACTIVITY_TITLE"));
                activityVo.setActivityDescription(rs.getString("ACTIVITY_DESCRIPTION"));
                activityVo.setActivityPrice(rs.getInt("ACTIVITY_PRICE"));
                activityVo.setMinParticipants(rs.getInt("MIN_PARTICIPANTS"));
                activityVo.setMaxParticipants(rs.getInt("MAX_PARTICIPANTS"));
                activityVo.setAttendeesCount(rs.getInt("ATTENDEES_COUNT"));
                activityVo.setRegistrationStartTime(rs.getTimestamp("REGISTRATION_STARTTIME"));
                activityVo.setRegistrationDeadline(rs.getTimestamp("REGISTRATION_DEADLINE"));
                activityVo.setActivityStatus(rs.getByte("ACTIVITY_STATUS"));
                activityVo.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                activityVo.setEndTime(rs.getTimestamp("END_TIME"));
                list.add(activityVo);
            }
        } catch (SQLException se) {
            throw new RuntimeException("A database error occurred. " + se.getMessage());
        }
        return list;
    }
}