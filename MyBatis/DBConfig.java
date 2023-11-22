package com.rocketsoftware.common.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;

@Configuration
public class DBConfig {

    private static final Logger logger = LoggerFactory.getLogger(DBConfig.class);

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driverClassName}")
    private String driverClassName;

    @Value("${spring.datasource.validationQuery}")
    private String validationQuery;

    // LME
    @Value("${spring.datasource.rdoe.url}")
    private String dbUrlForLME;

    @Value("${spring.datasource.rdoe.username}")
    private String usernameForLME;

    @Value("${spring.datasource.rdoe.password}")
    private String passwordForLME;

    @Value("${spring.datasource.rdoe.driverClassName}")
    private String driverClassNameForLME;

    @Value("${spring.datasource.rdoe.validationQuery}")
    private String validationQueryForLME;

    // SS
    @Value("${spring.datasource.ss.url}")
    private String dbUrlForSS;

    @Value("${spring.datasource.ss.username}")
    private String usernameForSS;

    @Value("${spring.datasource.ss.password}")
    private String passwordForSS;

    @Value("${spring.datasource.ss.driverClassName}")
    private String driverClassNameForSS;

    @Value("${spring.datasource.ss.validationQuery}")
    private String validationQueryForSS;

    // LMI
    @Value("${spring.datasource.rdoi.url}")
    private String dbUrlForLMI;

    @Value("${spring.datasource.rdoi.username}")
    private String usernameForLMI;

    @Value("${spring.datasource.rdoi.password}")
    private String passwordForLMI;

    @Value("${spring.datasource.rdoi.driverClassName}")
    private String driverClassNameForLMI;

    @Value("${spring.datasource.rdoi.validationQuery}")
    private String validationQueryForLMI;

    @Value("${spring.datasource.initialSize}")
    private Integer initialSize;

    @Value("${spring.datasource.minIdle}")
    private int minIdle;

    @Value("${spring.datasource.maxActive}")
    private int maxActive;

    @Value("${spring.datasource.maxWait}")
    private int maxWait;

    @Value("${spring.datasource.timeBetweenEvictionRunsMillis}")
    private int timeBetweenEvictionRunsMillis;

    @Value("${spring.datasource.minEvictableIdleTimeMillis}")
    private int minEvictableIdleTimeMillis;

    @Value("${spring.datasource.testWhileIdle}")
    private boolean testWhileIdle;

    @Value("${spring.datasource.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${spring.datasource.testOnReturn}")
    private boolean testOnReturn;
    
    @Value("${spring.datasource.poolPreparedStatements}")  
    private boolean poolPreparedStatements; 
    @Value("${spring.datasource.maxPoolPreparedStatementPerConnectionSize}")  
    private int maxPoolPreparedStatementPerConnectionSize;  
    @Value("${spring.datasource.filters}")  
    private String filters;    
    @Value("${spring.datasource.connectionProperties}")  
    private String connectionProperties;

    @Value("${spring.datasource.timeBetweenConnectErrorMillis}")  
    private int timeBetweenConnectErrorMillis;
    @Value("${spring.datasource.failFast}")  
    private boolean failFast;

    
    private void setPoolPramaters(DruidDataSource datasource) {
        datasource.setInitialSize(initialSize);  
        datasource.setMinIdle(minIdle);  
        datasource.setMaxActive(maxActive);  
        datasource.setMaxWait(maxWait);  
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);  
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);  
        datasource.setTestWhileIdle(testWhileIdle);  
        datasource.setTestOnBorrow(testOnBorrow);  
        datasource.setTestOnReturn(testOnReturn);  
        datasource.setPoolPreparedStatements(poolPreparedStatements);  
        datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        
        // set retry interval time.
        datasource.setTimeBetweenConnectErrorMillis(timeBetweenConnectErrorMillis);
        datasource.setFailFast(failFast);
        try {  
            datasource.setFilters(filters);  
        } catch (SQLException e) {  
            logger.warn("Failed to initialize Datasource filters");
        }  
        datasource.setConnectionProperties(this.connectionProperties);
    }

    public DataSource dataSource(DBEnv env) {
        DruidDataSource datasource = new DruidDataSource();
        switch (env) {
            case LME:
                datasource.setUrl(dbUrlForLME);
                datasource.setUsername(usernameForLME);
                datasource.setPassword(passwordForLME);
                datasource.setDriverClassName(driverClassNameForLME);
                datasource.setValidationQuery(validationQueryForLME);
                break;
            case SS:
                datasource.setUrl(dbUrlForSS);
                datasource.setUsername(usernameForSS);
                datasource.setPassword(passwordForSS);
                datasource.setDriverClassName(driverClassNameForSS);
                datasource.setValidationQuery(validationQueryForSS);
                break;
            case LMI:
                datasource.setUrl(dbUrlForLMI);
                datasource.setUsername(usernameForLMI);
                datasource.setPassword(passwordForLMI);
                datasource.setDriverClassName(driverClassNameForLMI);
                datasource.setValidationQuery(validationQueryForLMI);
                break;
            case RDOE_CONFIG:
                datasource.setUrl(getConfigDatabase());
                datasource.setUsername(usernameForSS);
                datasource.setPassword(passwordForSS);
                datasource.setDriverClassName(driverClassNameForSS);
                datasource.setValidationQuery(validationQueryForSS);
                break;

            default:
                datasource.setUrl(this.dbUrl);
                datasource.setUsername(username);
                datasource.setPassword(password);
                datasource.setDriverClassName(driverClassName);
                datasource.setValidationQuery(validationQuery);
                break;
        }

        setPoolPramaters(datasource);
        logger.info("DataSource for {} initialized.", env);
        return datasource;
    }

    @Bean("dataSource")
    public DataSource dataSource() {
        return dataSource(DBEnv.DEFAULT);
    }

    @Bean("dataSourceForLME")
    public DataSource dataSourceForLME() {
        return dataSource(DBEnv.LME);
    }

    @Bean("dataSourceForSS")
    public DataSource dataSourceForSS() {
        return dataSource(DBEnv.SS);
    }

    @Bean("dataSourceForLMI")
    public DataSource dataSourceForLMI() {
        return dataSource(DBEnv.LMI);
    }

    @Bean("dataSourceForRDOeConfig")
    public DataSource dataSourceForRDOeConfig() {
        return dataSource(DBEnv.RDOE_CONFIG);
    }


    private String getConfigDatabase(){
        String dbUrlForRDOeConfig = dbUrlForSS.substring(0, dbUrlForSS.lastIndexOf("/"));
        dbUrlForRDOeConfig = dbUrlForRDOeConfig+"/aldoncfg";
        return dbUrlForRDOeConfig;
    }
}
