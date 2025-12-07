package ContractGuard.ContractGuard.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ContractGuard API")
                .description("API for managing API contracts, detecting breaking changes, and validating compliance")
                .version("1.0.0")
                .contact(new Contact()
                    .name("ContractGuard Team")
                    .url("https://contractguard.dev")
                    .email("support@contractguard.dev"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")));
    }
}

