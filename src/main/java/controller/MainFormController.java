package controller;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainFormController {
    public  Label lblProgress;
    public  Label lblSize;
    public JFXButton btnSelectFile;
    public Label lblFile;
    public JFXButton btnSelectDir;
    public Label lblFolder;
    public Rectangle pgbContainer;
    public Rectangle pgbBar;
    public JFXButton btnCopy;
    public JFXButton btnSelectDirectory;
    public Label lblDirectory;

    private File srcFile;
    private File destDir;
    private int fileSize;
    private  List<File> files;
    //private int fileSize = 0;

    public void initialize() {
        btnCopy.setDisable(true);
    }

    public void btnSelectFileOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Select a file to copy");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*.*"));

        files = fileChooser.showOpenMultipleDialog(lblFolder.getScene().getWindow());

        if (!files.isEmpty()) {
            double tot = 0;
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                tot += (file.length()/1024.0);
            }
            lblFile.setText("Total size selected: " + (tot)+ "Kb");
        }

        else {
            lblFile.setText("No file selected");
        }

        btnCopy.setDisable(files.size() == 0 || destDir == null);
    }

    public void btnSelectDirectoryOnAction(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a folder to copy");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        srcFile = (directoryChooser.showDialog(lblFolder.getScene().getWindow()));

        if (!srcFile.isDirectory()){
            lblDirectory.setText("Not a directory");
            return;
        }

        else {
            lblDirectory.setText("Selected Directory: " + srcFile.getName());
        }

        btnCopy.setDisable(srcFile == null || destDir == null);
   }

    public void btnSelectDirOnAction(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a destination folder");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        destDir = directoryChooser.showDialog(lblFolder.getScene().getWindow());

        if (destDir != null) {
            lblFolder.setText(destDir.getAbsolutePath());
        } else {
            lblFolder.setText("No folder selected");
        }

        btnCopy.setDisable(destDir == null || destDir == null);
    }

    public void btnCopyOnAction(ActionEvent actionEvent) throws IOException, InterruptedException {
        for (int i = 0; i < files.size(); i++) {
            int finalI = i;
            var task = new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    FileInputStream fis = new FileInputStream(files.get(finalI));
                    File destFile = new File(destDir, files.get(finalI).getName());
                    FileOutputStream fos = new FileOutputStream(destFile, true);

                    long fileSize = files.get(finalI).length();
                    for (int j = 0; j < fileSize; j++) {
                        int readByte = fis.read();
                        fos.write(readByte);
                        updateProgress(j, fileSize);
                    }

                    fos.close();
                    fis.close();

                    return null;
                }
            };
            task.workDoneProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number prevWork, Number curWork) {
                    pgbBar.setWidth(pgbContainer.getWidth() * curWork.doubleValue() / task.getTotalWork());
                    lblProgress.setText("Progress: " + (task.getProgress() * 100) + "%");
                    lblSize.setText((task.getWorkDone() / 1024.0) + " / " + (task.getTotalWork() / 1024.0) + " Kb");
                }
            });

            if (finalI == files.size()-1) {
                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {
                        pgbBar.setWidth(pgbContainer.getWidth());
                        new Alert(Alert.AlertType.INFORMATION, "File has been copied successfully").showAndWait();
                        lblFolder.setText("No folder selected");
                        lblFile.setText("No file selected");
                        btnCopy.setDisable(true);
                        pgbBar.setWidth(0);
                        lblProgress.setText("Progress: 0%");
                        lblSize.setText("0 / 0 Kb");
                        srcFile = null;
                        destDir = null;
                    }
                });
            }

            new Thread(task).start();
        }

        /*if (files.size() != 0){
            findFiles(srcFile, destDir);
        }*/
    }



    private  void findFiles(File selectDir, File writeDir) throws IOException {
        File[] files = selectDir.listFiles();
        long size = selectDir.length();

        for (File file : files) {
            if (!file.isDirectory()) {
                copyType(file, writeDir);
            }else{
                findFiles(file, writeDir);
            }
        }

    }

    private static void copyType(File select, File copy) throws IOException {
        FileInputStream fis = new FileInputStream(select);
        File destFile = new File(copy, select.getName());
        int[] byteBuffer = new int[(int) (select).length()];
        FileOutputStream fos = new FileOutputStream(destFile, true);

        for (int j = 0; j < byteBuffer.length; j++) {
            byteBuffer[j] = fis.read();
            fos.write(byteBuffer[j]);
            int k = j+1;
            int fileSize = byteBuffer.length;



        }

        fos.close();
        fis.close();
    }




}




