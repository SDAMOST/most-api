package pl.salezjanie.most;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MostApplication {

	public static void main(String[] args) {
		SpringApplication.run(MostApplication.class, args);
	}

}
