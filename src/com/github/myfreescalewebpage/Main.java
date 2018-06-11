package com.github.myfreescalewebpage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		/* Retrieve options */
		Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        Option optionName = Option.builder("n").longOpt("name").desc("Package name").hasArg(true).argName("name").required(true).build();
        Option optionVersion = Option.builder("v").longOpt("version").desc("Package version").hasArg(true).argName("version").required(true).build();
        Option optionArchitecture = Option.builder("a").longOpt("arch").desc("Package architecture").hasArg(true).argName("architecture").required(true).build();
        Option optionDepends = Option.builder("dep").longOpt("depends").desc("Package depends").hasArg(true).argName("depends").required(false).build();
        Option optionMaintainer = Option.builder("maint").longOpt("maintainer").desc("Package maintainer").hasArg(true).argName("maintainer").required(false).build();
        Option optionDescription = Option.builder("desc").longOpt("desc").desc("Package description").hasArg(true).argName("description").required(false).build();
        Option optionPreInst = Option.builder("preinst").longOpt("preinst").desc("Package pre-installation script path").hasArg(true).argName("preinst").required(false).build();
        Option optionPostInst = Option.builder("postinst").longOpt("postinst").desc("Package post-installation script path").hasArg(true).argName("postinst").required(false).build();
        Option optionPreRm = Option.builder("prerm").longOpt("prerm").desc("Package pre-remove script path").hasArg(true).argName("prerm").required(false).build();
        Option optionPostRm = Option.builder("postrm").longOpt("postrm").desc("Package post-remove script path").hasArg(true).argName("postrm").required(false).build();
        Option optionInputPath = Option.builder("i").longOpt("input").desc("Input path").hasArg(true).argName("input").required(true).build();
        Option optionOutputPath = Option.builder("o").longOpt("output").desc("Output Path").hasArg(true).argName("output").required(true).build();
        options.addOption(optionName);
        options.addOption(optionVersion);
        options.addOption(optionArchitecture);
        options.addOption(optionDepends);
        options.addOption(optionMaintainer);
        options.addOption(optionDescription);
        options.addOption(optionPreInst);
        options.addOption(optionPostInst);
        options.addOption(optionPreRm);
        options.addOption(optionPostRm);
        options.addOption(optionInputPath);
        options.addOption(optionOutputPath);
        CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			/* Display usage and stop */
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ipkbuilder", "\nCreate IPK packages\n\n", options, "\nPlease report issues at https://github.com/myfreescalewebpage/IpkBuilder", true);
			return;
		}
		
		String packageName = commandLine.getOptionValue(optionName.getLongOpt());
		String packageVersion = commandLine.getOptionValue(optionVersion.getLongOpt());
		String packageArchitecture = commandLine.getOptionValue(optionArchitecture.getLongOpt());
		String packageDepends = commandLine.getOptionValue(optionDepends.getLongOpt());
		String packageMaintainer = commandLine.getOptionValue(optionMaintainer.getLongOpt());
		String packageDescription = commandLine.getOptionValue(optionDescription.getLongOpt());
		String preinst = commandLine.getOptionValue(optionPreInst.getLongOpt());
		String postinst = commandLine.getOptionValue(optionPostInst.getLongOpt());
		String prerm = commandLine.getOptionValue(optionPreRm.getLongOpt());
		String postrm = commandLine.getOptionValue(optionPostRm.getLongOpt());
		String inputPath = commandLine.getOptionValue(optionInputPath.getLongOpt());
		String outputPath = commandLine.getOptionValue(optionOutputPath.getLongOpt());
		
		/* Create temporary directory */
		File temporaryDirectory = new File(outputPath + File.separator + "ipk");
		if (temporaryDirectory.exists()) temporaryDirectory.delete();
		temporaryDirectory.mkdir();
		
		/* Create files inside the temporary directory */
		createDebianBinary(temporaryDirectory);
		createControlTarGz(temporaryDirectory, packageName, packageVersion, packageArchitecture, packageDepends, packageMaintainer, packageDescription, preinst, postinst, prerm, postrm);
		createDataTarGz(temporaryDirectory, inputPath);
		
		/* Create IPK in the output directory */
		DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");
		createIpk(temporaryDirectory, outputPath + File.separator + String.format("%s-%s-%s-%s.ipk", packageName, packageVersion, dateFormat.format(new Date()), packageArchitecture));
		
		/* Remove temporary directory */
		FileUtils.deleteDirectory(temporaryDirectory);
	}
	
	/**
	 * Create debian-binary
	 * @param temporaryDirectory Output temporary directory where to create the file
	 * @throws IOException
	 */
	public static void createDebianBinary(File temporaryDirectory) throws IOException {
		
		/* Create debian-binary file */
		File debianBinaryFile = new File(temporaryDirectory, "debian-binary");
		if (debianBinaryFile.exists()) debianBinaryFile.delete();
		PrintWriter printWriter = new PrintWriter(debianBinaryFile);
		printWriter.print("2.0\n");
		printWriter.flush();
		printWriter.close();
	}
	
	/**
	 * Create control.tar.gz
	 * @param temporaryDirectory Output temporary directory where to create the file
	 * @param packageName Package name
	 * @param packageVersion Package version
	 * @param packageArchitecture Package architecture
	 * @param packageDepends Package depends, null if not defined
	 * @param packageMaintainer Package maintainer, null if not defined
	 * @param packageDescription Package description, null if not defined
	 * @param preinst Pre-installation script, null if not defined
	 * @param postinst Post-installation script, null if not defined
	 * @param prerm Pre-remove script, null if not defined
	 * @param postrm Post-remove script, null if not defined
	 * @throws IOException
	 */
	public static void createControlTarGz(File temporaryDirectory, String packageName, String packageVersion, String packageArchitecture, String packageDepends, String packageMaintainer, String packageDescription, String preinst, String postinst, String prerm, String postrm) throws IOException {
		
		/* Create temporary control directory */
		File temporaryControlDirectory = new File(temporaryDirectory.getAbsolutePath() + File.separator + "control");
		if (temporaryControlDirectory.exists()) temporaryControlDirectory.delete();
		temporaryControlDirectory.mkdir();

		/* Create control file */
		File controlFile = new File(temporaryControlDirectory, "control");
		if (controlFile.exists()) controlFile.delete();
		PrintWriter printWriter = new PrintWriter(controlFile);
		printWriter.print("Package: " + ((packageName != null) ? packageName : "") + "\n");
		printWriter.print("Version: " + ((packageVersion != null) ? packageVersion : "") + "\n");
		printWriter.print("Architecture: " + ((packageArchitecture != null) ? packageArchitecture : "") + "\n");
		printWriter.print("Depends: " + ((packageDepends != null) ? packageDepends : "") + "\n");
		printWriter.print("Maintainer: " + ((packageMaintainer != null) ? packageMaintainer : "") + "\n");
		printWriter.print("Description: " + ((packageDescription != null) ? packageDescription : "") + "\n");
		printWriter.flush();
		printWriter.close();
		
		/* Copy scripts */
		if (preinst != null) FileUtils.copyFile(new File(preinst), new File(temporaryControlDirectory.getAbsolutePath() + File.separator + "preinst"));
		if (postinst != null) FileUtils.copyFile(new File(postinst), new File(temporaryControlDirectory.getAbsolutePath() + File.separator + "postinst"));
		if (prerm != null) FileUtils.copyFile(new File(prerm), new File(temporaryControlDirectory.getAbsolutePath() + File.separator + "prerm"));
		if (postrm != null) FileUtils.copyFile(new File(postrm), new File(temporaryControlDirectory.getAbsolutePath() + File.separator + "postrm"));
		
		/* Create control.tar.gz file */
		createTarGZ(true, temporaryControlDirectory.getAbsolutePath(), temporaryDirectory.getAbsolutePath() + File.separator + "control.tar.gz");
		
		/* Finally remove temporary control directory */
		FileUtils.deleteDirectory(temporaryControlDirectory);
	}
	
	/**
	 * Create data.tar.gz
	 * @param temporaryDirectory Output temporary directory where to create the file
	 * @param dataPath Data path
	 * @throws IOException
	 */
	public static void createDataTarGz(File temporaryDirectory, String dataPath) throws IOException {
		createTarGZ(true, dataPath, temporaryDirectory.getAbsolutePath() + File.separator + "data.tar.gz");
	}
	
	/**
	 * Create IPK file
	 * @param temporaryDirectory Output temporary directory where to get files
	 * @param ipkFilename IPK filename
	 * @throws IOException
	 */
	public static void createIpk(File temporaryDirectory, String ipkFilename) throws IOException {
		createTarGZ(false, temporaryDirectory.getAbsolutePath(), ipkFilename);
	}
	
	/**
	 * Create tar gz archive
	 * @param dotRoot Root directory as dot
	 * @param dataPath Data path
	 * @param tarGzFilename tar gz filename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void createTarGZ(boolean dotRoot, String dataPath, String tarGzFilename) throws FileNotFoundException, IOException {
		
		/* Create tar gz file */
		File outputFile = new File(tarGzFilename);
		if (outputFile.exists()) outputFile.delete();
		FileOutputStream fos = new FileOutputStream(outputFile);
		GzipCompressorOutputStream gz = new GzipCompressorOutputStream(fos);
		TarArchiveOutputStream tgz = new TarArchiveOutputStream(gz);
		addFileToTarGz(true, dotRoot, tgz, dataPath, "");
		tgz.flush();
		tgz.finish();
		tgz.close();
		gz.close();
		fos.close();
	}
	
	/**
	 * Add files to the tar gz archive
	 * @param rootDirectory Root directory
	 * @param dotRoot Root directory as dot
	 * @param tgz Archive instance
	 * @param path Path of the file to be added
	 * @param base Base name of the file to be added
	 * @throws IOException
	 */
	public static void addFileToTarGz(boolean rootDirectory, boolean dotRoot, TarArchiveOutputStream tgz, String path, String base) throws IOException {
		
		File file = new File(path);
		String entryName = base + file.getName();
		if (!rootDirectory) {
			TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);
			tarEntry.setIds(0, 0);
			/* Set file permissions */
			/* On Unix it is not exactly copying permissions, it applies owner permissions to everybody */
			/* On Windows, files are executable, writable and readable, so all permissions are set */
			tarEntry.setMode((file.canExecute() ? 73 : 0) + (file.canWrite() ? 146 : 0) + (file.canRead() ? 292 : 0));
			tgz.putArchiveEntry(tarEntry);
		} else if (dotRoot){
			entryName = ".";
		} else {
			entryName = "";
		}
		if (file.isFile()) {
			FileInputStream fis = new FileInputStream(file);
			IOUtils.copy(fis, tgz);
			fis.close();
			tgz.closeArchiveEntry();
		} else {
			if (!rootDirectory) tgz.closeArchiveEntry();
			final File[] children = file.listFiles();
			if (children != null) {
				for (final File child : children) {
					addFileToTarGz(false, dotRoot, tgz, child.getAbsolutePath(), (entryName.isEmpty() ? "" : (entryName + "/")));
				}
			}
		}
	}
}
