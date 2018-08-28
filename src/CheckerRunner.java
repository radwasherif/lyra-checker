import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.SaveAndSyncHandlerImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import com.jetbrains.python.run.PythonRunConfiguration;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class CheckerRunner extends GenericProgramRunner {
    public static String codeFilePath, inputFilePath;
    public static VirtualFile inputFile;
    static HashMap<String, ArrayList<CheckerError>> errors = new HashMap<>();
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
        //system path separator
        System.out.println("1- Checker runner");
        String sep = File.separatorChar + "";
        Project project = env.getProject();
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (inputFile == null) {
            System.out.println(" 1-* No input file");
            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder("Choose input file first.", IconLoader.findIcon("file-icon.png"), Color.LIGHT_GRAY, null)
                    .setFadeoutTime(7500)
                    .createBalloon()
                    .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                            Balloon.Position.atRight);
            return null;
        }
        project.save();
        FileDocumentManager.getInstance().saveAllDocuments();
        // get path to lyra in the virtual environment
        PythonRunConfiguration runConfiguration = (PythonRunConfiguration) env.getRunProfile();
        String interpreterPath = runConfiguration.getInterpreterPath();
        String array [] = interpreterPath.split(sep);
        array[array.length - 1] = "lyra";
        String lyraPath = String.join(sep, array);
        System.out.println("2- Lyra path: " + lyraPath);

        //get path to code file to be analysed
        codeFilePath = runConfiguration.getScriptName();
        System.out.println("3- Code path: " + codeFilePath);
        inputFilePath = inputFile.getCanonicalPath();
        System.out.println("4- Input file path: " + inputFilePath);

        if (codeFilePath == null || inputFilePath == null)
            return null;

        String command = lyraPath + " --analysis assumptions " + codeFilePath + " " + inputFilePath;
        System.out.println("5 - command: " + command);
        try {

            Process proc =  Runtime.getRuntime().exec(command);
            BufferedReader br;
            String line  = null;
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//            System.out.println("-------Input stream-------");
//            while((line = br.readLine()) != null) {
//                System.out.println(line);
//            }

            br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            System.out.println("-------Error stream-------");
            while((line = br.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("-------Error stream end-------");

            Thread.sleep(300);
            SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
//          inputFile.refresh(true, true);
//            FileDocumentManager.getInstance().reloadFiles(inputFile);
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
            System.out.println("6- Refreshing input file " + inputFilePath);

            String errFilePath = inputFilePath + ".err";
            File errFile = new File(errFilePath);
            if (errFile.exists() && errFile.length() == 0) {
                String filename = inputFile.getName();

                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder("No errors found in file " + filename + ".", MessageType.INFO, null)
                        .createBalloon()
                        .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                                Balloon.Position.atRight);
            } else {
                br = new BufferedReader(new FileReader(errFile));
                String separator = br.readLine();
                StringTokenizer st = new StringTokenizer(br.readLine(), separator);
                String line_number = st.nextToken();
                st.nextToken();
                st.nextToken();
                String message = st.nextToken();
                message = "Please fix error on line " + line_number + " : " + message;
//                System.out.println("SHOWING BALLOON " + message);
                JBPopupFactory.getInstance()
                        .createHtmlTextBalloonBuilder(message, MessageType.ERROR, null)
                        .setHideOnClickOutside(false)
                        .setHideOnCloseClick(true)
                        .setHideOnKeyOutside(false)
                        .createBalloon()
                        .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                                Balloon.Position.atRight);
//                JBPopupFactory.getInstance()
//                        .createMessage(message).show(RelativePoint.getCenterOf(statusBar.getComponent()));
            }
        } catch (IOException e) {
            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder("Checker runner I/O exception.", MessageType.ERROR, null)
                    .setFadeoutTime(7500)
                    .createBalloon()
                    .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                            Balloon.Position.atRight);
        } catch (Exception e) {
            e.printStackTrace();
        }


//        //write errors
//        String errorFilePath = inputFilePath + ".err";
//        ArrayList<CheckerError> errorList = new ArrayList<>();
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(errorFilePath));
//            String line = null;
//            String separator = br.readLine();
//            while((line = br.readLine()) != null) {
//                StringTokenizer st = new StringTokenizer(line, separator);
//                int line_number = Integer.parseInt(st.nextToken())  - 1;
//                int startOffset = Integer.parseInt(st.nextToken());
//                int endOffset = Integer.parseInt(st.nextToken());
//                CheckerError error = new CheckerError(line_number, startOffset, endOffset, st.nextToken());
//                errorList.add(error);
//            }
//            System.out.println("ERRORS WRITTEN");
//            System.out.println(errorList.toString());
//            if (errorList.isEmpty()) {
//                String filename = VirtualFileManager.getInstance().refreshAndFindFileByUrl(inputFilePath).getName();
//                JBPopupFactory.getInstance()
//                        .createHtmlTextBalloonBuilder("No errors found in file" + filename + ".", MessageType.INFO, null)
//                        .setFadeoutTime(7500)
//                        .createBalloon()
//                        .show(RelativePoint.getCenterOf(statusBar.getComponent()),
//                                Balloon.Position.atRight);
//            }
//            errors.put(inputFilePath, errorList);
//
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        return null;
    }

}