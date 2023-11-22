package com.rocketsoftware.rdop.config;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.pagehelper.PageInterceptor;
import com.rocketsoftware.common.config.DBEnv;

@Configuration
@MapperScans({
    @MapperScan(basePackages="com.rocketsoftware.rdop.dao.rdop", sqlSessionFactoryRef="sqlSessionFactory"),
    @MapperScan(basePackages="com.rocketsoftware.rdop.dao.rdoe", sqlSessionFactoryRef="sqlSessionFactoryForLME"), 
    @MapperScan(basePackages="com.rocketsoftware.rdop.dao.rdoi", sqlSessionFactoryRef="sqlSessionFactoryForLMI")
    })
public class MyBatisConfig {
    
    @Autowired
    private DataSource dataSource;
    @Autowired
    private DataSource dataSourceForLME;
    @Autowired
    private DataSource dataSourceForLMI;
    @Autowired
    private DatabaseInitializer databaseInitializer;
    
    public SqlSessionFactory sqlSessionFactoryBean(DBEnv env) {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        //https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md
        Properties properties = new Properties();
        properties.setProperty("reasonable", "true");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("returnPageInfo", "check");
        properties.setProperty("params", "count=countSql");

        PageInterceptor interceptor = new PageInterceptor();
        interceptor.setProperties(properties);
        String mapperScanPath = null;
        switch (env) {
        case LME:
            bean.setDataSource(this.dataSourceForLME);
            mapperScanPath = "mapper/rdoe/**/*Mapper.xml";
            break;
        case LMI:
            bean.setDataSource(this.dataSourceForLMI);
            mapperScanPath = "mapper/rdoi/**/*Mapper.xml";
            break;
        default:
            bean.setDataSource(this.dataSource);
            mapperScanPath = "mapper/rdop/**/*Mapper.xml";
            break;
        }
        bean.setPlugins(new Interceptor[] { interceptor });

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            bean.setMapperLocations(resolver.getResources(mapperScanPath));
            return bean.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactoryBean() {
        return sqlSessionFactoryBean(DBEnv.DEFAULT);
    }
    
    @Bean(name = "sqlSessionFactoryForLME")
    public SqlSessionFactory sqlSessionFactoryBeanForLME() {
        return sqlSessionFactoryBean(DBEnv.LME);
    }
    
    @Bean(name = "sqlSessionFactoryForLMI")
    public SqlSessionFactory sqlSessionFactoryBeanForLMI() {
        return sqlSessionFactoryBean(DBEnv.LMI);
    }

    @Bean("sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
    
    @Bean("sqlSessionTemplateForLME")
    public SqlSessionTemplate sqlSessionTemplateForLME(SqlSessionFactory sqlSessionFactoryForLME) {
        return new SqlSessionTemplate(sqlSessionFactoryForLME);
    }
    
    @Bean("sqlSessionTemplateForLMI")
    public SqlSessionTemplate sqlSessionTemplateForLMI(SqlSessionFactory sqlSessionFactoryForLMI) {
        return new SqlSessionTemplate(sqlSessionFactoryForLMI);
    }
    
    @Bean("jdbcTemplate")
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate temp = new JdbcTemplate(dataSource);
        return temp;
    }
    
    @Bean("jdbcTemplateForLME")
    public JdbcTemplate jdbcTemplateForLME() {
        JdbcTemplate temp = new JdbcTemplate(dataSourceForLME);
        return temp;
    }
    
    @Bean("jdbcTemplateForLMI")
    public JdbcTemplate jdbcTemplateForLMI() {
        JdbcTemplate temp = new JdbcTemplate(dataSourceForLMI);
        return temp;
    }
}
