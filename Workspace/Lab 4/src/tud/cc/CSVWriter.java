package tud.cc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;



public class CSVWriter
	implements AutoCloseable
{
	private final PrintWriter writer;
	
	
	private static CSVWriter _jobs = null;
	public static CSVWriter getJobs() throws FileNotFoundException, UnsupportedEncodingException
	{
		if (_jobs == null)
			_jobs = new CSVWriter("jobs.csv");
		return _jobs;
	}
	public static CSVWriter _samples = null;
	public static CSVWriter getSamples() throws FileNotFoundException, UnsupportedEncodingException
	{
		if (_samples == null)
			_samples = new CSVWriter("samples.csv");
		return _samples;
	}
	
	
	public CSVWriter(String filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		writer = new PrintWriter(filename, "UTF-8");
	}
	
	
	public synchronized void writeLine(Object... obs)
	{
		for (Object o : obs)
		{
			writer.print(o.toString() + ", ");
		}
		writer.println();
		writer.flush();
	}

	
	@Override
	public void close() throws Exception 
	{
		writer.close();
	}

}
