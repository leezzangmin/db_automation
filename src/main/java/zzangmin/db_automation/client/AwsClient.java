package zzangmin.db_automation.client;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;

@Component
public class AwsClient {

    private static final Region defaultRegion = Region.AP_NORTHEAST_2;
    private final AwsCredentialsProvider awsCredentialsProvider = DefaultCredentialsProvider.create();

    @Bean
    public RdsClient getRdsClient() {
        RdsClient rdsClient = RdsClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(defaultRegion)
                .build();

        return rdsClient;
    }

//    @Bean
//    public SsmClient getSsmClient() {
//    }

}
