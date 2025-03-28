/**********************************************
Workshop #6
Course: APD545
Last Name: Chu
First Name: Sin Kau
ID: 155131220
Section: NDD
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature Sin Kau Chu
Date: 27-Mar-2025
**********************************************/
package com.groceryapp;
import com.groceryapp.model.JdbcDao;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        //establishing database tables if they don't exist
        JdbcDao.initializeDatabase();
        
        //loading login screen first
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groceryapp/view/Login.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Grocery Store Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}