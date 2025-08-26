package com.portfolio;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
  info = @Info(
    title = "Portfolio API",
    version = "v1",
    description = "API de portfólio de projetos - Code Group"
  )
)
@SpringBootApplication
public class PortfolioApplication {
  public static void main(String[] args) {
    SpringApplication.run(PortfolioApplication.class, args);
  }
}
