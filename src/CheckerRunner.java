import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.python.run.PythonRunConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CheckerRunner extends GenericProgramRunner {
    public static String codeFilePath, inputFilePath;
    public static long lastModificationStamp;
    @NotNull
    @Override
    public String getRunnerId() {
        return "CheckerRunner";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile runProfile) {
        boolean result = executorId.equals(CheckerExecutor.EXECUTOR_ID) ;
        result = result && runProfile.getClass().toString().contains("PythonRunConfiguration");
        return result;
    }

    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
        Editor editor = env.getDataContext().getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            throw new NullPointerException("Editor is null");
        }
        String sep = File.separatorChar + "";
        // get the path to the current open file -- the input file to be checked
        Document document =  editor.getDocument();
        System.out.println("HERE");
        VirtualFile inputFile = FileDocumentManager.getInstance().getFile(document);
        inputFilePath = inputFile.getCanonicalPath();

        // get path to lyra in the virtual environment
        PythonRunConfiguration runConfiguration = (PythonRunConfiguration) env.getRunProfile();
        String interpreterPath = runConfiguration.getInterpreterPath();
        String array [] = interpreterPath.split(sep);
        array[array.length - 1] = "lyra";
        String lyraPath = String.join(sep, array);
        System.out.println("Lyra path: " + lyraPath);

//        //get path to code file to be analised
        codeFilePath = runConfiguration.getScriptName();
        System.out.println("Code path: " + codeFilePath);
        System.out.println("Input file path: " + inputFilePath);
        File file = new File(codeFilePath);
        long newModificationStamp = file.lastModified();
        boolean code_modified = newModificationStamp > lastModificationStamp;
        lastModificationStamp = newModificationStamp;
//        System.out.println("Modification stamps: " + newModificationStamp + " " + lastModificationStamp);
//        System.out.println("Code modified: " + code_modified);
        String command = lyraPath + " --analysis assumptions " + codeFilePath + " " + inputFilePath;
        System.out.println("Final command: " + command);

        try {
           Process proc =  Runtime.getRuntime().exec(command);
            BufferedReader br;
            String line  = null;
//            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//            System.out.println("-------Input stream-------");
//            while((line = br.readLine()) != null) {
//                System.out.println(line);
//            }

            br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            System.out.println("-------Error stream-------");
            while((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getCwd() {
        String s = this.getClass().getClassLoader().getResource("").getPath();
        System.out.println(s);
//        /home/radwa/IdeaProjects/lyra-checker/venv
       return s;
    }
}