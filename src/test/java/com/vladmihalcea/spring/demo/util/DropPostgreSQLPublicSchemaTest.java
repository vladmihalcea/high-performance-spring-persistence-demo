package com.vladmihalcea.spring.demo.util;

import com.vladmihalcea.spring.demo.config.SpringPersistenceDemoConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Vlad Mihalcea
 */
@RunWith(SpringRunner.class)
@Import(SpringPersistenceDemoConfiguration.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisabledInNativeImage
@DisabledInAotMode
public class DropPostgreSQLPublicSchemaTest {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private boolean drop = true;

    @Test
    public void test() {
        if (drop) {
            try {
                transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
                    Session session = entityManager.unwrap(Session.class);
                    session.doWork(connection -> {
                        ScriptUtils.executeSqlScript(connection,
                            new EncodedResource(
                                new ClassPathResource("scripts/drop/drop.sql")
                            ),
                            true, true,
                            ScriptUtils.DEFAULT_COMMENT_PREFIX,
                            ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
                            ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER,
                            ScriptUtils.DEFAULT_COMMENT_PREFIX
                        );
                    });
                    return null;
                });
            } catch (TransactionException e) {
                LOGGER.error("Failure", e);
            }
        }
    }
}
