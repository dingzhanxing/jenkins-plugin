package com.rocket.devops.rdoi.utils;

import com.google.common.base.Stopwatch;
import com.rocket.devops.rdoi.Messages;
import com.rocket.devops.rdoi.common.AppConfiguration;
import com.rocket.devops.rdoi.common.exception.RDORuntimeException;
import hudson.model.TaskListener;
import org.kohsuke.stapler.export.Exported;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class LogReader extends AgentAccess{

    private static final long serialVersionUID = 1L;

    @Exported
    private String LogFileName;

    public LogReader(TaskListener listener, String logFileName) {
        super(listener);
        LogFileName = logFileName;
    }

    @Override
    public Integer call() throws IOException {

        PrintStream logger = listener.getLogger();
        Integer runResult = 0;
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");

        logger.println("Agent os.name:" + osName);
        logger.println("Agent os.version:" + osVersion);

        File file = new File(LogFileName);
        //Integer timeToWaitSeconds = Integer.parseInt(AppConfiguration.getProperty("wait.maxtime.read.logfile"));
        Integer timeToWait = Integer.parseInt(AppConfiguration.getProperty("wait.maxtime.read.logfile")) * 1000/10;

        int count = 0;
        while (!file.exists()){
            try {
                Thread.sleep(timeToWait);
                count++;
            } catch (InterruptedException e) {
                throw new RDORuntimeException(e);
            }
            if(count>= 10)
            break;
            file = new File(LogFileName);
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(LogFileName)));
        } catch (FileNotFoundException e) {
            String msg = Messages.RDOi_Build_logfile_errors(LogFileName);
            throw new RDORuntimeException(msg);
        }
        Stopwatch stopWatch = Stopwatch.createStarted();
        long elapsedSecondsStart = stopWatch.elapsed(TimeUnit.SECONDS);
        //logger.println("elapsedSecondsStart:" + elapsedSecondsStart);
        long elapsedSecondsConsume = 0;
        String data = null;
        while(true)
        {
            data = br.readLine();
            if(data == null ){
                continue;
            }else {
                if (data.contains(Constants.JENKINS_BUILD_SUCCESS)){
                    runResult = 0;
                    break;
                } else if (data.contains(Constants.JENKINS_BUILD_FAILED) || data.contains(Constants.JENKINS_BUILD_COMPLETED)) {
                    runResult = 1;
                    break;
                }
                logger.println(data);
            }
//            elapsedSecondsConsume = stopWatch.elapsed(TimeUnit.SECONDS);
//            if (elapsedSecondsConsume > timeToWaitSeconds * 4){
//                runResult = 1;
//                break;
//            }
            //logger.println("elapsedSecondsConsume:" + elapsedSecondsConsume);
        }
        if (null != br)
            br.close();
        return runResult;
    }
}
