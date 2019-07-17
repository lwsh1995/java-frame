package cloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

// C-Cross O-Origin R-Resource S-Sharing
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        //允许跨域
        config.setAllowCredentials(true);
        //设置源，如 www.a.com
        config.setAllowedOrigins(Arrays.asList("*"));
        //设置请求头，如 order/create
        config.setAllowedHeaders(Arrays.asList("*"));
        //设置请求方法，如 GET、POST
        config.setAllowedMethods(Arrays.asList("*"));
        //设置时间
        config.setMaxAge(300l);
        //对所有路径生效
        source.registerCorsConfiguration("/**",config);
        return new CorsFilter(source);
    }
}
