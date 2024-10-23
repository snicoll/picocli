package picocli;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import picocli.CommandLine.Option;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Issue2342FileArg {
    static class CompileOptions {
        @Option(names = { "--compiler-arguments" }, split = " ",
            description = "Compiler arguments to use to compile generated sources.")
        private List<String> compilerArguments;

        @Option(names = "--target", description = "Test for presence in compiler arguments")
        private String target;

    }

    @Test // Quote on the command line
    public void testWithSingleArgumentValue() {
        CompileOptions co = new CompileOptions();
        String[] args = new String[] {
            "--compiler-arguments",
            "--parameters --source 21 --target 21 -nowarn",
            "--target",
			"my-file.jar"
        };
        new CommandLine(co).parseArgs(args);

        String[] expected = new String[] {
            "--parameters", "--source", "21", "--target", "21", "-nowarn"
        };
        assertArrayEquals(expected, co.compilerArguments.toArray());
		assertEquals("my-file.jar", co.target);
    }

	@Test
	public void testWithSingleArgumentValueFromFileShouldBeQuoted() throws IOException {
		Path tempDir = Files.createTempDirectory("test");
		Path argsFile = tempDir.resolve("args");
		try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(argsFile))) {
			out.println("--compiler-arguments '--parameters --source 21 --target 21 -nowarn'");
			out.println("--target my-file.jar");
		}
		String[] args = new String[]{
				String.format("@%s", argsFile.toAbsolutePath())
		};

		CompileOptions co = new CompileOptions();
		new CommandLine(co).parseArgs(args);
		String[] expected = new String[] {
				"--parameters", "--source", "21", "--target", "21", "-nowarn"
		};
		assertArrayEquals(expected, co.compilerArguments.toArray());
		assertEquals("my-file.jar", co.target);

	}

	@Test // This fails because quotes aren't present
	public void testWithSingleArgumentValueFromFile() throws IOException {
		Path tempDir = Files.createTempDirectory("test");
		Path argsFile = tempDir.resolve("args");
		try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(argsFile))) {
			out.println("--compiler-arguments --parameters --source 21 --target 21 -nowarn");
			out.println("--target my-file.jar");
		}
		String[] args = new String[]{
				String.format("@%s", argsFile.toAbsolutePath())
		};

		CompileOptions co = new CompileOptions();
		CommandLine commandLine = new CommandLine(co);
		commandLine.setUseSimplifiedAtFiles(true);
		commandLine.parseArgs(args);
		String[] expected = new String[] {
				"--parameters", "--source", "21", "--target", "21", "-nowarn"
		};
		assertArrayEquals(expected, co.compilerArguments.toArray());
		assertEquals("my-file.jar", co.target);

	}


}
