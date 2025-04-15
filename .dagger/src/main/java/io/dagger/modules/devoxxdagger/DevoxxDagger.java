package io.dagger.modules.devoxxdagger;

import static io.dagger.client.Dagger.dag;

import io.dagger.client.*;
import io.dagger.module.annotation.DefaultPath;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;
import java.util.concurrent.ExecutionException;

/** DevoxxDagger main object */
@Object
public class DevoxxDagger {
  @Function
  public String isClean(@DefaultPath("/") Directory sources, Secret token)
      throws ExecutionException, DaggerQueryException, InterruptedException {
    dag().signoff(sources, token).isClean();
    return "repository is clean";
  }
}
