package by.bsuir.linguaclient.configuration;

import jakarta.annotation.PostConstruct;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.spring.SpringFxWeaver;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.prefs.Preferences;

@Configuration
public class AppConfiguration {

    @Value("${app.folder.path}")
    private String appFolderPath;

    @PostConstruct
    public void init() throws IOException {
        File folder = new File(appFolderPath);
        folder.mkdirs();
    }

    @Bean
    public FxWeaver fxWeaver(ConfigurableApplicationContext applicationContext) {
        return new SpringFxWeaver(applicationContext);
    }

    @Bean
    public DataSource dataSource(@Value("${app.datasource.url}") String url,
                                 @Value("${app.datasource.username}") String username,
                                 @Value("${app.datasource.password}") String password,
                                 @Value("${app.datasource.driver-class-name}") String driverClassName) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    @Bean
    public Preferences preferences() {
        return Preferences.userRoot().node(getClass().getName());
    }
}
