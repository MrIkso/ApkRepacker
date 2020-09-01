package brut.util;

import com.mrikso.apktool.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import brut.common.BrutException;

public class CommandRunner {
    private static String[] mOutputs = new String[2];

    public void exec(List<String> cmd, Logger logger) throws BrutException {
        Process ps = null;
        int exitValue = -99;
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            ps = builder.start();
            new StreamForwarder(ps.getErrorStream(), "ERROR", logger).start();
            new StreamForwarder(ps.getInputStream(), "OUTPUT", logger).start();
            exitValue = ps.waitFor();
            if (exitValue != 0)
                throw new BrutException("could not exec (exit code = " + exitValue + "): " + cmd);
        } catch (IOException ex) {
            throw new BrutException("could not exec: " + cmd, ex);
        } catch (InterruptedException ex) {
            throw new BrutException("could not exec : " + cmd, ex);
        }
    }


    public String getStdOut() {
        return mOutputs[0];
    }

    public String getStdError() {
        return mOutputs[1];
    }

    static class StreamForwarder extends Thread  {

        private final InputStream mIn;
        private final String mType;
        private Logger mLogger;
        StreamForwarder(InputStream is, String type, Logger logger) {
            mIn = is;
            mType = type;
            mLogger = logger;
        }

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(mIn));
                String line;
                while ((line = br.readLine()) != null) {
                    if (mType.equals("OUTPUT")) {
                        mOutputs[0] = line;
                        mLogger.info(R.string.log_text,line);
                    } else {
                        mOutputs[1] = line;
                        mLogger.error(R.string.log_text,line);
                        //Log.e("commandRunner", line);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
