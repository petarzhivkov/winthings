package org.openhab.binding.withings;

import org.openhab.core.binding.BindingProvider;

public interface WithingsBindingProvider extends BindingProvider {

    WithingsBindingConfig getItemConfig(String itemName);

}
