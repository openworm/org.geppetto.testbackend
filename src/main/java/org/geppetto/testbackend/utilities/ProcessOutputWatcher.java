package org.geppetto.testbackend.utilities;


import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.testbackend.services.DummySimulatorService;

/**
 *
 * @author Padraig Gleeson
 *  
 */

public class ProcessOutputWatcher extends Thread
{

    private InputStreamReader inputStrReader = null;

    private String referenceName = null;

    private StringBuffer log = new StringBuffer();
    
	private static Log _logger = LogFactory.getLog(ProcessOutputWatcher.class);

    public ProcessOutputWatcher(InputStream inputStr, String referenceName)
    {
        this.inputStrReader = new InputStreamReader(inputStr);
        this.referenceName = referenceName;
    }

    public String getLog()
    {
        return log.toString();
    }
    
    @Override
    public void run()
    {
        try
        {
            //int numberOfBytesRead;

            BufferedReader br = new BufferedReader(inputStrReader);
            String line=null;
            while ( (line = br.readLine()) != null)
            {
                _logger.info(referenceName +"> "+line);
                log.append(line+"\n");
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
