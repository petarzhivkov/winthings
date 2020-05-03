package org.openhab.binding.withings;

import org.openhab.binding.withings.internal.model.MeasureType;
import org.openhab.core.binding.BindingConfig;

public class WithingsBindingConfig implements BindingConfig {

    public String accountId;
    public MeasureType measureType;

    public WithingsBindingConfig(String accountId, MeasureType measureType) {
        this.accountId = accountId;
        this.measureType = measureType;
    }

    @Override
    public String toString() {
        return "WithingsBindingConfig [accountId=" + accountId + ", measureType=" + measureType + "]";
    }

}
