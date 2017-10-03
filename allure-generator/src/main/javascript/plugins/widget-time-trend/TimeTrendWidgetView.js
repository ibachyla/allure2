import './styles.scss';
import template from './TimeTrendWidgetView.hbs';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators/index';
import TimeTrendChartView from '../../components/graph-time-trend-chart/TimeTrendChartView';

@regions({
    chart: '.time-trend__chart'
})
@className('time-trend')
class TimeTrendWidgetView extends View {
    template = template;

    initialize() {
        this.model = this.model.getWidgetData('time-trend');
    }

    onRender() {
        this.showChildView('chart', new TimeTrendChartView({
            items: this.model.get('items')
        }));
    }
}

export default TimeTrendWidgetView;
