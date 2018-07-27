import com.intellij.execution.Executor;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindowId;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CheckerExecutor extends Executor {
    public static final Icon LYRA_ICON = IconLoader.findIcon("/icon.png");
    public static final String EXECUTOR_ID = "MyChecker";
    @Override
    public String getToolWindowId() {
        return ToolWindowId.RUN;
    }

    @Override
    public Icon getToolWindowIcon() {
        return LYRA_ICON;
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return LYRA_ICON;
    }

    @Override
    public Icon getDisabledIcon() {
        return LYRA_ICON;
    }

    @Override
    public String getDescription() {
        return "Runs the Lyra analysis on the current file.";
    }

    @NotNull
    @Override
    public String getActionName() {
        return "Run my Lyra Checker";
    }

    @NotNull
    @Override
    public String getId() {
        return EXECUTOR_ID;
    }

    @NotNull
    @Override
    public String getStartActionText() {
        return "Start my checker";
    }

    @Override
    public String getContextActionId() {
        return "Start my checker";
    }

    @Override
    public String getHelpId() {
        return null;
    }
}
