package io.qameta.allure.history;

import io.qameta.allure.core.Configuration;

import java.util.List;

public interface IHistoryTrendManager {

    List<HistoryTrendItem> load(Configuration configuration) throws IoHistoryTrendException;

    void save(Configuration configuration, List<HistoryTrendItem> historyTrendItems) throws IoHistoryTrendException;
}
