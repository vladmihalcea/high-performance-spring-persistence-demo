package com.vladmihalcea.spring.demo;

import com.vladmihalcea.spring.demo.config.SpringPersistenceDemoConfiguration;
import io.hypersistence.utils.test.transaction.VoidCallable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import org.junit.Before;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@RunWith(SpringRunner.class)
@Import(SpringPersistenceDemoConfiguration.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisabledInNativeImage
@DisabledInAotMode
public abstract class BaseDemoTest {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	protected TransactionTemplate transactionTemplate;

	@PersistenceContext
	protected EntityManager entityManager;

	@PersistenceUnit
	protected EntityManagerFactory entityManagerFactory;

	@Autowired
	protected DataSource dataSource;

	@Before
	public void init() {
		executeStatement("TRUNCATE user_votes CASCADE");
		executeStatement("TRUNCATE post_comments CASCADE");
		executeStatement("TRUNCATE post_tags CASCADE");
		executeStatement("TRUNCATE tags CASCADE");
		executeStatement("TRUNCATE users CASCADE");
		executeStatement("TRUNCATE posts CASCADE");
	}

	protected void executeTransaction(VoidCallable callable) {
		try {
			transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
				callable.execute();
				return null;
			});
		} catch (TransactionException e) {
			LOGGER.error("Failure", e);
		}
	}

	protected void executeStatement(String sql) {
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement()) {
			boolean autoCommit = connection.getAutoCommit();
			try {
				if (!autoCommit) {
					connection.setAutoCommit(true);
				}
				statement.executeLargeUpdate(sql);
			} finally {
				if (!autoCommit) {
					connection.setAutoCommit(false);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Statement failed", e);
		}
	}

	protected void warmUpHibernateQueryPlanCache(VoidCallable function) {
		function.execute();
	}
}
