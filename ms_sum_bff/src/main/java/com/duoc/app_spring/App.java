package com.duoc.app_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        System.out.println("Iniciando aplicación...");
        SpringApplication.run(App.class, args);
        System.out.println("Aplicación iniciada correctamente en el puerto 8090");
    }
}