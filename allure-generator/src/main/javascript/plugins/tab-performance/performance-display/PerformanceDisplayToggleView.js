import {View} from 'backbone.marionette';
import {on} from '../../../decorators';
import settings from '../../../util/settings';
import template from './PerformanceDisplayToggleView.hbs';

class PerformanceDisplayToggleView extends View {
    template = template;

    serializeData() {
        return {
            performanceShowPercentage: settings.get('performanceShowPercentage')
        };
    }

    @on('click .button')
    onCheckChange(e) {
        const el = this.$(e.currentTarget);
        el.toggleClass('button_active');
        const checked = el.hasClass('button_active');
        settings.save('performanceShowPercentage', checked);
    }
}

export default PerformanceDisplayToggleView;
