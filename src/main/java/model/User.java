package model;

// A Java Record: holds the data of one row in the users table.
public record User(int id, String username, String password, String roles, String status) {
}
