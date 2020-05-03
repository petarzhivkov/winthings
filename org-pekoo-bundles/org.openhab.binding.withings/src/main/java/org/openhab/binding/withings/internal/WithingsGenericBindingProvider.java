package org.openhab.binding.withings.internal;

import org.openhab.binding.withings.WithingsBindingConfig;
import org.openhab.binding.withings.WithingsBindingProvider;
import org.openhab.binding.withings.internal.model.MeasureType;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;

public class WithingsGenericBindingProvider extends AbstractGenericBindingProvider implements WithingsBindingProvider {

 	@Override
    public String getBindingType() {
        return "withings";
    }

    @Override
    public WithingsBindingConfig getItemConfig(String itemName) {
        return (WithingsBindingConfig) bindingConfigs.get(itemName);
    }

    @Override
    public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
        super.processBindingConfiguration(context, item, bindingConfig);

        String[] configElements = bindingConfig.split(":");

        String accountId = null;
        MeasureType measureType = null;

        if (configElements.length == 1) {
            measureType = MeasureType.valueOf(configElements[0].toUpperCase());
        } else if (configElements.length == 2) {
            accountId = configElements[0];
            measureType = MeasureType.valueOf(configElements[1].toUpperCase());
        } else {
            throw new BindingConfigParseException("Unknown Binding configuration '{}'. The Binding "+ "configuration should consists of either one or two elements.");
        }

        if (measureType == null) {
            throw new BindingConfigParseException("Could not convert string '" + bindingConfig + "' to according measure type.");
        }

        WithingsBindingConfig config = new WithingsBindingConfig(accountId, measureType);

        addBindingConfig(item, config);
    }

    @Override
    public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
        if (!(item instanceof NumberItem)) {
            throw new BindingConfigParseException(getConfigParseExcpetionMessage(item));
        }
    }

    private String getConfigParseExcpetionMessage(Item item) {
        return "item '" + item.getName() + "' is of type '" + item.getClass().getSimpleName() + "', only NumberItems are allowed - please check your *.items configuration";
    }
}