import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;


public class InputAnnotator implements Annotator {

    static class InputAnnotation {
        TextRange range;
        String message;

        public InputAnnotation  (TextRange r,  String m) {
            range = r;
            message = m;
        }
    }
    static class InputAnnotationList {
        long modStamp;
        List<InputAnnotation> annotations = new ArrayList<>();

        InputAnnotationList(long m) {
            modStamp = m;
        }

    }
    public static final Map<PsiFile, InputAnnotationList> annotations = new HashMap<>();
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        if (element instanceof PsiFile) {
            System.out.println("Annotate");
            PsiFile file = (PsiFile) element;

            if (!(annotations.containsKey(file) && annotations.get(file).modStamp == file.getText().hashCode())) {

                VirtualFile inputFile = element.getContainingFile().getVirtualFile();
                Document document = FileDocumentManager.getInstance().getDocument(inputFile);
                String infilePath = inputFile.getCanonicalPath();
                String errfilePath = infilePath + ".err";
                File errFile = new File(errfilePath);
                InputAnnotationList annotationList = new InputAnnotationList(file.getText().hashCode());
                if (errFile.exists()) {
                    System.out.println("Error file: " + errfilePath);
                    System.out.println("Code file: " + infilePath);
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(errFile));
                        String separator = br.readLine();
                        String line = null;
                        while((line = br.readLine()) != null) {
                            StringTokenizer st = new StringTokenizer(line, separator);
                            int line_number = Integer.parseInt(st.nextToken()) - 1;
                            st.nextToken(); st.nextToken(); //skipping manually calculated offset
                            int startOffset = document.getLineStartOffset(line_number);
                            int endOffset = document.getLineEndOffset(line_number);
                            String message = st.nextToken();
                            InputAnnotation inputAnnotation = new InputAnnotation(new TextRange(startOffset, endOffset), message);
                            annotationList.annotations.add(inputAnnotation);
                            System.out.printf("Line %d, [%d, %d], Message: %s\n", line_number, startOffset, endOffset, message);

                        }
                    } catch (FileNotFoundException e) {
                        createPopup("Error file not found", MessageType.ERROR, element.getProject());
                    } catch (IOException e) {
                        createPopup("Annotator I/O Exception", MessageType.ERROR, element.getProject());
                    }

                }
                annotations.put(file, annotationList);



            }

            if (annotations.containsKey(file)) {
                InputAnnotationList annotationList = annotations.get(file);
                for (InputAnnotation a : annotationList.annotations) {
                    if (a.message.startsWith("*")) {
                        annotationHolder.createWarningAnnotation(a.range, a.message.substring(1));
                    } else {
                        annotationHolder.createErrorAnnotation(a.range, a.message);
                    }
                }
            }
        }






    }

    public void createPopup(String message, MessageType type, Project project) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(message, type, null)
                .setFadeoutTime(7500)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                        Balloon.Position.atRight);
    }
}
