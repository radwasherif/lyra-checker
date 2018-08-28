import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Consumer;

import java.awt.*;

public class ChooseFile extends AnAction {

    public ChooseFile() {

        super("Choose input file", "", IconLoader.findIcon("file-icon.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        VirtualFile toSelect = null;
        System.out.println("2-* CHOOSING INPUT FILE");
        if (CheckerRunner.inputFilePath != null) {
            System.out.println("2-** TO SELECT FILE");
            toSelect = VirtualFileManager.getInstance().refreshAndFindFileByUrl(CheckerRunner.inputFilePath);
        }
        InputConsumer consumer = new InputConsumer();
        FileChooser.chooseFile(descriptor, anActionEvent.getProject(), toSelect, consumer);
        FileType type = consumer.inputFile.getFileType();
        if (type != PlainTextFileType.INSTANCE) {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(anActionEvent.getProject());
            JBPopupFactory.getInstance()

                    
                    .createHtmlTextBalloonBuilder("Choose an input text file.", IconLoader.findIcon("file-icon.png"), Color.LIGHT_GRAY, null)
                    .setFadeoutTime(7500)
                    .createBalloon()
                    .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                            Balloon.Position.atRight);
        } else {
            CheckerRunner.inputFile = consumer.inputFile;
        }
        System.out.println("5-* SET INPUT FILE");
    }


}

class InputConsumer implements Consumer<VirtualFile> {
    public VirtualFile inputFile;

    public InputConsumer() {
        super();
        System.out.println("3-* CONSUMER CONSTRUCTOR");
    }

    @Override
    public void consume(VirtualFile virtualFile) {
        System.out.println("4-* CONSUME");
        inputFile = virtualFile;
    }
}
