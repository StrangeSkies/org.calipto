package org.preste.source;

import java.io.IOException;
import java.nio.charset.Charset;

import com.oracle.truffle.api.TruffleFile;

public class PresteFileDetector implements TruffleFile.FileTypeDetector {
  public static final String MIME_TYPE = "application/x-preste";

  @Override
  public String findMimeType(TruffleFile file) throws IOException {
    String name = file.getName();
    if (name != null && name.endsWith(".preste")) {
      return MIME_TYPE;
    }
    return null;
  }

  @Override
  public Charset findEncoding(TruffleFile file) throws IOException {
    return null;
  }
}
