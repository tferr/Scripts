package bar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

import net.imagej.ImageJ;

import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt.MessageType;
import org.scijava.ui.DialogPrompt.OptionType;
import org.scijava.ui.DialogPrompt.Result;
import org.scijava.ui.UIService;
import org.scijava.util.FileUtils;

/** Installs lib files in the user's local installation */
@Plugin(type = Command.class, headless = true, menuPath = "BAR > Utilities > Install Multi-language libs...")
public class Installer implements Command {

	/* exit status */
	private int exitStatus;
	public final int CLEAN_EXIT = 0;
	public final int ERROR = 1;
	public final int UNINITIALIZED = -1;

	private static final String HELP_URL = "https://imagej.net/BAR#BAR_lib";

	@Parameter
	private LogService logService;

	@Parameter
	PlatformService platformService;

	@Parameter
	private UIService uiService;

	@Parameter(visibility = ItemVisibility.MESSAGE, persist=false)
	private final String help = helpMsg();

	@Parameter(label = "Action:", choices = { "Install files silently", "Install files in debug mode",
			"No installation. Open the documentation page" })
	private String action;

	private String helpMsg() {
		final StringBuffer sb = new StringBuffer();
		sb.append("<html>").append("<body><div WIDTH=400>")
				.append("This command (re)installs <i>BAR libs</i> on your ImageJ installation. ")
				.append("These are are centralized multi-language (IJM, Clojure, Groovy, JavaScript, ")
				.append("JRuby, Jython, etc.) template libraries exemplifying how to implement ")
				.append("reusable functions and methods that you can customize and share across your ")
				.append("macros and scripts.<br> <br>").append("For more details have a look at the BAR <a href='")
				.append(HELP_URL).append("'> documentation page</a>.");
		return sb.toString();
	}

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		ij.command().run(Installer.class, true);
	}

	@Override
	public void run() {

		if (action.toLowerCase().contains("documentation")) {
			open(HELP_URL);
			return;
		}

		final boolean verbose = action.toLowerCase().contains("debug");
		final int status = installLib(verbose);

		MessageType msgType = MessageType.QUESTION_MESSAGE;
		final OptionType optionType = OptionType.YES_NO_CANCEL_OPTION;
		String msg = "Files successfuly copied. Open lib directory?";

		if (status != CLEAN_EXIT) {
			msgType = MessageType.WARNING_MESSAGE;
			msg = "An error occurred when copying files\n";
			msg += (verbose) ? "Check Console for details" : "Turn on debugging for details";
			msg += "\nAttempt to open lib Directory?";
		}
		final Result choice = uiService.showDialog(msg, "BAR libs", msgType, optionType);
		if (choice == Result.YES_OPTION) {
			Utils.revealFile(Utils.getLibDir());
		}

	}

	public void installLib() {
		installLib(false);
	}

	public int installLib(final boolean verbose) {

		final String commonPath = ""; // We'll keep it empty for now in case it
										// parent of the lib directory changes
										// again (eg, in some previous BAR
										// versions "lib" was not on the root of
										// resources but at "scripts/BAR/")
		final String resourcePath = commonPath + "lib/";
		exitStatus = CLEAN_EXIT;
		try {
			final URL source = Utils.getBARresource(resourcePath);
			if (verbose)
				logService.info("Recursively copying resources from " + source);

			// Define destination path
			final String sourceFilename = source.getFile();
			final int startOfBasename = sourceFilename.indexOf("!") + commonPath.length() + 1;
			final String destinationPath = Utils.getBARDir() + sourceFilename.substring(startOfBasename);

			final Collection<URL> urlList = FileUtils.listContents(source, true, false);
			for (final URL url : urlList) {

				final String urlPath = url.getPath();
				final int baseIndex = urlPath.lastIndexOf("/") + 1;
				final String filename = urlPath.substring(baseIndex);

				if (filename.isEmpty())
					continue;

				File destinationFile = null;
				long bytesCopied = 0;
				try {

					if (verbose)
						logService.info("Copying " + filename + " to " + destinationPath);
					final File destinationDir = new File(destinationPath);
					destinationDir.mkdirs();
					destinationFile = new File(destinationPath, filename);
					if (!destinationFile.exists()) {
						destinationFile.createNewFile();
					} else if (verbose) {
						logService.warn("Existing file at destination will be overwritten");
					}

					final Path to = Paths.get(destinationFile.toURI());
					final CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, };
					final InputStream in = url.openStream();
					bytesCopied = Files.copy(in, to, options);

				} finally {

					if (bytesCopied == 0 || destinationFile == null || !destinationFile.exists())
						exitStatus = ERROR;

					if (verbose) {
						if (exitStatus == ERROR)
							logService.error(filename + " could not be copied");
						else
							logService.info(filename + " copied (" + String.valueOf(bytesCopied) + " bytes)");
					}
				}

			}

		} catch (NullPointerException | IndexOutOfBoundsException | IOException | SecurityException exc) {
			if (verbose)
				exc.printStackTrace();
			exitStatus = ERROR;
		}

		return exitStatus;
	}

	private void open(final String url) {
		try {
			platformService.open(new URL(url));
			return;
		} catch (final IOException ignored) {
			uiService.showDialog("An error ocurred while opening\n" + url, MessageType.ERROR_MESSAGE);
			return;
		}
	}

}
