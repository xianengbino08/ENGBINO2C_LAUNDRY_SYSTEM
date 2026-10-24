package com.laundry.model;

// A Java Record: holds one row of the users table.
public record User(int userId, String username, String password,
                   String fullName, String role, String status) {
}
