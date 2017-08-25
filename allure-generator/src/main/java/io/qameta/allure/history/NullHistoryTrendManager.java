package io.qameta.allure.history;

import io.qameta.allure.core.Configuration;

import java.util.Collections;
import java.util.List;

public class NullHistoryTrendManager implements IHistoryTrendManager {

    @Override
    public List<HistoryTrendItem> load(final Configuration configuration) throws IoHistoryTrendException {
        return Collections.emptyList();
    }

    @Override
    public void save(final Configuration configuration, final List<HistoryTrendItem> historyTrendItems)
            throws IoHistoryTrendException {
        //does nothing
    }
}
