package io.qameta.allure.history;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.org.lidalia.slf4jtest.LoggingEvent.error;

import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import io.qameta.allure.core.Configuration;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;
import uk.org.lidalia.slf4jtest.TestLoggerFactoryResetRule;

public class DelegatingHistoryTrendManagerTest
{

    private static final String ACTIVE_MANAGER = "mocked";

    private static final TestLogger TEST_LOGGER = TestLoggerFactory.getTestLogger(DelegatingHistoryTrendManager.class);
    private static final String WRONG_MANAGER = "wrong";

    @Rule
    public TestLoggerFactoryResetRule testLoggerFactoryResetRule = new TestLoggerFactoryResetRule();

    private DelegatingHistoryTrendManager delegatingHistoryTrendManager = new DelegatingHistoryTrendManager();

    @Test
    public void testLoad() throws Exception {
        IHistoryTrendManager mockedHistoryTrendManager = mockHistoryTrendManager(ACTIVE_MANAGER);
        Configuration configuration = mock(Configuration.class);
        delegatingHistoryTrendManager.load(configuration);
        verify(mockedHistoryTrendManager).load(configuration);
    }

    @Test
    public void testLoadWrongManager() throws Exception {
        mockHistoryTrendManager(WRONG_MANAGER);
        delegatingHistoryTrendManager.load(mock(Configuration.class));
        assertLogger();
    }

    @Test
    public void testSave() throws Exception {
        IHistoryTrendManager mockedHistoryTrendManager = mockHistoryTrendManager(ACTIVE_MANAGER);
        Configuration configuration = mock(Configuration.class);
        List<HistoryTrendItem> items = Collections.emptyList();
        delegatingHistoryTrendManager.save(configuration, items);
        verify(mockedHistoryTrendManager).save(configuration, items);
    }

    @Test
    public void testSaveWrongManager() throws Exception {
        mockHistoryTrendManager(WRONG_MANAGER);
        delegatingHistoryTrendManager.save(mock(Configuration.class), Collections.emptyList());
        assertLogger();
    }

    private IHistoryTrendManager mockHistoryTrendManager(String activeManager) {
        IHistoryTrendManager mockedHistoryTrendManager = mock(IHistoryTrendManager.class);
        delegatingHistoryTrendManager.setManagers(Collections.singletonMap(ACTIVE_MANAGER, mockedHistoryTrendManager));
        delegatingHistoryTrendManager.setActiveManager(activeManager);
        return mockedHistoryTrendManager;
    }

    private void assertLogger() {
        assertThat(TEST_LOGGER.getLoggingEvents(), is(Collections.singletonList(error(
                "History trend is disabled. Reason: storage mode not recognized. Actual: {}, Expected one of: {}",
                WRONG_MANAGER, Collections.singleton(ACTIVE_MANAGER)))));
    }
}
