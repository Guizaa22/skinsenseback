package beauty_center.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Beauty Center API")
                .version("1.0.0")
                .description("Production-ready API for beauty center management system")
                .contact(new Contact()
                    .name("Development Team")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
            .getComponents()
            .addSecuritySchemes("Bearer Token",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT token from /auth/login endpoint"))
            .getOpenApi();
    }

}
