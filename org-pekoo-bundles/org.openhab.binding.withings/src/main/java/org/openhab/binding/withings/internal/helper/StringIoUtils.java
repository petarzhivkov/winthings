package org.openhab.binding.withings.internal.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringIoUtils {
	
	public static final String LINE_SEPARATOR;

    static {
        // avoid security issues
        try (final StringBuilderWriter buf = new StringBuilderWriter(4); final PrintWriter out = new PrintWriter(buf)) {
            out.println();
            LINE_SEPARATOR = buf.toString();
        }
    }
	
	public static boolean isNotBlank(String str) {
		return !StringIoUtils.isBlank(str);
	}

	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}
	
	public static BufferedReader toBufferedReader(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }
	
    public static List<String> readLines(final InputStream input, final Charset encoding) throws IOException {
        final InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(encoding));
        return readLines(reader);
    }
	
	public static List<String> readLines(final Reader input) throws IOException {
	        final BufferedReader reader = toBufferedReader(input);
	        final List<String> list = new ArrayList<>();
	        String line = reader.readLine();
	        while (line != null) {
	            list.add(line);
	            line = reader.readLine();
	        }
	        return list;
	 }
	
	
	
    public static void writeLines(final Collection<?> lines, String lineEnding, final OutputStream output, final Charset encoding) throws IOException {
		if (lines == null) {
			return;
		}
		if (lineEnding == null) {
			lineEnding = LINE_SEPARATOR;
		}
		final Charset cs = Charsets.toCharset(encoding);
		for (final Object line : lines) {
			if (line != null) {
				output.write(line.toString().getBytes(cs));
			}
			output.write(lineEnding.getBytes(cs));
		}
	}
    
    public static class Charsets {
    	public static Charset toCharset(final Charset charset) {
            return charset == null ? Charset.defaultCharset() : charset;
        }
    }

}
