package zzangmin.db_automation.client;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.profiles.Profile;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.pi.PiClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.nio.file.Paths;
import java.util.*;


@Component
public class AwsClient {

    private static final Region defaultRegion = Region.AP_NORTHEAST_2;
    private final ProfileFile profiles = ProfileFile.builder()
        .content(Paths.get(System.getProperty("user.home"), ".aws", "credentials"))
        .type(ProfileFile.Type.CREDENTIALS)
        .build();

    private final Map<String, RdsClient> accountIdRdsClients = new HashMap<>();
    private final Map<String, SecretsManagerClient> accountIdSecretsManagerClients = new HashMap<>();
    private final Map<String, CloudWatchClient> accountIdCloudWatchClients = new HashMap<>();
    private final Map<String, PiClient> accountIdPiClients = new HashMap<>();

    @PostConstruct
    public void initAwsClients() {
        Map<String, Profile> profileMap = profiles.profiles();

        for (String profileName : profileMap.keySet()) {
            AwsCredentialsProvider credentialsProvider = ProfileCredentialsProvider.builder()
                    .profileFile(profiles)
                    .profileName(profileName)
                    .build();

            String accountId = getAwsAccountId(credentialsProvider);

            accountIdRdsClients.put(accountId, RdsClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .region(defaultRegion)
                    .build());

            accountIdSecretsManagerClients.put(accountId, SecretsManagerClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .region(defaultRegion)
                    .build());

            accountIdCloudWatchClients.put(accountId, CloudWatchClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .region(defaultRegion)
                    .build());

            accountIdPiClients.put(accountId, PiClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .region(defaultRegion)
                    .build());
        }
    }

    public Optional<RdsClient> getRdsClient(String accountId) {
        return Optional.ofNullable(accountIdRdsClients.get(accountId));
    }

    public Optional<SecretsManagerClient> getSecretsManagerClient(String accountId) {
        return Optional.ofNullable(accountIdSecretsManagerClients.get(accountId));
    }

    public Optional<CloudWatchClient> getCloudWatchClient(String accountId) {
        return Optional.ofNullable(accountIdCloudWatchClients.get(accountId));
    }

    public Optional<PiClient> getPiClient(String accountId) {
        return Optional.ofNullable(accountIdPiClients.get(accountId));
    }

    public Map<String, RdsClient> findAllRdsClients() {
        return new HashMap<String, RdsClient>(accountIdRdsClients);
    }

    public Map<String, SecretsManagerClient> findAllSecretManagerClients() {
        return new HashMap<String, SecretsManagerClient>(accountIdSecretsManagerClients);
    }

    public Map<String, CloudWatchClient> findAllCloudWatchClients() {
        return new HashMap<String, CloudWatchClient>(accountIdCloudWatchClients);

    }

    public Map<String, PiClient> findAllPerformanceInsightClients() {
        return new HashMap<String, PiClient>(accountIdPiClients);

    }

    private String getAwsAccountId(AwsCredentialsProvider credentialsProvider) {
        StsClient stsClient = StsClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.AWS_GLOBAL)
                .build();

        // https://docs.aws.amazon.com/cli/latest/reference/sts/get-caller-identity.html
        GetCallerIdentityResponse response = stsClient.getCallerIdentity(GetCallerIdentityRequest.builder().build());
        return response.account();
    }
}
