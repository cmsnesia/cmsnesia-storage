package com.cmsnesia.storage;

import ch.sbb.esta.openshift.gracefullshutdown.GracefulshutdownSpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CmsnesiaStorageApplication {

  public static void main(String[] args) {
    GracefulshutdownSpringApplication.run(CmsnesiaStorageApplication.class, args);
  }
}
