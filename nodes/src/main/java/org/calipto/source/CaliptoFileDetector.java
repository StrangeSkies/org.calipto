package org.calipto.source;

import java.io.IOException;
import java.nio.charset.Charset;

import com.oracle.truffle.api.TruffleFile;

public class CaliptoFileDetector implements TruffleFile.FileTypeDetector {
  public static final String MIME_TYPE = "application/x-calipto";

  @Override
  public String findMimeType(TruffleFile file) throws IOException {
    String name = file.getName();
    if (name != null && name.endsWith(".calipto")) {
      return MIME_TYPE;
    }
    return null;
  }

  @Override
  public Charset findEncoding(TruffleFile file) throws IOException {
    return null;
  }
}
