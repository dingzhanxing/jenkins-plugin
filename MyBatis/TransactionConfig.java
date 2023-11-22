package com.rocketsoftware.rdop.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

@Configuration
@Profile("!test")
public class TransactionConfig {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private DataSource dataSourceForLME;
    
    @Autowired
    private DataSource dataSourceForLMI;

    @Bean(value = "transactionManagerForLME")
    public TransactionManager transactionManagerForLME() {
        return new DataSourceTransactionManager(dataSourceForLME);
    }

    

    @Bean(value = "transactionManagerForLMI")
    public TransactionManager transactionManagerForLMI() {
        return new DataSourceTransactionManager(dataSourceForLMI);
    }

    @Bean(value = "transactionManager")
    public TransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}
