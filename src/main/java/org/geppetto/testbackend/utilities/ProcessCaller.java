package org.geppetto.testbackend.utilities;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;

public class ProcessCaller extends Thread{
	
	private static Log _logger = LogFactory.getLog(ProcessCaller.class);
	private File _file;
	private boolean _recompile;
	public volatile boolean run = true;
	
	public ProcessCaller(File file, boolean recompile){
		this._recompile = recompile;
		this._file = file;
	}
	@Override
	public void run(){
		if(run){
			run = false;
			try {
				this.compileFileWithNeuron(_file, _recompile);
			} catch (GeppettoExecutionException e) {
				_logger.error("Geppetto Exectuion Exception " + e.getMessage());
			}
		}
	}
	
	/*
     * Compliles all of the mod files at the specified location using NEURON's nrnivmodl/mknrndll.sh
     */
	public boolean compileFileWithNeuron(File modDirectory, boolean forceRecompile) throws GeppettoExecutionException {
		_logger.info("Going to compile the mod files in: " + modDirectory.getAbsolutePath() + ", forcing recompile: " + forceRecompile);

		Runtime rt = Runtime.getRuntime();

		File neuronHome = null;
		try {

			neuronHome = Utilities.findNeuronHome();

			String commandToExecute = null;

			String directoryToExecuteIn = modDirectory.getCanonicalPath();

			if(modDirectory.isDirectory()){
				commandToExecute = neuronHome.getCanonicalPath()
						+ System.getProperty("file.separator")
						+ "bin"
						+ System.getProperty("file.separator")
						+ "nrnivmodl";
			}else{
				String extension = "";

				int i = modDirectory.getAbsolutePath().lastIndexOf('.');
				if (i > 0) {
				    extension = modDirectory.getAbsolutePath().substring(i+1);
				}
				
				_logger.info("File with extension " + extension + " detected");
				
				directoryToExecuteIn = modDirectory.getParentFile().getAbsolutePath();
				if(extension.equals("hoc")){
					commandToExecute = neuronHome.getCanonicalPath()
							+ System.getProperty("file.separator")
							+ "bin"
							+ System.getProperty("file.separator")
							+ "nrngui " + modDirectory.getAbsolutePath();
				}
				else if(extension.equals("py")){
					commandToExecute = "python " + modDirectory.getAbsolutePath();
				}
			}
			
			_logger.info("commandToExecute: " + commandToExecute);
			_logger.info("from directory : " + directoryToExecuteIn);
			
			Process currentProcess = rt.exec(commandToExecute, null, new File(directoryToExecuteIn));
			ProcessOutputWatcher procOutputMain = new ProcessOutputWatcher(currentProcess.getInputStream(),  "NMODL Compile >> ");
			procOutputMain.start();

			ProcessOutputWatcher procOutputError = new ProcessOutputWatcher(currentProcess.getErrorStream(), "NMODL Error   >> ");
			procOutputError.start();

			_logger.info("Have successfully executed command: " + commandToExecute);
			
			currentProcess.waitFor();
		} catch (InterruptedException e) {
			_logger.error("Interrupted Exception " + e.getMessage());
		} catch (GeppettoInitializationException e) {
			_logger.error("Initialization error" + e.getMessage());
		} catch (IOException e) {
			_logger.error("Initialization error " + e.getMessage());
		}

        return true;
    }

}
