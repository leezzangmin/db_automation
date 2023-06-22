package zzangmin.db_automation.client;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.ssm.SsmClient;


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

    @Bean
    public SsmClient getSsmClient() {
        SsmClient ssmClient = SsmClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(defaultRegion)
                .build();

        return ssmClient;
    }

    @Bean
    public CloudWatchClient getCloudWatchClient() {
        CloudWatchClient cloudWatchClient = CloudWatchClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(defaultRegion)
                .build();

        return cloudWatchClient;
    }

}
