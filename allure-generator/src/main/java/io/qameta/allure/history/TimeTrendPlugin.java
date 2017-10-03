package io.qameta.allure.history;

import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.entity.GroupTime;

import java.util.List;

public class TimeTrendPlugin extends AbstractTrendPlugin<GroupTime> {

    public TimeTrendPlugin(final ITrendManager<GroupTime> trendManager) {
        super(trendManager);
    }

    @Override
    public String getName() {
        return "time-trend";
    }

    @Override
    protected GroupTime createCurrent(final List<LaunchResults> launchesResults) {
        GroupTime groupTime = new GroupTime();
        launchesResults.stream().flatMap(launchResults -> launchResults.getResults().stream())
                .forEach(groupTime::update);
        return groupTime;
    }
}
