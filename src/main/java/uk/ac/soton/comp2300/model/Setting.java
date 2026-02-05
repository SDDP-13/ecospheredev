package uk.ac.soton.comp2300.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.List;
import java.util.function.Consumer;

public class Setting {

    public static final List<SettingOption> settingsList = List.of(
    new SettingOption("notifications", "Notifications", "Keep up to date while not in game", false, enabled -> {
        System.out.println(enabled ? "Notifications ON" : "Notifications OFF");
    }),
    new SettingOption("darkMode", "Dark mode", "For low light conditions", false, enabled -> {
        System.out.println("Dark mode = " + enabled);
    })
    );


  public static class SettingOption {
    private final String key;
    private final String title;
    private final String description;

    private final BooleanProperty enabled;
    private final Consumer<Boolean> onChanged;

    public SettingOption(String key, String title, String description, boolean defaultValue, Consumer<Boolean> onChanged) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.enabled = new SimpleBooleanProperty(defaultValue);
        this.onChanged = onChanged;

      this.enabled.addListener((obs, oldV, newV) -> {
        if (this.onChanged != null) this.onChanged.accept(newV);
      });
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }

    public boolean isEnabled() { return enabled.get(); }
    public void setEnabled(boolean value) { enabled.set(value); }
    public BooleanProperty enabledProperty() { return enabled; }
  }
}
