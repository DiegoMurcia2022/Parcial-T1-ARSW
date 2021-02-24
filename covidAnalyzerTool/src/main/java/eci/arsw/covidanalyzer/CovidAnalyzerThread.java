package eci.arsw.covidanalyzer;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CovidAnalyzerThread extends Thread {
    private final List<File> resultFiles;
    private final ResultAnalyzer resultAnalyzer;
    private final AtomicInteger amountOfFilesProcessed;
    private final TestReader testReader;
    private boolean pause;
    private int a;
    private int b;

    public CovidAnalyzerThread(List<File> resultFiles, int a, int b, ResultAnalyzer resultAnalyzer,AtomicInteger amountOfFilesProcessed, TestReader testReader) {
        this.resultFiles = resultFiles;
        this.resultAnalyzer = resultAnalyzer;
        this.amountOfFilesProcessed = amountOfFilesProcessed;
        this.testReader = testReader;
        this.a = a;
        this.b = b;
    }

    public void run() {
        for (File file:resultFiles) {
            synchronized (this) {
                while (pause) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            List<Result> results = testReader.readResultsFromFile(file);

            for (Result result:results) {
                resultAnalyzer.addResult(result);
            }

            amountOfFilesProcessed.incrementAndGet();
        }
    }

    public void pauseThread() {
        pause = true;
    }

    public void resumeThread() {
        pause = false;

        synchronized (this) {
            notifyAll();
        }
    }
}
