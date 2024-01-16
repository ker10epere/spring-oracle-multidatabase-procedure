package com.withintegrity.multiprocedure;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootApplication
public class MultiProcedureApplication {
    /*
    REQUIRED DEPENDENCY
    https://mvnrepository.com/artifact/com.oracle.database.spring/oracle-spring-boot-starter-ucp/23.4.0

    <!-- https://mvnrepository.com/artifact/com.oracle.database.spring/oracle-spring-boot-starter-ucp -->
    <dependency>
        <groupId>com.oracle.database.spring</groupId>
        <artifactId>oracle-spring-boot-starter-ucp</artifactId>
        <version>23.4.0</version>
    </dependency>

     */

    // DATABASE 1
    @Bean
    DataSource ds() throws SQLException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();

        //set the connection properties on the data source.

        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL("jdbc:oracle:thin:@//localhost:1521/ORCL");
        pds.setUser("OISS");
        pds.setPassword("OISS");

        //Override any pool properties.
        pds.setInitialPoolSize(5);
        pds.setMinPoolSize(5);
        pds.setMaxPoolSize(10);
        pds.setMaxIdleTime(30000);
        pds.setInactiveConnectionTimeout(30000);
        return pds;
    }

    // DATABASE 2
    @Bean
    DataSource ds2() throws SQLException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();

        //set the connection properties on the data source.
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL("jdbc:oracle:thin:@//localhost:1521/ORCL");
        pds.setUser("OISS2");
        pds.setPassword("OISS2");

        //Override any pool properties.
        pds.setInitialPoolSize(5);
        pds.setMinPoolSize(5);
        pds.setMaxPoolSize(10);
        pds.setMaxIdleTime(30000);
        pds.setInactiveConnectionTimeout(30000);
        return pds;
    }

    public static void main(String[] args) {
        SpringApplication.run(MultiProcedureApplication.class, args);
    }

}
