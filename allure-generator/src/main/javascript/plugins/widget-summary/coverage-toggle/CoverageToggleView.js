import './styles.scss';
import {View} from 'backbone.marionette';
import {on} from '../../../decorators';
import settings from '../../../util/settings';
import template from './CoverageToggleView.hbs';

class CoverageToggleView extends View {
    template = template;

    serializeData() {
        const isCoverageChecked = settings.get('isCoverageChecked');
        return {
            active: isCoverageChecked
        };
    }

    @on('click .button')
    onCheckChange(e) {
        const el = this.$(e.currentTarget);
        el.toggleClass('button_active');
        const checked = el.hasClass('button_active');
        settings.save('isCoverageChecked', checked);
    }
}

export default CoverageToggleView;
