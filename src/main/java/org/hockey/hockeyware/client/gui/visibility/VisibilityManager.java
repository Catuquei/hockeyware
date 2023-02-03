package org.hockey.hockeyware.client.gui.visibility;

import org.hockey.hockeyware.client.setting.Setting;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the Visibility of Settings.
 */
@SuppressWarnings("unused")
public class VisibilityManager {
    private static final VisibilitySupplier ALWAYS = () -> true;
    private final Map<Setting<?>, VisibilitySupplier> visibilities =
            new HashMap<>();

    public VisibilitySupplier getVisibility(Setting<?> setting) {
        return visibilities.getOrDefault(setting, ALWAYS);
    }

    public void registerVisibility(Setting<?> setting,
                                   VisibilitySupplier visibility) {
        if (visibility == null) {
            visibilities.remove(setting);
            return;
        }

        visibilities.compute(setting, (k, v) ->
        {
            if (v == null) {
                return visibility;
            }

            return visibility.compose(v);
        });
    }

    public boolean isVisible(Setting<?> setting) {
        return getVisibility(setting).isVisible();
    }
}