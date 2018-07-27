import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;


public class InputAnnotator implements Annotator {
    static class Tuple <L, R> {
        public L l;
        public R r;
        public Tuple(L l, R r) {
            this.l = l;
            this.r = r;
        }
    }
    public static long lastModificationStamp;
    public static ArrayList<Tuple<TextRange, String>> errors;
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        String infilePath = element.getContainingFile().getVirtualFile().getCanonicalPath();
        String errfilePath = infilePath + ".err";
        File errFile = new File(errfilePath);
        long newModificationStamp = errFile.lastModified();
        if (errFile.exists()) {
            System.out.println("Error file: " + errfilePath);
            System.out.println("Code file: " + infilePath);
            if (newModificationStamp > lastModificationStamp || errors == null) {
                lastModificationStamp = newModificationStamp;
                try {
                    BufferedReader br = new BufferedReader(new FileReader(errFile));
                    String separator = br.readLine();
                    String line = null;
                    errors = new ArrayList<>();
                    while((line = br.readLine()) != null) {
                        System.out.println(line);
                        StringTokenizer st = new StringTokenizer(line,separator);
                        int line_number = Integer.parseInt(st.nextToken());
                        int startOffset = Integer.parseInt(st.nextToken());
                        int endOffset = Integer.parseInt(st.nextToken());
                        String message = "";
                        while (st.hasMoreTokens()) {
                            message += st.nextToken() + "; ";
                        }
//                        System.out.printf("Start: %d, End: %d, Message: %s\n", startOffset, endOffset, message);
//                        annotationHolder.createErrorAnnotation(new TextRange(startOffset, endOffset), message);
                        errors.add(new Tuple<>(new TextRange(startOffset, endOffset), message));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (Tuple<TextRange, String> err: errors) {

                annotationHolder.createErrorAnnotation(err.l, err.r);
            }

        }


    }
}
