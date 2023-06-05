//package zzangmin.db_automation.config;
//
//import javax.sql.DataSource;
//
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class DataSourceConfig {
//
//    @Bean
//    public DataSource dynamicDataSource() {
//        DynamicRoutingDataSource dynamicDataSource = new DynamicRoutingDataSource();
//
//        // Loop through the database configurations and create DataSource for each database
//        for (DatabaseConfig config : properties.getDatabases()) {
//            DataSource dataSource = DataSourceBuilder.create()
//                    .driverClassName(config.getDriverClassName())
//                    .url(config.getUrl())
//                    .username(config.getUsername())
//                    .password(config.getPassword())
//                    .build();
//
//            dynamicDataSource.addDataSource(config.getDatabaseName(), dataSource);
//        }
//        return dynamicDataSource;
//    }
//}