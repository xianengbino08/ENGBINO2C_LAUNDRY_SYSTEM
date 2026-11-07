package com.laundry;

import com.laundry.dao.DatabaseInitializer;
import com.laundry.dao.UserDao;
import com.laundry.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();

        UserDao userDao = new UserDao();
        Scanner scanner = new Scanner(System.in);

        new Authentication(userDao, scanner).start();

        scanner.close();
    }
}
2
x