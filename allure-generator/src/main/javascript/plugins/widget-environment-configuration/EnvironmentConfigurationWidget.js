import {View} from 'backbone.marionette';
import {on} from '../../decorators';
import template from './EnvironmentConfigurationWidget.hbs';

const CATEGORY_LIMIT = 9;

export default class EnvironmentConfigurationWidget extends View {
    template = template;

    initialize() {
        this.model = this.model.getWidgetData('environment');
        this.model.get('items').forEach(function(category) {
            category.categoryLimit = CATEGORY_LIMIT;
        });
    }

    serializeData() {
        return {
            items: this.model.get('items')
        };
    }

    @on('click .environment-widget__expand')
    onExpandParametersClick(e) {
        const el = this.$(e.currentTarget);
        const categoryName = el.data('category');
        var category = this.model.get('items').filter(function(category) {
            return category.categoryName === categoryName;
        })[0];
        category.categoryLimit = category.environmentItems.length;
        this.render();
    }
}

