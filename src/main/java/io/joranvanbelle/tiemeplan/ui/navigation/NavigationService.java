package io.joranvanbelle.tiemeplan.ui.navigation;

import java.util.EnumMap;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class NavigationService {
    private final BorderPane contentPane;
    private final Map<Screen, ViewProvider> providers = new EnumMap<>(Screen.class);
    private Screen currentScreen;

    public NavigationService(BorderPane contentPane) {
        this.contentPane = contentPane;
    }

    public void register(Screen screen, ViewProvider provider) {
        providers.put(screen, provider);
    }

    public void navigateTo(Screen screen) {
        var provider = providers.get(screen);
        if (provider == null) {
            throw new IllegalStateException("No view registered for screen " + screen);
        }
        Node view = provider.getView();
        contentPane.setCenter(view);
        currentScreen = screen;
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }
}
