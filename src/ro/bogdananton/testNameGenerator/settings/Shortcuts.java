package ro.bogdananton.testNameGenerator.settings;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.impl.KeymapManagerImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;

public class Shortcuts implements ApplicationComponent {
    public Shortcuts(){
    }

    public void initComponent(){
        final Keymap keymap = KeymapManagerImpl.getInstance().getActiveKeymap();
        keymap.addShortcut("generateTestMethod", new KeyboardShortcut(KeyStroke.getKeyStroke('U', InputEvent.CTRL_MASK + InputEvent.ALT_MASK), KeyStroke.getKeyStroke('U', 0)));
    }

    public void disposeComponent(){
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName(){
        return "Shortcuts";
    }
}
