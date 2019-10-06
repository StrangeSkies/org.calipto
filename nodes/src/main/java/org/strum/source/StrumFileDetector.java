package org.strum.source;

import java.io.IOException;
import java.nio.charset.Charset;

import com.oracle.truffle.api.TruffleFile;

public class StrumFileDetector implements TruffleFile.FileTypeDetector {
  public static final String MIME_TYPE = "application/x-strum";

  @Override
  public String findMimeType(TruffleFile file) throws IOException {
    String name = file.getName();
    if (name != null && name.endsWith(".strum")) {
      return MIME_TYPE;
    }
    return null;
  }

  @Override
  public Charset findEncoding(TruffleFile file) throws IOException {
    return null;
  }
}
