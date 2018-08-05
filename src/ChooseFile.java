import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.util.Consumer;

public class ChooseFile extends AnAction {

    public ChooseFile() {

        super("Choose input file", "", IconLoader.findIcon("file-icon.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        VirtualFile toSelect = null;
        System.out.println("CHOOSING");
        if (CheckerRunner.inputFilePath != null) {
            toSelect = VirtualFileManager.getInstance().refreshAndFindFileByUrl(CheckerRunner.inputFilePath);
        }
        InputConsumer consumer = new InputConsumer();
        FileChooser.chooseFile(descriptor, anActionEvent.getProject(), null, consumer);
        CheckerRunner.inputFilePath = consumer.inputFile.getCanonicalPath();
    }


}

class InputConsumer implements Consumer<VirtualFile> {
    public VirtualFile inputFile;

    public InputConsumer() {
        super();
    }

    @Override
    public void consume(VirtualFile virtualFile) {
        System.out.println("CONSUME");
        inputFile = virtualFile;
    }
}
