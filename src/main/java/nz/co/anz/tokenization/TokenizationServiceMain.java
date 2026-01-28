
package nz.co.anz.tokenization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Entry point for the Tokenization Micro-service.
 */
@EnableCaching
@SpringBootApplication(scanBasePackageClasses = { TokenizationServiceMain.class })
public class TokenizationServiceMain extends SpringBootServletInitializer
{
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application)
    {
        return application.sources(TokenizationServiceMain.class);
    }

    /**
     * Entry point for the micro-service.
     *
     * @param args  Provided command line arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(TokenizationServiceMain.class, args);
    }
}
