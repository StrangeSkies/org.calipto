package org.strum;

import javax.naming.OperationNotSupportedException;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;

@TruffleLanguage.Registration(name = "Strum", version = "0.3", mimeType = StrumLanguage.MIME_TYPE)
public class StrumLanguage extends TruffleLanguage<StrumContext> {
  public static final String MIME_TYPE = "application/x-strum";

  public static final StrumLanguage INSTANCE = new StrumLanguage();

  private StrumLanguage() {}

  @Override
  protected StrumContext createContext(TruffleLanguage.Env env) {
    return new StrumContext();
  }

  @Override
  protected CallTarget parse(ParsingRequest request) throws Exception {
    StrumContext context = new StrumContext();

    throw new OperationNotSupportedException();
  }

  @Override
  protected boolean isObjectOfLanguage(Object obj) {
    return false;
  }
}