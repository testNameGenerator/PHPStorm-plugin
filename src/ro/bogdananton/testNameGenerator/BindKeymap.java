package ro.bogdananton.testNameGenerator;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.impl.KeymapManagerImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;

public class BindKeymap  implements ApplicationComponent {
    public BindKeymap(){
    }

    public void initComponent(){
        //hack: hooking into ctrl+shift+T ;)
        final Keymap keymap = KeymapManagerImpl.getInstance().getActiveKeymap();
        keymap.addShortcut("generateTestMethod", new KeyboardShortcut(KeyStroke.getKeyStroke('U', InputEvent.CTRL_MASK + InputEvent.ALT_MASK), null));
    }

    public void disposeComponent(){
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName(){
        return "BindKeymap";
    }
}
