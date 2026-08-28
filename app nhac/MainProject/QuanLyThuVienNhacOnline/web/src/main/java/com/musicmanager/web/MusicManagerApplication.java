package com.musicmanager.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MusicManagerApplication
{
	public static void main(String[] args){
		SpringApplication.run(MusicManagerApplication.class, args);

        System.out.println("Kết nối thành công, truy cập: http://localhost:8080/");
	}
}
