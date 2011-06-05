package org.jcoffeescript.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.jcoffeescript.JCoffeeScriptCompiler;
import org.jcoffeescript.Option;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

/**
 * JCoffeeScript Ant Task
 * <p/>
 * Based on JSMin Ant Task (jsmin-ant-task)
 * <p/>
 * User: zenithar
 * Date: 05/06/11
 * Time: 18:58
 */
public class AntTask extends Task {
    private static final int BUFFER_SIZE = 262144;
    private static final int BUFFER_OFFSET = 0;
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

    private JCoffeeScriptCompiler compiler = null;

    private Vector filesets = new Vector();
    private File srcfile;
    private File destdir;
    private File destfile;
    private boolean suffix;
    private String suffixValue = ".compiled";

    private boolean bare = false;

    private boolean force = false;

    /**
     * Receives a nested fileset from the ant task
     *
     * @param fileset The nested fileset to recieve.
     */
    public void addFileSet(FileSet fileset) {
        if (!filesets.contains(fileset)) {
            filesets.add(fileset);
        }
    }

    /**
     * Receives the destdir attribute from the ant task.
     *
     * @param destdir
     */
    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }

    /**
     * Receives the destfile attribute from the ant task.
     *
     * @param destfile
     */
    public void setDestfile(File destfile) {
        this.destfile = destfile;
    }

    /**
     * Receives the force attribute from the ant task
     *
     * @param force
     */
    public void setForce(boolean force) {
        this.force = force;
    }

    /**
     * Receives the suffix attribute from the ant task
     *
     * @param suffix
     */
    public void setSuffix(boolean suffix) {
        this.suffix = suffix;
    }

    /**
     * Set a custom suffix value
     *
     * @param suffixValue
     */
    public void setSuffixvalue(String suffixValue) {
        this.suffixValue = suffixValue;
    }

    /**
     * Set export in bare mode.
     *
     * @param bare
     */
    public void setBare(boolean bare) {
        this.bare = bare;
    }

    /**
     * Compile CoffeeScript file to JavaScript file.
     *
     * @param srcFile    CoffeeScript File
     * @param outputFile JavaScript File
     */
    public void compile(File srcFile, File outputFile) {
        File output;

        if (outputFile == null) {
            // Declare output file
            output = new File(getOutputDirectory(srcFile), getOutputFileName(srcFile));
        } else {
            output = outputFile;
        }

        //  If output file exists and is newer than source, and force is not set
        if (FILE_UTILS.isUpToDate(srcFile, output, 0) & !this.force) {
            log("Not compiling " + output.getAbsolutePath() + " File exists and is up-to-date, use force attribute.");
            return;
        }

        try {
            // Declare temp file
            File tmpFile = FILE_UTILS.createTempFile("JCoffeeScriptTemp", "tmp", null, true, true);

            // Declare input / output streams
            FileInputStream inputStream = new FileInputStream(srcFile);
            FileOutputStream outputStream = new FileOutputStream(tmpFile);

            outputStream.write(compiler.compile(readSourceFrom(inputStream)).getBytes());

            // Close file streams
            inputStream.close();
            outputStream.close();

            // Copy temp file to output file.
            FILE_UTILS.copyFile(tmpFile, output, null, this.force);
            log("Compiling " + output.getAbsolutePath());

            // Delete the temp file
            FILE_UTILS.delete(tmpFile);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    /**
     * Returns the output filename, adds .min.js as suffix attribute is set to true.
     *
     * @param file The source file.
     * @return outputFile The output filename.
     */
    public String getOutputFileName(File file) {

        // Get output file name....
        String outputFile;
        String inputFile = file.getName();

        outputFile = inputFile.substring(0, inputFile.lastIndexOf("."));
        if (this.suffix == true) {
            outputFile = outputFile + this.suffixValue;
        }
        outputFile += ".js";

        return outputFile;
    }

    /**
     * Returns the output directory, uses destdir if specified, creating it if doesn't already exist.
     *
     * @param file The source file.
     * @return outputDirectory The output directory.
     */
    public File getOutputDirectory(File file) {

        File outputDirectory;

        // If destdir has been set then use it
        if (this.destdir != null) {

            outputDirectory = this.destdir;

            // If destdir doesn't exist then create it...
            if (!outputDirectory.isDirectory()) {

                try {

                    // Make directory
                    outputDirectory.mkdirs();

                } catch (Exception e) {

                    throw new BuildException(e);

                }
            }

        } else {

            // Use source directory...
            outputDirectory = new File(file.getParent());
        }

        return outputDirectory;
    }

    /**
     * Executes the task
     */
    public void execute() throws BuildException {
        final Collection<Option> options = buildOptions();

        if(this.compiler == null) {
            this.compiler = new JCoffeeScriptCompiler(options);
        }

        // If we have a src file passed through....
        if (this.srcfile != null) {

            // Call JSMin class with src file passed through
            compile(this.srcfile, this.destfile);

            // Otherwise if there is a fileset ...
        } else if (filesets.size() != 0) {

            // Loop through fileset
            for (int i = 0; i < filesets.size(); i++) {

                // Get current fileset
                FileSet fs = (FileSet) filesets.elementAt(i);

                // Ummm....?
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());

                // Get base directory from fileset
                File dir = ds.getBasedir();

                // Get included files from fileset
                String[] srcs = ds.getIncludedFiles();

                // Loop through files
                for (int j = 0; j < srcs.length; j++) {

                    // Make file object from base directory and filename
                    File temp = new File(dir, srcs[j]);
                    compile(temp, null);
                }
            }

            // If no srcfile or fileset passed through, throw ant error
        } else {
            throw new BuildException("You must specify a srcfile attribute or a fileset child element", getLocation());
        }

    }


    private String readSourceFrom(InputStream inputStream) {
        final InputStreamReader streamReader = new InputStreamReader(inputStream);
        try {
            try {
                StringBuilder builder = new StringBuilder(BUFFER_SIZE);
                char[] buffer = new char[BUFFER_SIZE];
                int numCharsRead = streamReader.read(buffer, BUFFER_OFFSET, BUFFER_SIZE);
                while (numCharsRead >= 0) {
                    builder.append(buffer, BUFFER_OFFSET, numCharsRead);
                    numCharsRead = streamReader.read(buffer, BUFFER_OFFSET, BUFFER_SIZE);
                }
                return builder.toString();
            } finally {
                streamReader.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<Option> buildOptions() {
        final Collection<Option> options = new LinkedList<Option>();

        if (this.bare) {
            options.add(Option.BARE);
        }
        return options;
    }
}
