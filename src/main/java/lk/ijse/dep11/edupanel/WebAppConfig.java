package lk.ijse.dep11.edupanel;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@EnableWebMvc
@ComponentScan
public class WebAppConfig {

    @Bean
    public Bucket defaultBucket() throws IOException {
        InputStream serviceAccount = getClass().getResourceAsStream("/dep11-edupanel-sk-firebase-adminsdk-tj9sk-df1560bbb5.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket("dep11-edupanel-sk.appspot.com")
                .build();

        FirebaseApp.initializeApp(options);
        return StorageClient.getInstance().bucket();

    }

//    @Bean
//    public CommonsMultipartResolver multipartResolver(){
//        return new CommonsMultipartResolver();
//    }
    @Bean
    public StandardServletMultipartResolver multipartResolver(){
        return new StandardServletMultipartResolver();
    }
}



