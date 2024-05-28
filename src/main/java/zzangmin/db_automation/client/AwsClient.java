package zzangmin.db_automation.client;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.profiles.Profile;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.pi.PiClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
    public List<RdsClient> getRdsClients() {
        List<RdsClient> rdsClients = new ArrayList<>();

        ProfileFile profiles = ProfileFile.builder()
                .content(Paths.get(System.getProperty("user.home"), ".aws", "credentials"))
                .type(ProfileFile.Type.CREDENTIALS)
                .build();
        Map<String, Profile> profiles1 = profiles.profiles();

        for (String profileName : profiles1.keySet()) {

            AwsCredentialsProvider credentialsProvider = ProfileCredentialsProvider.builder()
                    .profileFile(profiles)
                    .profileName(profileName)
                    .build();

            RdsClient rdsClient = RdsClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .region(defaultRegion)
                    .build();

            rdsClients.add(rdsClient);
        }
        return rdsClients;
    }

    @Bean
    public SecretsManagerClient getSecretManagerClient() {
        SecretsManagerClient secretsManagerClient = SecretsManagerClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(defaultRegion)
                .build();

        return secretsManagerClient;
    }

    @Bean
    public CloudWatchClient getCloudWatchClient() {
        CloudWatchClient cloudWatchClient = CloudWatchClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(defaultRegion)
                .build();

        return cloudWatchClient;
    }

    @Bean
    public PiClient getPerformanceInsightClient() {
        PiClient performanceInsightClient = PiClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(defaultRegion)
                .build();

        return performanceInsightClient;
    }

}
