package io.qameta.allure.history;

import io.qameta.allure.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class DelegatingHistoryTrendManager implements IHistoryTrendManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingHistoryTrendManager.class);

    private String activeManager;
    private Map<String, IHistoryTrendManager> managers;

    @Override
    public List<HistoryTrendItem> load(final Configuration configuration) throws IoHistoryTrendException {
        return getActiveManager().load(configuration);
    }

    @Override
    public void save(final Configuration configuration, final List<HistoryTrendItem> historyTrendItems)
            throws IoHistoryTrendException {
        getActiveManager().save(configuration, historyTrendItems);
    }

    private IHistoryTrendManager getActiveManager() {
        IHistoryTrendManager manager = managers.get(activeManager);
        if (manager == null) {
            LOGGER.error(
                    "History trend is disabled. Reason: storage mode not recognized. Actual: {}, Expected one of: {}",
                    activeManager, managers.keySet());
            manager = new NullHistoryTrendManager();
        }
        return manager;
    }

    public void setActiveManager(final String activeManager) {
        this.activeManager = activeManager;
    }

    public void setManagers(final Map<String, IHistoryTrendManager> managers) {
        this.managers = managers;
    }
}
