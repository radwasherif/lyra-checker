import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;


public class InputAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        System.out.println("Annotate");
        VirtualFile inputFile = element.getContainingFile().getVirtualFile();
        Document document = FileDocumentManager.getInstance().getDocument(inputFile);
        String infilePath = inputFile.getCanonicalPath();
        String errfilePath = infilePath + ".err";
        File errFile = new File(errfilePath);
//        ArrayList<CheckerError> errorList = CheckerRunner.errors.get(inputFile.getCanonicalPath());
        if (errFile.exists()) {
//            for (CheckerError e: errorList) {
//                int startOffset = document.getLineStartOffset(e.line);
//                int endOffset = document.getLineEndOffset(e.line);
//                System.out.printf("Line %d, [%d, %d], Message: %s\n", e.line, startOffset, endOffset, e.message);
//                annotationHolder.createErrorAnnotation(new TextRange(startOffset, endOffset), e.message);
//            }

            System.out.println("Error file: " + errfilePath);
            System.out.println("Code file: " + infilePath);
            try {
                BufferedReader br = new BufferedReader(new FileReader(errFile));
                String separator = br.readLine();
                String line = null;
                while((line = br.readLine()) != null) {
                    System.out.println(line);
                    StringTokenizer st = new StringTokenizer(line, separator);
                    int line_number = Integer.parseInt(st.nextToken()) - 1;
                    System.out.println(st.nextToken());
                    System.out.println(st.nextToken());
                    int startOffset = document.getLineStartOffset(line_number);
                    int endOffset = document.getLineEndOffset(line_number);
                    String message = st.nextToken();
                    System.out.printf("Line %d, [%d, %d], Message: %s\n", line_number, startOffset, endOffset, message);
                    if(line_number < document.getLineCount())
                        annotationHolder.createErrorAnnotation(new TextRange(startOffset, endOffset), message);
                }
            } catch (FileNotFoundException e) {
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(element.getProject());
                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder("Annotation file not found. Please try again.", MessageType.ERROR, null)
                        .setFadeoutTime(7500)
                        .createBalloon()
                        .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                                Balloon.Position.atRight);
            } catch (IOException e) {

                StatusBar statusBar = WindowManager.getInstance().getStatusBar(element.getProject());
                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder("Annotation I/O exception.", MessageType.ERROR, null)
                        .setFadeoutTime(7500)
                        .createBalloon()
                        .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                                Balloon.Position.atRight);
            }

        }


    }
}
