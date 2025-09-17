package com.vladmihalcea.spring.demo.config;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.vladmihalcea.spring.demo.domain.views.PostView;
import io.hypersistence.utils.common.ReflectionUtils;
import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 *
 * @author Vlad Mihalcea
 */
@Configuration
@ComponentScan(
    basePackages = {
        "com.vladmihalcea.spring.demo",
    }
)
@EnableJpaRepositories(
    basePackages = "com.vladmihalcea.spring.demo.repository",
    repositoryBaseClass = BaseJpaRepositoryImpl.class
)
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class SpringPersistenceDemoConfiguration {

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public CriteriaBuilderFactory criteriaBuilderFactory(EntityManagerFactory entityManagerFactory) {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        return config.createCriteriaBuilderFactory(entityManagerFactory);
    }

    @Bean
    public EntityViewConfiguration entityViewConfiguration(CriteriaBuilderFactory criteriaBuilderFactory) {
        EntityViewConfiguration entityViewConfiguration = EntityViews.createDefaultConfiguration();
        for(Class entityViewClass : ReflectionUtils.getClassesByPackage(PostView.class.getPackageName())) {
            entityViewConfiguration.addEntityView(entityViewClass);
        }
        return entityViewConfiguration;
    }

    @Bean
    public EntityViewManager entityViewManager(CriteriaBuilderFactory cbf, EntityViewConfiguration entityViewConfiguration) {
        return entityViewConfiguration.createEntityViewManager(cbf);
    }
}
