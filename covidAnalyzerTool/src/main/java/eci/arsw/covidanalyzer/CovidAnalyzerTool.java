package eci.arsw.covidanalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Camel Application
 */
public class CovidAnalyzerTool implements Runnable {

    private ResultAnalyzer resultAnalyzer;
    private TestReader testReader;
    private int amountOfFilesTotal;
    private AtomicInteger amountOfFilesProcessed;
    private static final int THREAD_NUMBER = 5;
    private ConcurrentLinkedDeque<CovidAnalyzerThread> threads;
    private boolean pause;

    public CovidAnalyzerTool() {
        resultAnalyzer = new ResultAnalyzer();
        testReader = new TestReader();
        amountOfFilesProcessed = new AtomicInteger();
        threads = new ConcurrentLinkedDeque<>();
        pause = false;
        amountOfFilesTotal = -1;
    }

    public void processResultData() {
        amountOfFilesProcessed.set(0);
        List<File> resultFiles = getResultFileList();
        amountOfFilesTotal = resultFiles.size();
        int range = amountOfFilesTotal/THREAD_NUMBER;

        for (int i=0; i<THREAD_NUMBER; i++) {
            if (i==THREAD_NUMBER-1) {
                threads.addLast(new CovidAnalyzerThread(resultFiles, i*range, amountOfFilesTotal-1, resultAnalyzer, amountOfFilesProcessed, testReader));
            } else {
                threads.addLast(new CovidAnalyzerThread(resultFiles, i*range, (i*range)+range-1, resultAnalyzer, amountOfFilesProcessed, testReader));
            }

            threads.getLast().start();
        }
    }

    private List<File> getResultFileList() {
        List<File> csvFiles = new ArrayList<>();
        try (Stream<Path> csvFilePaths = Files.walk(Paths.get("src/main/resources/")).filter(path -> path.getFileName().toString().endsWith(".csv"))) {
            csvFiles = csvFilePaths.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return csvFiles;
    }


    public Set<Result> getPositivePeople() {
        return resultAnalyzer.listOfPositivePeople();
    }

    public void showReport() {
        String message = "Processed %d out of %d files.\nFound %d positive people:\n%s";
        Set<Result> positivePeople = getPositivePeople();
        String affectedPeople = positivePeople.stream().map(Result::toString).reduce("", (s1, s2) -> s1 + "\n" + s2);
        message = String.format(message, amountOfFilesProcessed.get(), amountOfFilesTotal, positivePeople.size(), affectedPeople);
        System.out.println(message);
    }

    public void pauseThread() {
        System.out.println("--------------------PAUSE--------------------");
        pause = true;

        for (CovidAnalyzerThread thread:threads) {
            thread.pauseThread();
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resumeThread() {
        System.out.println("--------------------RESUME--------------------");
        pause = false;
        for (CovidAnalyzerThread thread:threads) {
            thread.resumeThread();
        }
    }

    public void run() {
        Thread thread = new Thread(this::processResultData);
        thread.start();

        while (amountOfFilesTotal==-1 || amountOfFilesProcessed.get()<amountOfFilesTotal) {
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();

            if (line.contains("exit")) {
                break;
            } else if (line.isEmpty()) {
                if (pause) {
                    resumeThread();
                } else {
                    pauseThread();
                    showReport();
                }
            } else if (!pause && !line.isEmpty()) {
                showReport();
            }
        }
    }

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Thread thread = new Thread(new CovidAnalyzerTool());
        thread.start();
    }
}

